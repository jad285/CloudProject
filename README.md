# CloudProject

## Project Setup:
Located in this repository are 2 files (WordCount2.jar and Engine.java), a folder called Jar Dependencies, a folder called Screenshots, and a DockerFile.
\n
WordCount2.jar - Inverse Indexing Application for Cluster \n
Engine.java - Client Application/GUI
\n
In the following steps, all referenced screenshots can be found in the folder Screenshots.

## Step 1 - SETUP GOOGLE CLOUD PARTS
Go to your google cloud console and: \n
1 - create a bucket \n
2 - create a cluster (Hadoop Cluster, with Docker, HBase, and Zookeeper) - See Screenshots/cluster to see a template on creation \n
3 - get credentials (a JSON file) - https://cloud.google.com/docs/authentication/getting-started#auth-cloud-implicit-java \n

## Step 2 - UPLOAD JAR TO BUCKET
In your google cloud console, navigate to your new bucket and upload the file WordCount2.jar
Reference Screenshots/bucket-upload to see what your bucket should look like post uploading a file (NOTE: you only need WordCount2.jar in your bucket)

## Step 3 - CODE MANIPULATION
Next, you must edit a small section of the code in Engine.java

What to edit:
Between Lines 48-55

Information needed: 
myBucket = Name of bucket created in Step 1
myProjectID = The ID of your GCP Project
myCluster = Name of cluster created in Step 1
myRegion = Region tied to both cluster and project 

See Screenshots/code-manipulation as a reference to the area to change within the code
