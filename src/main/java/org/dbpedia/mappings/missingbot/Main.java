package org.dbpedia.mappings.missingbot;


import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.dbpedia.mappings.missingbot.create.NewMappingArticle;
import org.dbpedia.mappings.missingbot.create.Record;
import org.dbpedia.mappings.missingbot.create.airpedia.AirpediaPropertyMapping;
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
import java.util.Hashtable;
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
                        + "%s/application.wadl\nTry out %s/missings\nPress Ctrl-C for exit.",
                        addr, addr));

        try {
            Thread.currentThread().join();
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            httpServer.stop();
        }
    }

    public static void create_mappings(String template_path) {
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
    }

    public static void import_airpedia_classes(String classes_file, String language) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(classes_file));

        String line;

        String template = "{{TemplateMapping\n" +
                "| mapToClass = %s\n" +
                "| mappings = \n" +
                "}}";


        while((line = reader.readLine()) != null) {
            String values[] = line.split("\t");
            if(!values[0].equals(language)) {
                continue;
            }
            String name = values[1];
            String cls = values[3];
            String title = "Mapping_" + language + ":" + name;
            System.out.println(title);
            String new_mapping = String.format(template, cls);
            System.out.println(new_mapping);
            Article article = new Article(bot, title);

            String txt = article.getText();

            if(txt.length() != 0) {
                logger.info("Article " + title + " already exists");
                continue;
            }

            article.setText(new_mapping);
            article.setEditSummary("import class mappings from airpedia for language: " + language + " with precision 0.9.");
            article.save();
            logger.info("Article " + title + " created.");

        }
        reader.close();

    }

    public static void import_airpedia_properties(String classes_file, String language) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(classes_file));

        Hashtable<String, AirpediaPropertyMapping> new_mappings = new Hashtable<String, AirpediaPropertyMapping>();

        String line;

        while((line = reader.readLine()) != null) {
            String values[] = line.split("\t");
            String lang = values[0];
            String name = values[1];
            String template_property = values[2];
            String ontology_property = values[3];
            String cap_name  = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            String title = "Mapping_" + lang + ":" + cap_name;

            if(new_mappings.get(title) == null) {
                AirpediaPropertyMapping map = new AirpediaPropertyMapping(bot,title);
                map.addProperty(template_property, ontology_property);
                new_mappings.put(title, map);
            } else {
                AirpediaPropertyMapping map = new_mappings.get(title);
                map.addProperty(template_property, ontology_property);
            }
        }

        reader.close();

        for(String title : new_mappings.keySet()) {
            AirpediaPropertyMapping article = new_mappings.get(title);

            if(article.isEmpty()) {
                logger.info("Article " + title + " does not exists");
                continue;
            }

            if(!article.hasMapping()) {
                logger.info("not found mappings in article " + title);
                continue;
            }

            String property_mapping = article.buildPropertyMapping();

            if(property_mapping.isEmpty()) {
                logger.info("properties already exists for article: " + title + "\n" );
                continue;
            }

            logger.info("Create properties for artcile: " + title + "\n" + property_mapping);
            article.setEditSummary("import property mappings from airpedia for language: " + language);
            article.save();
        }

    }

    public static void list_missing(String language, String filter, boolean db) {
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

            if(db) {
                Store.initStore(config.getString("jdbc_url"));
                Store store = new Store();
                store.put(missing, article.en_label, translation, language);
                logger.info(missing + "\t" + article.en_label + "\t" + translation);
            } else {
                System.out.println(missing + "\t" + article.en_label + "\t" + translation);
            }

        }
    }

    public static void translate_labels(List<String> articles, String language, Translator trans) {
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
                          "list missing labels for given language.");

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
                            "store listing results in database.");

        options.addOption( "create_mappings",
                           true,
                           "create missing template mappings from file.");

        options.addOption( "start_rest",
                           false,
                           "start rest service for to request articles with missing labels.");

        options.addOption( "import_template",
                           true,
                           "import template mappings from airpedia files. \n" +
                           "format: the first column contains the template/infobox, " +
                           "while the second one contains the guessed class in the DBpedia Ontology.\n" +
                           "(required: lang parameter)");

        options.addOption( "import_property",
                           true,
                           "import template properties from airpedia files.");

        return options;
    }


    public static void main(String[] args) {

        Options options = constructOptions();

        HelpFormatter formatter = new HelpFormatter();

        // create the parser
        CommandLineParser parser = new GnuParser();

        CommandLine line = null;

        try {
            // parse the command line arguments
             line = parser.parse( options, args );
        } catch( ParseException exp ) {
            // oops, something went wrong
            formatter.printHelp( "missingBot", options );
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.exit(1);
        }

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
            create_mappings(template_path);
            System.exit(0);
        }


        if(line.hasOption("import_property")) {
            String property_file = line.getOptionValue("import_property");

            try {
                import_airpedia_properties(property_file, "lang");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            System.exit(0);
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

        if(!line.hasOption("lang")) {
            System.err.println("language parameter is required.");
            System.exit(1);
        }

        String language = line.getOptionValue("lang");

        if(line.hasOption("import_template")) {
            String class_file = line.getOptionValue("import_template");

            try {
                import_airpedia_classes(class_file, language);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            System.exit(0);
        }

        if(line.hasOption("list_missing")) {
            list_missing(language, filter, line.hasOption("db"));
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

        translate_labels(articles, language, trans);

    }
}

