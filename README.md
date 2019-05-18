# TrackAS
TrackAS is a Java tool that parses Arcan's output and tracks the architectural smells detected in each versionString analysed by Arcan.

# Installation
The installation process is simple, but it requires `git` and `mvn` (Maven) to be installed on your system.
```bash
git clone https://github.com/darius-sas/trackas
cd trackas
mvn package -DskipTests=true
```
This will compile the project and will create a `trackas` directory within the `./target` directory.
The directory will contain a `.jar` and a `lib` folder, namely the executable and the necessary libraries.

# Usage
TrackAS can be run as any standard executable `.jar` file:
```bash
java -jar trackas.jar -help
```
This command will provide further information on the available commands.

## Input formats
TrackAS is able to parse the `.graphML` files produced by Arcan representing the system dependency graph and the detected smells.
In order to be able to detect the versionString of the system analysed,these files must conform to the following naming pattern:
```
<project-name>-<versionString>.graphml
```
The files must all have the same root directory, hence it is not necessary to have them all under the same folder.
TrackAS will recursively find all the `.graphml` files in the given folder.

The versions release order shall respect the lexicographical order in order to guarantee a correct tracking.

### Running Arcan
TrackAS can also execute Arcan by itself and avoid you the trouble. The only requirement is to provide the input JAR files that conform to the same name convention mentioned for the `.graphml` files.
The following input folder structures are supported by TrackAS in order to correctly provide the JAR files to Arcan:
#### FOLDER-BASED PROJECTS
```
input-folder
|-- <project-name>-version1
|    |-- jar-name1.jar
|    `- jar-nameN.jar
|-- <project-name>-version2
|    |-- jar-name1.jar
|    `-- jar-nameN.jar
...
```
#### SINGLE JAR-BASED PROJECTS
```
input-folder
|-- jar-version1.jar
|-- jar-version2.jar
...
`-- jar-versionN.jar
```

## Output files
