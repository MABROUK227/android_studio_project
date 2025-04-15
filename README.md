# Application Tales - Documentation

## Présentation

Tales est une application Android conçue pour les enfants de 3 à 10 ans, permettant de générer des histoires personnalisées à l'aide de l'API ChatGPT et des images via l'API DALL-E.

## Fonctionnalités principales

- Génération d'histoires personnalisées basées sur les préférences de l'enfant
- Création d'images illustrant chaque page de l'histoire
- Stockage des histoires générées dans Firebase
- Navigation intuitive entre les pages des histoires
- Interface adaptée aux enfants avec des couleurs vives et une typographie ludique

## Architecture

L'application est construite selon l'architecture MVVM (Model-View-ViewModel) :

- **Model** : Représente les données et la logique métier
- **View** : Représente l'interface utilisateur (Fragments, Activities)
- **ViewModel** : Fait le lien entre le Model et la View, gère les données à afficher

### Structure des packages

- `com.example.tales.model` : Classes de données
- `com.example.tales.repository` : Accès aux données (Firebase)
- `com.example.tales.service` : Services (OpenAI, Firebase)
- `com.example.tales.ui` : Interface utilisateur (Fragments)
- `com.example.tales.util` : Classes utilitaires
- `com.example.tales.viewmodel` : ViewModels

## Configuration requise

- Android 7.0 (API 24) ou supérieur
- Connexion Internet pour la génération d'histoires et d'images
- Compte Firebase pour le stockage des histoires

## Installation

1. Cloner le dépôt
2. Ouvrir le projet dans Android Studio
3. Configurer Firebase :
   - Créer un projet Firebase
   - Ajouter l'application Android au projet
   - Télécharger le fichier `google-services.json` et le placer dans le dossier `app/`
4. Configurer l'API OpenAI :
   - Obtenir une clé API OpenAI
   - Remplacer la clé API dans `OpenAIService.kt`
5. Compiler et exécuter l'application

## Utilisation

1. Écran d'accueil : Cliquer sur "Écouter une histoire dès maintenant"
2. Écran de personnalisation : Saisir les informations de l'enfant (nom, âge, préférences)
3. Génération de l'histoire : Attendre la génération de l'histoire et des images
4. Lecture de l'histoire : Naviguer entre les pages avec les boutons précédent/suivant

## Optimisations

- Gestion efficace du cycle de vie de l'application
- Optimisation de la mémoire avec nettoyage du cache Glide
- Préchargement des images pour une navigation fluide
- Vérification de la connectivité réseau avant les appels API
- Documentation complète du code pour faciliter la maintenance

## Tests

- Tests unitaires pour les repositories, services et viewmodels
- Tests d'intégration pour vérifier les interactions entre composants
- Tests sur différents appareils (téléphones et tablettes)

## Crédits

- API OpenAI pour la génération de texte et d'images
- Firebase pour le stockage des données
- Glide pour le chargement optimisé des images
