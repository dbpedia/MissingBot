package org.dbpedia.mappings.missingbot;


import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.dbpedia.mappings.missingbot.create.NewMappingArticle;
import org.dbpedia.mappings.missingbot.create.Record;
import org.dbpedia.mappings.missingbot.rest.filter.CharsetResponseFilter;
import org.dbpedia.mappings.missingbot.rest.filter.CorsResponseFilter;
import org.dbpedia.mappings.missingbot.storage.Store;
import org.dbpedia.mappings.missingbot.label.AllMissingLabelTitles;
import org.dbpedia.mappings.missingbot.label.TranslateLabelArticle;
import org.dbpedia.mappings.missingbot.translate.Translator;
import org.dbpedia.mappings.missingbot.translate.file.FileTranslator;
import org.dbpedia.mappings.missingbot.translate.google.TranslateLabel;
import org.dbpedia.mappings.missingbot.util.ParseCSV;
import org.glassfish.grizzly.http.server.HttpServer;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static Configuration config;
    private static MediaWikiBot bot;

    public static List<String> getArticles(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        String line;
        ArrayList<String> articles = new ArrayList<String>();

        while((line = reader.readLine()) != null) {
            String values[] = line.split("\t");
            articles.add(values[0]);
        }
        reader.close();

        return articles;
    }

    private static URI getBaseURI(int port) {
        return UriBuilder.fromUri("http://0.0.0.0/").port(port).build();
    }

    protected static HttpServer startServer(int port) throws IOException {
        System.out.println("Starting grizzly...");
        ResourceConfig rc = new PackagesResourceConfig("org/dbpedia/mappings/missingbot/rest/resources");
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, CorsResponseFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, CharsetResponseFilter.class.getName());

        URI baseUri = getBaseURI(port);

        return GrizzlyServerFactory.createHttpServer(baseUri, rc);
    }

    public static void startH2Console() {
        try {
            Server.createWebServer().start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void run_rest(int port) throws IOException {
//        startH2Console();
        HttpServer httpServer = startServer(port);
        String addr = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        System.out.println(String.format("Jersey app started with WADL available at "
                        + "%s/application.wadl\nTry out %s/missings\nHit enter to stop it...",
                addr, addr));

        System.in.read();
        httpServer.stop();
    }

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
                          "config file for dbpedia mappings wiki (default: bot.properties)");

        options.addOption("t",
                          "translation_file",
                          true,
                          "Tab seperated file with one translation per line e.g. <article>\\t<english label>\\t<translation>\\n");

        options.addOption("f",
                          "filter",
                          true,
                          "filter for missing labels. Options: OntologyClass, OntologyProperty and Datatype. Default: All");

        options.addOption( "db",
                           false,
                            "path for h2 db to save missings in.");

        options.addOption( "create_mappings",
                           true,
                           "create missing template mappings from file.");

        options.addOption( "start_rest",
                           false,
                           "start rest service for to request articles with missing labels.");

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

            String configFile;

            if(line.hasOption("config")) {
                configFile = line.getOptionValue("config");
            } else {
                configFile = "bot.properties";
            }

            try {
                config = new PropertiesConfiguration(configFile);
            } catch (ConfigurationException e) {
                e.printStackTrace();
                System.exit(1);
                return;
            }

            if(line.hasOption("start_rest")) {
                try {
                    Store.initStore(config.getString("jdbc_url"));
                    run_rest(config.getInt("rest_port"));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                System.exit(0);
            }

            bot = new MediaWikiBot(config.getString("wikihosturl"));
            bot.login(config.getString("wikiuser"),
                      config.getString("password"));

            if(line.hasOption("create_mappings")) {
                String template_path = line.getOptionValue("create_mappings");
                List<Record> records = null;
                try {
                    records = ParseCSV.parseCreationCSV(template_path);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                String title_prefix = "Mapping_commons:";

                for(Record record : records) {
                    NewMappingArticle creator = new NewMappingArticle(
                            bot,
                            title_prefix + record.getName(),
                            record.getCategory(),
                            record.getUrl().split(" ")
                    );

                    if(creator.exists()) {
                        logger.info("Article with title: " + creator.getTitle() + " already exists.");
                        logger.info("Nothing to do.");
                    } else {
                        creator.save();
                        logger.info("Created mapping under title: " + creator.getTitle());
                    }
                }

                System.exit(0);
            }

            // for argument create_mappings no language is needed
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

            String language = line.getOptionValue("lang");

            if(line.hasOption("list_missing")) {
                AllMissingLabelTitles apt = new AllMissingLabelTitles(language, filter);

                for (String missing : apt) {
                    TranslateLabelArticle article = new TranslateLabelArticle(bot, missing, language);

                    if(!article.foundLabel()) {
                        continue;
                    }

                    String translation = "";
                    try {
                        TranslateLabel translate = new TranslateLabel(
                                config.getString("google_api_key"),
                                config.getString("app_name"));

                        translation = translate.translate(article.en_label, language);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(line.hasOption("db")) {
                        Store.initStore(config.getString("jdbc_url"));
                        Store store = new Store();
                        store.put(missing, article.en_label, translation, language);
                        logger.info(missing + "\t" + article.en_label + "\t" + translation);
                    } else {
                        System.out.println(missing + "\t" + article.en_label + "\t" + translation);
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
            List<String> articles;

            try {
                trans = new FileTranslator(line.getOptionValue("translation_file"));
                articles = getArticles(line.getOptionValue("translation_file"));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
                return;
            }

            int change_counter = 0;

            for (String missing : articles) {
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

                String old_revision = article.getRevisionId();

                // make minor edit and add summary
                article.setMinorEdit(true);
                article.setEditSummary("label@" + language + " = " + translated_label);

                article.save();
                change_counter++;

                logger.info(pad + "Revision from changed \"" +
                            old_revision +
                            "\" to \"" + article.getRevisionId() + "\"");

                logger.info("done!");
            }

            logger.info("Translated " + change_counter + " Labels.");

        }
        catch( ParseException exp ) {
            // oops, something went wrong
            formatter.printHelp( "missingBot", options );
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.exit(1);
        }
    }
}

