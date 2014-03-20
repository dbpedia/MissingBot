missingBot
==========

Bot for http://mappings.dbpedia.org to translate Labels.

## Build executable .jar with maven
```sh
$ git clone https://github.com/coderiot/missingBot.git
$ cd missingBot
$ mvn package
```

The executable .jar is located in the target/ directory.

## Configuration
Go to the directory where your executable .jar is located.

### create wiki.properties
```ini
# url for the mappings wiki. Default: http://mappings.dbpedia.org
wikihosturl=http://mappings.dbpedia.org/

# your mediawiki account with edit rights
wikiuser=USERNAME

# password for your mediawiki account
password=YOUR PASSWORD
```

Your need edit rights.

## Run the bot

### Help output
```sh
$ java -jar missingBot-1.0.jar -help
```

### list all missing labels

This command will list all missing labels in english
from the german mappings wiki.

```sh
$ java -jar missingBot-1.0.jar -ls -l de
```

### filter by title category

Filtering by missing labels by label category.
Options: OntologyClass, OntologyProperty and Datatype

```sh
$ java -jar missingBot-1.0.jar -ls -l de -f OntologyClass
```

### Translate labels
```sh
$ java -jar missingBot-1.0.jar -ls -l de -t translation.txt
```

The **translation.txt** is tab seperated with two columns.
The first column contains the english label and the second
column contains your translation.

Note: Filering titles by category works too.

#### translation file example
```
abbey	Abtei
administrative region	Verwaltungsregion
agent	Agent
altitude	Höhe
amateur boxer	Amateurboxer
```

## License

The source code is under the terms of the [GNU General Public License, version 2](http://www.gnu.org/licenses/gpl-2.0.html).
