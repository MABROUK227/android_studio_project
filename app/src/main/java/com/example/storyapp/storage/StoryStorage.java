package com.example.storyapp.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.storyapp.model.Story;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class StoryStorage {
    private static final String TAG = "StoryStorage";
    private static final String PREF_NAME = "story_preferences";
    private static final String STORIES_KEY = "saved_stories";
    private static final int MAX_STORIES = 10; // Limite le nombre d'histoires stockées
    
    private final SharedPreferences preferences;
    
    public StoryStorage(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Sauvegarde une histoire dans le stockage local
     * @param story L'histoire à sauvegarder
     * @return true si l'opération a réussi, false sinon
     */
    public boolean saveStory(Story story) {
        try {
            // Générer un ID unique si nécessaire
            if (story.getId() == null || story.getId().isEmpty()) {
                story.setId(UUID.randomUUID().toString());
            }
            
            // Récupérer les histoires existantes
            List<Story> stories = getAllStories();
            
            // Ajouter la nouvelle histoire
            stories.add(story);
            
            // Trier par date de création (la plus récente en premier)
            Collections.sort(stories, new Comparator<Story>() {
                @Override
                public int compare(Story s1, Story s2) {
                    return Long.compare(s2.getCreationDate(), s1.getCreationDate());
                }
            });
            
            // Limiter le nombre d'histoires stockées
            if (stories.size() > MAX_STORIES) {
                stories = stories.subList(0, MAX_STORIES);
            }
            
            // Convertir la liste en JSON
            JSONArray jsonArray = new JSONArray();
            for (Story s : stories) {
                JSONObject storyJson = new JSONObject();
                storyJson.put("id", s.getId());
                storyJson.put("title", s.getTitle());
                storyJson.put("childName", s.getChildName());
                storyJson.put("imageUrl", s.getImageUrl());
                storyJson.put("creationDate", s.getCreationDate());
                
                // Convertir les parties de l'histoire en JSONArray
                JSONArray partsArray = new JSONArray();
                for (String part : s.getStoryParts()) {
                    partsArray.put(part);
                }
                storyJson.put("storyParts", partsArray);
                
                jsonArray.put(storyJson);
            }
            
            // Sauvegarder dans les SharedPreferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(STORIES_KEY, jsonArray.toString());
            return editor.commit();
            
        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors de la sauvegarde de l'histoire", e);
            return false;
        }
    }
    
    /**
     * Récupère toutes les histoires stockées
     * @return Liste des histoires
     */
    public List<Story> getAllStories() {
        List<Story> stories = new ArrayList<>();
        
        String storiesJson = preferences.getString(STORIES_KEY, "");
        if (storiesJson.isEmpty()) {
            return stories;
        }
        
        try {
            JSONArray jsonArray = new JSONArray(storiesJson);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject storyJson = jsonArray.getJSONObject(i);
                
                Story story = new Story();
                story.setId(storyJson.getString("id"));
                story.setTitle(storyJson.getString("title"));
                story.setChildName(storyJson.getString("childName"));
                story.setImageUrl(storyJson.getString("imageUrl"));
                story.setCreationDate(storyJson.getLong("creationDate"));
                
                // Récupérer les parties de l'histoire
                JSONArray partsArray = storyJson.getJSONArray("storyParts");
                List<String> storyParts = new ArrayList<>();
                for (int j = 0; j < partsArray.length(); j++) {
                    storyParts.add(partsArray.getString(j));
                }
                story.setStoryParts(storyParts);
                
                stories.add(story);
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors de la récupération des histoires", e);
        }
        
        return stories;
    }
    
    /**
     * Récupère une histoire par son ID
     * @param storyId ID de l'histoire à récupérer
     * @return L'histoire correspondante ou null si non trouvée
     */
    public Story getStoryById(String storyId) {
        List<Story> stories = getAllStories();
        
        for (Story story : stories) {
            if (story.getId().equals(storyId)) {
                return story;
            }
        }
        
        return null;
    }
    
    /**
     * Supprime une histoire du stockage local
     * @param storyId ID de l'histoire à supprimer
     * @return true si l'opération a réussi, false sinon
     */
    public boolean deleteStory(String storyId) {
        List<Story> stories = getAllStories();
        boolean removed = false;
        
        for (int i = 0; i < stories.size(); i++) {
            if (stories.get(i).getId().equals(storyId)) {
                stories.remove(i);
                removed = true;
                break;
            }
        }
        
        if (removed) {
            try {
                // Convertir la liste en JSON
                JSONArray jsonArray = new JSONArray();
                for (Story s : stories) {
                    JSONObject storyJson = new JSONObject();
                    storyJson.put("id", s.getId());
                    storyJson.put("title", s.getTitle());
                    storyJson.put("childName", s.getChildName());
                    storyJson.put("imageUrl", s.getImageUrl());
                    storyJson.put("creationDate", s.getCreationDate());
                    
                    // Convertir les parties de l'histoire en JSONArray
                    JSONArray partsArray = new JSONArray();
                    for (String part : s.getStoryParts()) {
                        partsArray.put(part);
                    }
                    storyJson.put("storyParts", partsArray);
                    
                    jsonArray.put(storyJson);
                }
                
                // Sauvegarder dans les SharedPreferences
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(STORIES_KEY, jsonArray.toString());
                return editor.commit();
                
            } catch (JSONException e) {
                Log.e(TAG, "Erreur lors de la suppression de l'histoire", e);
                return false;
            }
        }
        
        return false;
    }
}
