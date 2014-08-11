MissingBot
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

### create bot.properties
```ini
# url for the mappings wiki. Default: http://mappings.dbpedia.org
wikihosturl=http://mappings.dbpedia.org/

# your mediawiki account with edit rights
wikiuser=USERNAME

# password for your mediawiki account
password=YOUR PASSWORD

# h2db location
jdbc_url=jdbc:h2:file:${sys:user.home}/db/missing_translation;AUTO_SERVER=TRUE

# rest service port
rest_port=9998

# api key for googles translate api
google_api_key=GOOGLE_API_KEY
app_name=YOUR_APP
```

You will need edit rights.

## Run the bot

### Help output
```sh
$ java -jar missingBot-1.2.jar -help
```

```
usage: missingBot
 -c,--config <arg>             config file for dbpedia mappings wiki
                               (default: bot.properties)
 -create_mappings <arg>        create missing template mappings from file.
 -db                           store listing results in database.
 -f,--filter <arg>             filter for missing labels. Options:
                               OntologyClass, OntologyProperty and
                               Datatype. Default: All
 -h,--help                     print this message
 -ls,--list_missing <arg>      list missing labels for given 2-letter
                               language.
 -start_rest                   start rest service for to request articles
                               with missing labels.
 -t,--translation_file <arg>   Tab seperated file with one translation per
                               line e.g. <article>\t<english
                               label>\t<translation>\n
```

### list missing labels translations

This command will list all missing labels in english
from the german mappings wiki.

```sh
$ java -jar missingBot-1.2.jar -list_missing de
```

### store missing labels in database
Using the jdbc url from bot.properties.

```sh
$ java -jar missingBot-1.2.jar -list_missing de -db
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
$ java -jar missingBot-1.2.jar -list_missing de -f OntologyClass
```

### translate labels
```sh
$ java -jar missingBot-1.2.jar de -t translation.txt
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

## using rest service
The rest service get data from the h2 database.
Store missing label data in db:
```sh
$ java -jar missingBot-1.2.jar -db -ls LANG
```

Start rest service:
```sh
$ java -jar missingBot-1.2.jar -start_rest
```

## mediawiki extension
Move the extension files in your ```extensions/``` folder.

```
cp mediawiki_extension/SuggestMissingLabelTranslation/ $MEDIAWIKI/extensions
```

Add the following line to your LocalSettings.php

```php
require_once( "$IP/extensions/SuggestMissingLabelTranslation/SuggestMissingLabelTranslation.php" );
```

Change the REST Service URL on line 12 in ```$IP/extensions/SuggestMissingLabelTranslation/SuggestMissingLabelTranslation.php"```

```
define( 'SMLT_REST_URL',  "$URL/missings/");¬
```

## License

The source code is under the terms of the [GNU General Public License, version 2](http://www.gnu.org/licenses/gpl-2.0.html).
