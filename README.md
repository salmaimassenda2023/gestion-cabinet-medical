# Manuel de Configuration de l'Environnement

Ce guide détaille les étapes nécessaires pour configurer et lancer l'environnement de développement local pour le projet "Gestion Cabinet Médical".

## 1. Prérequis Système

Assurez-vous d'avoir les outils suivants installés sur votre machine (macOS/Windows/Linux) :

*   **Java JDK 17+** : Requis pour le backend Spring Boot.
*   **Node.js LTS** (v22+ recommandé) : Requis pour le frontend Angular.
*   **Docker & Docker Desktop** : Pour gérer les bases de données et l'infrastructure locale.
*   **Maven** : Pour la gestion des dépendances Java (souvent inclus dans les IDE comme IntelliJ).
*   **Git** : Pour le versioning.

## 2. Infrastructure Locale (Docker)

Le projet utilise Docker Compose pour orchestrer les bases de données et services tiers (MongoDB, PostgreSQL, Kafka, Keycloak, etc.).

1.  Ouvrez un terminal.
2.  Naviguez vers le dossier backend :
    ```bash
    cd backend
    ```
3.  Lancez les conteneurs :
    ```bash
    docker-compose up -d
    ```
4.  Vérifiez que tous les conteneurs sont "up" (actifs) via Docker Desktop ou la commande `docker ps`.

### Services Accessibles
Une fois Docker lancé, vous avez accès aux interfaces d'administration suivantes :
*   **PgAdmin** (Admin PostgreSQL) : [http://localhost:5050](http://localhost:5050)
    *   Email: `root` / Pass: `root`
*   **Keycloak** (Auth) : [http://localhost:9098](http://localhost:9098)
    *   Admin: `admin` / `admin`

## 3. Lancement du Backend (Microservices)

Le backend est composé de plusieurs microservices. Il est recommandé de les lancer dans l'ordre suivant (selon l'architecture classique Spring Cloud) :

1.  **Config Server** 
2.  **Discovery Service (Eureka)** 
3.  **Gateway Service**
4.  **Autres Services Métiers** (Patient, Cabinet, Rendez-vous, Medicament, etc.)

**Procédure pour chaque service :**
1.  Ouvrez chaque service dans votre IDE (IntelliJ IDEA recommandé ou VS Code).
2.  Ou via le terminal dans le dossier du service :
    ```bash
    mvn spring-boot:run
    ```
3.  Assurez-vous que les ports ne sont pas en conflit.

## 4. Lancement du Frontend (Angular)

1.  Ouvrez un nouveau terminal.
2.  Naviguez vers le dossier frontend :
    ```bash
    cd medical_frontend
    ```
3.  Installez les dépendances (première fois uniquement) :
    ```bash
    npm install
    ```
4.  Lancez le serveur de développement :
    ```bash
    npm start
    ```
    *Ou `ng serve` si vous avez Angular CLI global.*

5.  L'application sera accessible sur : **[http://localhost:4200](http://localhost:4200)**

## 5. Résolution des Problèmes Courants

*   **Erreur de connexion DB** : Vérifiez que Docker tourne bien (`docker ps`) et que les ports (27017, 5432) ne sont pas occupés par une autre instance locale de Mongo/Postgres.
*   **Erreur Node/NPM** : Supprimez `node_modules` et `package-lock.json` puis relancez `npm install`.
*   **Port Occupé** : Si le port 8080 ou 4200 est pris, tuez le processus `kill -9 $(lsof -t -i:8080)` (sur Mac/Linux).

