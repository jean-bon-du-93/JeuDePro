# Guide de Déploiement du Serveur Snake Java

Ce document explique comment déployer le serveur de jeu Snake sur une machine virtuelle, en utilisant deux méthodes : manuellement via SSH ou en utilisant un panel de gestion de type Pterodactyl/Pelican.

## Prérequis

*   Une machine virtuelle (VM) sous une distribution Linux (ex: Ubuntu, Debian).
*   Accès `root` ou `sudo` sur la VM.
*   Pour la méthode B, un panel Pterodactyl ou Pelican fonctionnel.

---

## 1. Analyse du Projet

*   **Type :** Application serveur (backend).
*   **Langage :** Java 11.
*   **Gestionnaire de dépendances :** Maven.
*   **Artefact :** Un fichier "fat jar" exécutable (`snake-game-1.0-SNAPSHOT.jar`) est généré dans le dossier `target/`.
*   **Port :** Le serveur écoute sur le port TCP défini par la variable d'environnement `PORT`, ou **7070** par défaut.

---

## 2. Configuration Réseau (Oracle Cloud)

Avant toute chose, vous devez autoriser le trafic entrant sur le port de votre serveur.

1.  Rendez-vous dans le tableau de bord d'Oracle Cloud.
2.  Naviguez vers **Networking > Virtual Cloud Networks (VCN)**.
3.  Sélectionnez la VCN associée à votre VM.
4.  Cliquez sur **Security Lists** et choisissez celle de votre sous-réseau.
5.  Ajoutez une **Ingress Rule** (règle entrante) :
    *   **Source:** `0.0.0.0/0`
    *   **IP Protocol:** TCP
    *   **Destination Port Range:** `7070` (ou le port que vous comptez utiliser).

---

## 3. Méthode A : Déploiement Manuel (en SSH)

Cette méthode est directe mais nécessite une gestion manuelle pour maintenir le serveur en ligne (avec des outils comme `screen` ou `tmux`).

1.  **Connexion SSH :**
    ```bash
    ssh votre_utilisateur@ip_de_votre_vm
    ```

2.  **Installation des dépendances logicielles (Java 11 & Maven) :**
    ```bash
    sudo apt update
    sudo apt install openjdk-11-jdk maven -y
    ```
    Vérifiez que l'installation a réussi :
    ```bash
    java -version
    mvn -version
    ```

3.  **Compilation du projet :**
    (Assurez-vous d'être dans le dossier racine du projet)
    ```bash
    # Compilez le projet et créez le .jar
    mvn clean package
    ```
    Un fichier `snake-game-1.0-SNAPSHOT.jar` devrait maintenant exister dans le dossier `target/`.

4.  **Lancement du serveur :**
    ```bash
    java -jar target/snake-game-1.0-SNAPSHOT.jar
    ```
    Le serveur est maintenant en ligne. Pour qu'il continue de tourner après avoir fermé le terminal, utilisez un multiplexeur de terminal comme `screen` :
    ```bash
    # Installez screen s'il n'est pas présent
    sudo apt install screen

    # Lancez une nouvelle session screen
    screen -S snake-server

    # Lancez le serveur à l'intérieur de la session
    java -jar target/snake-game-1.0-SNAPSHOT.jar

    # Pour vous détacher de la session (la laissant tourner en fond), pressez Ctrl+A puis D.
    # Pour vous rattacher plus tard, utilisez "screen -r snake-server".
    ```

---

## 4. Méthode B : Déploiement avec le Panel Pelican/Pterodactyl

C'est la méthode recommandée pour une gestion simplifiée (start/stop/restart, logs, etc.).

1.  **Importer l'Œuf (Egg) :**
    *   Dans ce dépôt, vous trouverez le fichier `egg-snake-java.json`.
    *   Dans votre panel, allez dans la section **Admin > Nests**.
    *   Sélectionnez le "Nest" de votre choix (par exemple, "Java") ou créez-en un nouveau.
    *   Cliquez sur **Import Egg** et sélectionnez le fichier `egg-snake-java.json`.

2.  **Créer un nouveau serveur :**
    *   Allez dans la section **Servers** et cliquez sur **Create New**.
    *   Remplissez les informations de base (nom du serveur, propriétaire, etc.).
    *   Dans la section **Nest Configuration**, sélectionnez le "Nest" où vous avez importé l'œuf, puis choisissez l'œuf **"Snake Game (Java)"**.
    *   Assurez-vous que la configuration du dépôt du serveur dans le panel pointe bien vers ce projet.
    *   Configurez les ressources (CPU, RAM, disque) et le port du serveur (ex: `7070`).
    *   Cliquez sur **Create Server**.

3.  **Installation et Démarrage :**
    *   Le panel va automatiquement lancer le script d'installation lors du premier démarrage. Il clonera le dépôt (si configuré) et compilera le projet.
    *   Une fois l'installation terminée, le serveur démarrera. Vous pouvez gérer le serveur (Start, Stop, Restart) et voir les logs directement depuis l'interface web du panel.
