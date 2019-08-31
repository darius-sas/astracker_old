# ASTracker
ASTracker is a Java tool that parses Arcan's output and tracks the architectural smells detected in each versionString analysed by Arcan.

# Requirements
The requirements to install and execute the application are:

* JDK 11 (or newer)
* Maven 3 (3.6.0 is the one used, but other minor versions should work fine too)


# Installation
The installation process is simple, but it requires `git` and `mvn` (Maven) to be installed on your system.
```bash
git clone https://github.com/darius-sas/astracker
cd astracker
mvn clean compile assembly:single -DskipTests=true
```
or, to deploy to a docker container as a web service:
```
mvn clean package -DskipTests=true
```

This will compile the project and will create a `astracker` directory within the `./target` directory.
The directory will contain a `.jar` and a `lib` folder, namely the executable and the necessary libraries.

# Usage
ASTracker can be run as any standard executable `.jar` file:
```bash
java -jar astracker.jar -help
```
This command will provide further information on the available commands.

As an example, try running the following command on the folder `sample-data`, which contains the `.grampml`, describing the Architectural smells affecting multiple versions of Antlr:
```bash
java -jar target/astracker-0.7-jar-with-dependencies.jar -i sample-data -p antlr -o sample-data -pC
```

## Input formats
ASTracker is able to parse the `.graphML` files produced by Arcan representing the system dependency graph and the detected smells.
In order to be able to detect the versionString of the system analysed,these files must conform to the following naming pattern:
```
<project-name>-<versionString>.graphml
```
The files must all have the same root directory, hence it is not necessary to have them all under the same folder.
ASTracker will recursively find all the `.graphml` files in the given folder.

The versions release order shall respect the lexicographical order in order to guarantee a correct tracking.

### Running Arcan
ASTracker can also execute Arcan by itself and avoid you the trouble. The only requirement is to provide the input JAR files that conform to the same name convention mentioned for the `.graphml` files.
The following input folder structures are supported by ASTracker in order to correctly provide the JAR files to Arcan:
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
By default two graph files will be output by ASTracker:
```
condensed-graph-consecOnly.xml
track-graph-consecOnly.xml
```
These files contain the same information, but in different formats, each suitable for different types of analyses. 
The information described are *the architectural smells tracked* starting from the first version to the final one. Additional information, such as architectural smells characteristics, are stored as node properties.

In order to print the properties as a single CSV file, the `-pC` option can be added to the initial command line.
A file named `smell-characteristics-consecOnly.csv` will also appear in the output directory.