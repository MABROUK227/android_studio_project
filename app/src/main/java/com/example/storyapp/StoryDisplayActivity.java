package com.example.storyapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.storyapp.model.Story;
import com.example.storyapp.storage.StoryStorage;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class StoryDisplayActivity extends AppCompatActivity {

    private ImageView storyImage;
    private TextView storyContent;
    private Button previousButton;
    private Button nextButton;

    private StoryStorage storyStorage;
    private Story currentStory;
    private int currentPartIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_display);

        // Initialiser les vues
        storyImage = findViewById(R.id.storyImage);
        storyContent = findViewById(R.id.storyContent);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);

        // Initialiser le stockage
        storyStorage = new StoryStorage(this);

        // Récupérer l'ID de l'histoire
        String storyId = getIntent().getStringExtra("STORY_ID");
        if (storyId != null) {
            // Charger l'histoire
            currentStory = storyStorage.getStoryById(storyId);
            if (currentStory != null) {
                // Afficher l'histoire
                displayStory();
            } else {
                // Gérer l'erreur
                Toast.makeText(this, "Histoire introuvable", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Gérer l'erreur
            Toast.makeText(this, "ID d'histoire manquant", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Configurer les écouteurs d'événements
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreviousPart();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextPart();
            }
        });
    }

    private void displayStory() {
        // Charger l'image
        if (currentStory.getImageUrl() != null && !currentStory.getImageUrl().isEmpty()) {
            new DownloadImageTask(storyImage).execute(currentStory.getImageUrl());
        }

        // Mettre à jour les boutons de navigation
        updateNavigationButtons();

        // Afficher la partie actuelle de l'histoire
        displayCurrentPart();
    }

    private void displayCurrentPart() {
        List<String> storyParts = currentStory.getStoryParts();
        if (storyParts != null && !storyParts.isEmpty() && currentPartIndex < storyParts.size()) {
            storyContent.setText(storyParts.get(currentPartIndex));
        } else {
            storyContent.setText("Aucun contenu disponible");
        }
    }

    private void showPreviousPart() {
        if (currentPartIndex > 0) {
            currentPartIndex--;
            displayCurrentPart();
            updateNavigationButtons();
        }
    }

    private void showNextPart() {
        if (currentPartIndex < currentStory.getStoryParts().size() - 1) {
            currentPartIndex++;
            displayCurrentPart();
            updateNavigationButtons();
        }
    }

    private void updateNavigationButtons() {
        // Activer/désactiver le bouton précédent
        previousButton.setEnabled(currentPartIndex > 0);
        previousButton.setAlpha(currentPartIndex > 0 ? 1.0f : 0.5f);

        // Activer/désactiver le bouton suivant
        boolean hasNextPart = currentPartIndex < currentStory.getStoryParts().size() - 1;
        nextButton.setEnabled(hasNextPart);
        nextButton.setAlpha(hasNextPart ? 1.0f : 0.5f);
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }
}
