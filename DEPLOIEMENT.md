# Guide de Déploiement Détaillé - Serveur Snake Java

Version révisée et améliorée par Jules.

Ce document fournit des instructions complètes et détaillées pour déployer votre serveur de jeu Snake sur une VM Oracle Cloud gérée par un panel Pelican/Pterodactyl. Il couvre également la méthode manuelle pour référence.

## Sommaire
1.  [Analyse du Projet (Rappel)](#1--analyse-du-projet-rappel)
2.  [Étape Cruciale : Configuration Réseau sur Oracle Cloud](#2--étape-cruciale--configuration-réseau-sur-oracle-cloud)
3.  [Méthode A : Déploiement Manuel (Avancé)](#3--méthode-a--déploiement-manuel-avancé)
4.  [Méthode B : Déploiement via le Panel Pelican (Recommandé)](#4--méthode-b--déploiement-via-le-panel-pelican-recommandé)
    *   [4.1. Importer l'Œuf (Egg)](#41--importer-lœuf-egg)
    *   [4.2. Créer le Serveur](#42--créer-le-serveur)
    *   [4.3. Premier Démarrage et Installation](#43--premier-démarrage-et-installation)
5.  [Section de Dépannage (Troubleshooting)](#5--section-de-dépannage-troubleshooting)

---

### 1. Analyse du Projet (Rappel)
*   **Langage :** Java 11
*   **Construction :** Maven (crée un fichier `.jar` exécutable)
*   **Port :** Le serveur utilise la variable d'environnement `PORT` pour définir son port d'écoute. Si elle n'est pas définie, il utilise `7070` par défaut.

---

### 2. Étape Cruciale : Configuration Réseau sur Oracle Cloud
Aucun joueur ne pourra se connecter si les ports ne sont pas ouverts. Cette étape est **obligatoire**.

1.  **Connectez-vous** à votre console Oracle Cloud (OCI).
2.  Dans le menu, allez à **Networking** -> **Virtual Cloud Networks (VCN)**.
3.  Cliquez sur la VCN où se trouve votre VM.
4.  Sur la gauche, cliquez sur **Security Lists** et sélectionnez la liste de sécurité de votre VM (généralement le nom contient "Default Security List").
5.  Cliquez sur **Add Ingress Rules**.
6.  Remplissez comme suit :
    *   **Source Type:** `CIDR`
    *   **Source CIDR:** `0.0.0.0/0` (cela signifie "depuis n'importe où sur Internet")
    *   **IP Protocol:** `TCP`
    *   **Destination Port Range:** `7070` (ou le port que vous prévoyez d'utiliser. Vous pouvez aussi ouvrir une plage, par exemple `7070-7080`, si vous prévoyez d'héberger plusieurs serveurs).
7.  Cliquez sur le bouton **Add Ingress Rules** en bas. La règle est appliquée immédiatement.

---

### 3. Méthode A : Déploiement Manuel (Avancé)
Cette méthode est utile pour tester ou pour ceux qui préfèrent ne pas utiliser de panel.

1.  **Connectez-vous en SSH** à votre VM.
2.  **Installez Java 11 et Maven** :
    ```bash
    sudo apt update && sudo apt install -y openjdk-11-jdk maven
    ```
3.  **Vérifiez l'installation** :
    ```bash
    java -version
    mvn -version
    ```
4.  **Placez les fichiers** de votre projet dans un dossier (par exemple, via `git clone`).
5.  **Compilez le projet** depuis la racine de votre projet :
    ```bash
    mvn clean package
    ```
6.  **Lancez le serveur** en définissant le port :
    ```bash
    PORT=7070 java -jar target/snake-game-1.0-SNAPSHOT.jar
    ```
    *Note : Pour que le serveur continue de tourner après votre déconnexion, utilisez un outil comme `screen`.*

---

### 4. Méthode B : Déploiement via le Panel Pelican (Recommandé)
C'est la méthode la plus simple et la plus robuste pour une gestion à long terme.

#### 4.1. Importer l'Œuf (Egg)
L'œuf est un modèle qui explique à votre panel comment installer, démarrer et gérer votre serveur.

1.  **Téléchargez le fichier** `egg-snake-java.json` qui se trouve dans ce projet.
2.  Dans votre panel Pelican/Pterodactyl, allez dans la **zone d'administration** (icône en forme de clé à molette).
3.  Dans le menu de gauche, cliquez sur **Nests**. Les "Nests" sont des catégories pour regrouper les œufs (par ex: "Minecraft", "Jeux de survie", "Java").
4.  Choisissez un "Nest" existant (comme "Java") ou créez-en un nouveau.
5.  En haut à droite, cliquez sur le bouton bleu **Import Egg**.
6.  **Sélectionnez le fichier** `egg-snake-java.json` que vous avez téléchargé.
7.  Cliquez sur **Import**. L'œuf "Snake Game (Java via Maven)" devrait maintenant apparaître dans la liste des œufs de votre "Nest".

> *[Image suggérée : Une capture d'écran montrant le bouton "Import Egg" dans l'interface de Pterodactyl.]*

#### 4.2. Créer le Serveur
Maintenant que le panel a le modèle, nous pouvons créer un serveur basé sur celui-ci.

1.  Quittez la zone d'administration pour revenir à la vue utilisateur.
2.  Allez dans la section **Servers** et cliquez sur **Create New**.
3.  **Remplissez les informations de base** : nom du serveur, propriétaire...
4.  **Allocation & Port** : Assignez une adresse IP et le port principal (`7070`).
5.  **Nest Configuration** :
    *   **Nest** : Choisissez le "Nest" où vous avez importé l'œuf.
    *   **Egg** : Sélectionnez **"Snake Game (Java via Maven)"** dans la liste.
6.  **Configuration du Dépôt** : Configurez le panel pour qu'il télécharge automatiquement votre jeu depuis GitHub.
7.  **Variables de l'Œuf** :
    *   **Nom du Fichier JAR** : Le champ est pré-rempli avec `snake-game-1.0-SNAPSHOT.jar`. Ne modifiez cette valeur que si vous changez le nom de l'artefact dans votre `pom.xml`.
8.  **Définissez les limites de ressources** (CPU, RAM, Disque). 512 Mo de RAM sont un bon point de départ.
9.  Cliquez sur **Create Server**. Le panel va maintenant créer le conteneur pour votre serveur.

> *[Image suggérée : Une capture d'écran de la page de création de serveur, mettant en évidence la sélection du Nest et de l'Egg.]*

#### 4.3. Premier Démarrage et Installation
1.  Allez sur la page de votre nouveau serveur.
2.  Cliquez sur le bouton **Start**.
3.  Le panel va d'abord lancer le **script d'installation**. Vous verrez dans la console les messages de Maven qui télécharge les dépendances et compile votre projet. Cela peut prendre une à deux minutes.
4.  Une fois la compilation terminée, le script s'arrête et le panel lance automatiquement la **commande de démarrage**.
5.  Surveillez la console. Vous devriez voir le message de confirmation : **"Javalin has started"**.
6.  Le statut du serveur passera alors de **"Starting"** à **"On"**. Votre serveur est prêt !

---

### 5. Section de Dépannage (Troubleshooting)

**Problème : Le serveur reste bloqué sur "Starting" et redémarre en boucle.**
*   **Cause probable :** La compilation a échoué ou le serveur plante au démarrage.
*   **Solution :**
    1.  Allez dans l'onglet **"Settings"** de votre serveur.
    2.  Cliquez sur le bouton **"Reinstall Server"**. Cela relancera le script d'installation.
    3.  Regardez attentivement les logs de la console pendant l'installation. Cherchez des erreurs marquées en rouge (`[ERROR]`). Souvent, il s'agit d'une erreur de syntaxe dans votre `pom.xml` ou d'une dépendance qui n'a pas pu être téléchargée.

**Problème : L'importation de l'œuf échoue avec une erreur.**
*   **Cause probable :** Le fichier JSON est mal formaté ou votre version de Pterodactyl/Pelican est très ancienne.
*   **Solution :**
    1.  Assurez-vous d'avoir téléchargé le fichier `egg-snake-java.json` de la dernière version.
    2.  Ouvrez le fichier dans un éditeur de texte et vérifiez qu'il n'a pas été modifié. Vous pouvez utiliser un validateur JSON en ligne pour vous assurer que sa structure est correcte.

**Problème : Le serveur est "On", mais je ne peux pas me connecter.**
*   **Cause probable :** Le port n'est pas correctement ouvert dans le pare-feu d'Oracle Cloud.
*   **Solution :**
    1.  Relisez attentivement la [section 2](#2--étape-cruciale--configuration-réseau-sur-oracle-cloud) et vérifiez, point par point, que votre "Ingress Rule" est correctement configurée.
    2.  Assurez-vous que le port dans la règle de sécurité correspond bien au port que vous avez assigné à votre serveur dans le panel.
