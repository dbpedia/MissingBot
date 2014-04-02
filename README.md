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
$ java -jar missingBot-1.0.jar -list_missing -lang de
```

#### Output
```
...
OntologyClass:Treadmill	Treadmill
OntologyClass:Type	Type
OntologyClass:VideogamesLeague	videogames league
OntologyClass:VolleyballCoach	volleyball coach
OntologyClass:VolleyballLeague	volleyball league
...
```

### filter by title category

Filtering by missing labels by label category.

Options: OntologyClass, OntologyProperty and Datatype

```sh
$ java -jar missingBot-1.0.jar -list_missing -lang de -f OntologyClass
```

### translate labels
```sh
$ java -jar missingBot-1.0.jar -lang de -t translation.txt
```

The **translation.txt** is tab seperated with three columns.
The first column has the article title for the translation.
The second column contains the english label and the third
column contains your translation.

Note: Filering titles by category works too.

#### translation file example
```
OntologyClass:Abbey	abbey	Abtei
OntologyClass:AdministrativeRegion	administrative region	Verwaltungsregion
OntologyClass:Agent	agent	Agent
OntologyClass:Altitude	altitude	Höhe
OntologyClass:AmateurBoxer	amateur boxer	Amateurboxer
```

## License

The source code is under the terms of the [GNU General Public License, version 2](http://www.gnu.org/licenses/gpl-2.0.html).
