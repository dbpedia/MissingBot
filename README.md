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
```sh
$ java -jar missingBot-1.0.jar -help
```

## License

The source code is under the terms of the [GNU General Public License, version 2](http://www.gnu.org/licenses/gpl-2.0.html).
