package com.example.storyapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.storyapp.api.OpenAIApiClient;
import com.example.storyapp.model.Story;
import com.example.storyapp.storage.StoryStorage;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoryInputActivity extends AppCompatActivity {

    private EditText childNameInput;
    private EditText storyElementsInput;
    private Button generateButton;
    private ProgressBar loadingIndicator;
    private TextView loadingText;

    private OpenAIApiClient apiClient;
    private StoryStorage storyStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_input);

        // Initialiser les vues
        childNameInput = findViewById(R.id.childNameInput);
        storyElementsInput = findViewById(R.id.storyElementsInput);
        generateButton = findViewById(R.id.generateButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        loadingText = findViewById(R.id.loadingText);

        // Initialiser les clients
        apiClient = new OpenAIApiClient();
        storyStorage = new StoryStorage(this);

        // Configurer les écouteurs d'événements
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateStory();
            }
        });
    }

    private void generateStory() {
        // Récupérer les entrées utilisateur
        String childName = childNameInput.getText().toString().trim();
        String storyElements = storyElementsInput.getText().toString().trim();

        // Valider les entrées
        if (childName.isEmpty()) {
            childNameInput.setError("Veuillez entrer un prénom");
            return;
        }

        if (storyElements.isEmpty()) {
            storyElementsInput.setError("Veuillez entrer des éléments pour l'histoire");
            return;
        }

        // Afficher l'indicateur de chargement
        loadingIndicator.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);

        // Lancer la génération de l'histoire en arrière-plan
        new StoryGenerationTask(childName, storyElements).execute();
    }

    private class StoryGenerationTask extends AsyncTask<Void, Void, Story> {
        private final String childName;
        private final String storyElements;
        private String errorMessage;

        public StoryGenerationTask(String childName, String storyElements) {
            this.childName = childName;
            this.storyElements = storyElements;
        }

        @Override
        protected Story doInBackground(Void... voids) {
            try {
                // Générer l'histoire
                String storyContent = apiClient.generateStory(childName, storyElements);
                
                // Extraire le titre de l'histoire (première ligne)
                String title = extractTitle(storyContent);
                
                // Diviser l'histoire en parties
                List<String> storyParts = parseStoryParts(storyContent);
                
                // Générer l'image
                String imageUrl = apiClient.generateImage(title);
                
                // Créer l'objet Story
                Story story = new Story();
                story.setId(UUID.randomUUID().toString());
                story.setTitle(title);
                story.setChildName(childName);
                story.setStoryParts(storyParts);
                story.setImageUrl(imageUrl);
                story.setCreationDate(System.currentTimeMillis());
                
                // Sauvegarder l'histoire
                storyStorage.saveStory(story);
                
                return story;
                
            } catch (IOException | JSONException e) {
                errorMessage = "Erreur lors de la génération de l'histoire: " + e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Story story) {
            // Masquer l'indicateur de chargement
            loadingIndicator.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
            generateButton.setEnabled(true);

            if (story != null) {
                // Naviguer vers l'écran d'affichage de l'histoire
                Intent intent = new Intent(StoryInputActivity.this, StoryDisplayActivity.class);
                intent.putExtra("STORY_ID", story.getId());
                startActivity(intent);
            } else {
                // Afficher un message d'erreur
                Toast.makeText(StoryInputActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }

        private String extractTitle(String storyContent) {
            // Extraire la première ligne comme titre
            String[] lines = storyContent.split("\\n");
            if (lines.length > 0) {
                return lines[0].replaceAll("\\[PARTIE[123]\\]", "").trim();
            }
            return "Histoire de " + childName;
        }

        private List<String> parseStoryParts(String storyContent) {
            List<String> parts = new ArrayList<>();
            
            // Utiliser une expression régulière pour extraire les parties
            Pattern pattern = Pattern.compile("\\[PARTIE(\\d)\\](.*?)(?=\\[PARTIE\\d\\]|$)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(storyContent);
            
            while (matcher.find()) {
                String part = matcher.group(2).trim();
                parts.add(part);
            }
            
            // Si aucune partie n'a été trouvée, utiliser tout le contenu comme une seule partie
            if (parts.isEmpty()) {
                parts.add(storyContent);
            }
            
            return parts;
        }
    }
}
