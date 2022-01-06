# cubicfox-attendance

A simple attendance image creator based on `Benszerva`.

# requirements

1. jdk 17 (used: `openjdk version "17.0.1" 2021-10-19`); 
The source code is compatible with 8 `-Djava.version=8`

2. maven (used:`Apache Maven 3.8.4`)

3. docker (optional, used: Docker version 20.10.12)

# verify

Check the formatting with `spotless:check` goal.

Running the test

```
mvn verify
```

# run locally

```
mvn spring-boot:run
```

# build image
The jib is the image building tool used through a maven plugin. https://github.com/GoogleContainerTools/jib

The name for the image is required. `-Dimage=<THE_NAME_OF_THE_IMAGE>`

Additionally a tag can be specified: `-Dimage=<THE_NAME_OF_THE_IMAGE>:<THE_TAG_OF_THE_IMAGE>`

example; build locally with docker, with tag

```
mvn clean compile jib:dockerBuild -Dimage=cubicfox-attendance:dirty
```

# GCP
There are an already deploy version on GCP.

Check the deploy process in `cloudbuild.yaml`

