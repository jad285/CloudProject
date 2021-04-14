# CloudProject

## Project Setup:
Located in this repository are 2 files (WordCount2.jar and Engine.java), a folder called Jar Dependencies, a folder called Screenshots, a folder called 1, and a DockerFile.

WordCount2.jar - Inverse Indexing Application for Cluster 

Engine.java - Client Application/GUI

In the following steps, all referenced screenshots can be found in the folder Screenshots.

## Step 1 - SETUP GOOGLE CLOUD PARTS
Go to your google cloud console and: 

1 - create a bucket

2 - create a cluster (Hadoop Cluster, with Docker, HBase, and Zookeeper) - See Screenshots/cluster to see a template on creation 

3 - get credentials (a JSON file) - https://cloud.google.com/docs/authentication/getting-started#auth-cloud-implicit-java 

## Step 2 - UPLOAD JAR TO BUCKET
In your google cloud console, navigate to your new bucket and upload the file WordCount2.jar to the root of your bucket (bucketname/WordCount2.jar)

Reference Screenshots/bucket-upload to see what your bucket should look like post uploading a file (NOTE: you only need WordCount2.jar in your bucket)

## Step 3 - CODE MANIPULATION
Next, you must edit a small section of the code in Engine.java

What to edit:

Between Lines 48-55

Information needed: 

myCredentials = Replace "cool-ship-305200-926cc13293b1.json" with your own json file name

myBucket = Name of bucket created in Step 1

myProjectID = The ID of your GCP Project

myCluster = Name of cluster created in Step 1

myRegion = Region tied to both cluster and project 

*DO NOT TOUCH myLocalPathway*

See Screenshots/code-manipulation as a reference to the area to change within the code

## Step 4 - DEPENDENCIES AND EXPORTING TO JAR
At this stage, open Engine.java in Eclipse. Inside of Eclipse, right click on the src folder Engine.java is inside of and click "configure build path"

Inside of this window, click "libraries", click "Add External JARS", and add in the Jars located in the repository folder called Jar Dependencies. Click apply and close.

Next go to help in the top navigation bar and click on "Eclipse Marketplace". Search and install "Google Cloud Platform Libraries". Once installed, go back to configure build path, click "Add Library" in the libraries tab, Click "Google Cloud Platform Libraries", and checkmark the following shown in in the screenshot: Screenshots/google-cloud-libraries.

Once these steps above are done, right click on Engine.java and export as a runnable JAR file (Engine.jar)

## Step 5 - CONFIGURING YOUR FOLDERS
The first folder to configure is the folder called 1. Take this folder and move it to the path "~" (this may be a Mac term, in which case, you can move it where-ever you want but note this location

*NEXT, MOVE YOUR JSON CREDENTIALS FROM GOOGLE CLOUD INTO THIS FOLDER*

This folder now contains all the text files you can upload, along with your credentials. 

Next, you are going to need to create a new folder with your newly exported JAR file (Engine.jar) and the Dockerfile in this repository.

*ONCE THIS IS CREATED, OPEN UP YOUR DOCKERFILE AND CHANGE THE COPY TERM ~/dockerfolder2 TO THE DESTINATION OF THIS FOLDER, AND SIMILARLY, CHANGE THE WORKDIR TERM TO THE SAME THING*

Save this Dockerfile, navigate in console to inside this folder, and go to the next step.

## Step 6 - RUNNING THE APPLICATION (DOCKER)
