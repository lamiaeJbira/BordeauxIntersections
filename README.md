
### Smart Intersections - Application Mobile pour la Sécurité Routière

##### Description
Smart Intersections est une application mobile développée sous Android Studio, visant à améliorer la sécurité routière à Bordeaux. En exploitant les données ouvertes de Bordeaux Métropole, elle informe les usagers des intersections critiques et des chantiers en cours, tout en proposant des alertes personnalisées.

##### Fonctionnalités
- **Carte interactive** : Affichage géolocalisé des intersections et chantiers, utilisant le Google Maps SDK pour une intégration native et performante.
- **Liste des chantiers** : Consultation détaillée des zones de travaux avec fonctionnalités de recherche, filtrage et tri (par distance et type de chantier).
- **Système d’alertes** : Notifications contextuelles basées sur la proximité des zones de travaux, avec configuration personnalisable et historique des alertes.

##### Prérequis
- Android Studio et Android SDK (Android 13 - API 33)
- Émulateur ou appareil Android compatible (ex. Pixel 8)
- Connexion Internet et permissions de localisation

##### Installation

1. **Installation sur appareil** :
    - Téléchargez l’APK depuis le site officiel.
    - Autorisez l'installation d’applications provenant de sources externes.
    - Installez l'application sur votre appareil.
    - Au premier lancement, accordez les permissions requises (localisation, accès réseau).

2. **Installation en développement** :
    - Clonez le dépôt :
      ```
      git clone URL_DU_DEPOT
      cd BordeauxIntersections
      ```
    - Ouvrez le projet dans Android Studio.
    - Compilez et lancez l'application sur un émulateur ou un appareil connecté.

##### Utilisation

L'application est structurée autour de trois onglets principaux :
- **Carte** : Visualisation interactive des intersections et chantiers, avec zoom automatique et contrôles intégrés.
- **Liste** : Affichage détaillé des chantiers avec options de recherche, filtrage et tri.
- **Alertes** : Configuration des notifications en fonction de la proximité des zones de travaux, avec consultation de l’historique des alertes.

##### Structure du projet
/ ├── app/ │ ├── src/ │ │ ├── main/ │ │ │ ├── java/ │ │ │ │ └── com/example/smartintersections/ │ │ │ │ ├── activities/ │ │ │ │ ├── fragments/ │ │ │ │ └── services/ │ │ │ ├── res/ │ │ │ │ ├── layout/ │ │ │ │ └── values/ │ └── build.gradle └── README.md


##### Tests et Validation

L'application a été testée sur l'émulateur Pixel 8 sous Android 13. Les tests incluent :
- Vérification de la précision de la géolocalisation.
- Validation du déclenchement des alertes contextuelles.
- Tests de performance sur le chargement de la carte et la gestion des données.
