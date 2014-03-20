package org.dbpedia.mappings.missingbot;


import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static Options constructOptions() {
        // create Options object
        Options options = new Options();

        options.addOption("h",
                          "help",
                          false,
                          "print this message");

        options.addOption("ls",
                "list_missing",
                false,
                "list missing labels for given language");

        options.addOption("l",
                          "lang",
                          true,
                          "2-letter language code for missing mappings in wiki.");

        options.addOption("c",
                          "config",
                          true,
                          "config file for dbpedia mappings wiki (default: wiki.properties)");

        options.addOption("t",
                          "translation_file",
                          true,
                          "Tab seperated file with one translation per line e.g. <english label>\\t<translation>\\n");

        options.addOption("f",
                          "filter",
                          true,
                          "filter for missing labels. Options: OntologyClass, OntologyProperty and Datatype. Default: All");

        return options;
    }

    public static void main(String[] args) {

        Options options = constructOptions();

        HelpFormatter formatter = new HelpFormatter();

        // create the parser
        CommandLineParser parser = new GnuParser();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );

            if(line.hasOption("help")) {
                formatter.printHelp( "missingBot", options );
                System.exit(0);
            }

            if(!line.hasOption("lang")) {
                System.err.println("Missing required option: lang");
                formatter.printHelp( "missingBot", options );
                System.exit(1);
                return;
            }

            String filter = "";
            if(line.hasOption("filter")) {
                List<String> filters = Arrays.asList("OntologyClass", "OntologyProperty", "Datatype");
                for(String f : filters) {
                    String filter_option = line.getOptionValue("filter");
                    if(filter_option.equals(f)) {
                        filter = filter_option + ":";
                    }
                }
            }

            String configFile;

            if(line.hasOption("config")) {
                configFile = line.getOptionValue("config");
            } else {
                configFile = "wiki.properties";
            }

            Configuration config;

            try {
                config = new PropertiesConfiguration(configFile);
            } catch (ConfigurationException e) {
                e.printStackTrace();
                System.exit(1);
                return;
            }

            String language = line.getOptionValue("lang");

            MediaWikiBot bot = new MediaWikiBot(config.getString("wikihosturl"));
            bot.login(config.getString("wikiuser"),
                      config.getString("password"));

            AllMissingLabelTitles apt = new AllMissingLabelTitles(language, filter);

            // TODO es sollen englische Label gelistet werden
            if(line.hasOption("list_missing")) {
                for (String missing : apt) {
                    TranslateLabelArticle article = new TranslateLabelArticle(bot, missing, language);

                    if(article.foundLabel()) {
                        System.out.println(article.en_label);
                    }
                }
                System.exit(0);
            }

            if(!line.hasOption("translation_file")) {
                System.err.println("Missing required option: translation_file");
                formatter.printHelp( "missingBot", options );
                System.exit(1);
            }

            Translator trans;

            try {
                trans = new FileTranslator(line.getOptionValue("translation_file"));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
                return;
            }

            int change_counter = 0;

            for (String missing : apt) {
                logger.info("Processing " + missing + " ...");
                String pad = String.format("%11s", "");

                TranslateLabelArticle article = new TranslateLabelArticle(bot, missing, language);
                String translated_label = trans.translate(article.en_label);

                if(!article.foundLabel()) {
                    logger.info(pad + "No english label found in: " + missing);
                    logger.info("abort!");
                    continue;
                } else if(translated_label == null) {
                    logger.info(pad + "Found no Translation for: \"" + article.en_label + "\"");
                    logger.info("abort!");
                    continue;
                } else if(article.translationAlreadyExists()) {
                    logger.info(pad + "Translation already exists.");
                    logger.info("abort!");
                    continue;
                } else {
                    logger.info(pad + "Translate \"" + article.en_label + "\" to \"" + translated_label + "\"");
                }

                article.translated_label = translated_label;

                article.save();
                change_counter++;

                logger.info(pad + "Revision from changed \"" +
                            article.getRevisionId() +
                            "\" to \"" + article.getRevisionId() + "\"");

                logger.info("done!");
            }

            logger.info("Translated " + change_counter + " of " + apt.length + " Labels.");

        }
        catch( ParseException exp ) {
            // oops, something went wrong
            formatter.printHelp( "missingBot", options );
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.exit(1);
        }
    }
}

