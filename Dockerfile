FROM openjdk:8
COPY . ~/dockerfolder2
WORKDIR ~/dockerfolder2
RUN apt-get update && apt-get install -y libxrender1 libxtst6 libxi6
CMD ["java", "-jar", "Engine.jar"]
