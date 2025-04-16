package com.example.storyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button listenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser les vues
        listenButton = findViewById(R.id.listenButton);

        // Configurer les écouteurs d'événements
        listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviguer vers l'écran de saisie d'histoire
                Intent intent = new Intent(MainActivity.this, StoryInputActivity.class);
                startActivity(intent);
            }
        });
    }
}
