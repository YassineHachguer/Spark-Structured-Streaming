# park Structured Streaming avec HDFS (Docker)

## Objectif

Ce projet illustre l'utilisation de **Spark Structured Streaming** pour lire en continu des fichiers CSV déposés dans **HDFS**, appliquer un **filtrage**, et afficher les résultats en temps réel dans la console.

---

## Architecture

Les conteneurs Docker exécutent un mini cluster Hadoop + Spark :

- **HDFS (namenode + datanode)** → stockage des fichiers CSV
- **YARN (ResourceManager + NodeManager)** → gestion des ressources
- **Spark (master + worker)** → exécution du streaming

```
client (local)
│
├──> HDFS (/input)
│       ├── test.csv
│       └── test2.csv
│
└──> Spark Structured Streaming (console output)
```

---

##  Structure du projet

```
.
├── docker-compose.yml
├── config/                      # Fichier d'environnement Hadoop
├── volumes/
│   └── namenode/                # Volume persistant du namenode
├── src/main/java/org/example/
│   └── StructuredStreamingApp.java
└── README.md
```

---

##  Contenu du code

Le programme lit les fichiers CSV depuis HDFS et filtre les étudiants ayant une **note ≥ 15** :

```java
Dataset<Row> dfInput = ss.readStream()
    .option("header", true)
    .schema(schema)
    .csv("hdfs://namenode:8020/input");

Dataset<Row> dfOutput = dfInput.where("note >= 15");

StreamingQuery query = dfOutput.writeStream()
    .format("console")
    .outputMode("append")
    .start();

query.awaitTermination();
```

---

##  Démarrage du cluster

### 1️ Lancer Docker

```bash
docker compose up -d
```

### 2️ Vérifier les conteneurs

```bash
docker ps
```



---

##  Exécution du job Spark

### 1️ Copier le JAR dans le conteneur Spark Master

```bash
docker cp target/structured-streaming-app.jar spark-master:/opt/spark-app.jar
```

### 2️ Lancer le job

```bash
docker exec -it spark-master /opt/spark/bin/spark-submit \
  --class org.example.StructuredStreamingApp \
  --master spark://spark-master:7077 \
  /opt/spark-app.jar
```

Le job démarre et attend les nouveaux fichiers CSV dans le dossier `/input` de HDFS.

---

##  Gestion des fichiers dans HDFS

### 1️ Vérifier les fichiers existants

```bash
docker exec -it tp-streaming-namenode-1 hdfs dfs -ls /input
```

### 2️ Voir le contenu d'un fichier

```bash
hdfs dfs -cat /input/test.csv
```

### 3️ Ajouter un nouveau fichier (déclenche un nouvel événement streaming)

Crée un fichier local `test2.csv` :

```csv
id,nom,prenom,note
4,Leila,Naji,16
5,Youssef,Amar,19
```

```bash
hdfs dfs -put /data/test2.csv /input/
```

Le job Spark détecte automatiquement le nouveau fichier et affiche les lignes filtrées.

---

##  Exemple de sortie

```
-------------------------------------------
Batch: 1
-------------------------------------------
+---+------+--------+----+
|id |nom   |prenom  |note|
+---+------+--------+----+
|1  |Ali   |Ben     |17.0|
|3  |Omar  |Hassan  |18.0|
|4  |Leila |Naji    |16.0|
|5  |Youssef|Amar   |19.0|
+---+------+--------+----+
```

---




