package org.dbpedia.mappings.missingbot.create.airpedia;

import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by peterr on 22.08.14.
 */
public class AirpediaPropertyMapping extends Article {


    public List<Pair<String, String >> properties = new ArrayList<>();

    static String property_template = "\t{{ PropertyMapping | templateProperty = %s | ontologyProperty = %s }}\n";

    static String mapping_pattern = "\\| mappings = \n";

    public AirpediaPropertyMapping(MediaWikiBot bot,
                             String title) {
        super(bot, title);

    }

    public void addProperty(String property, String ontology) {
       properties.add(new ImmutablePair<String, String>(property, ontology));
    }

    public boolean isEmpty() {
        return this.getText().length() == 0;

    }

    public boolean hasMapping() {
        Matcher m = Pattern.compile(mapping_pattern).matcher(this.getText());

        return m.find();
    }

    public String buildPropertyMapping() {

        String property_txt = "";

        for(Pair<String, String> prop : properties) {
            String template_property = prop.getLeft();
            String ontology_property = prop.getRight();

            // check if property (template_property) exists
            if(!this.getText().contains(template_property)) {
                property_txt += String.format(property_template, template_property, ontology_property);
            }
        }

        return property_txt;
    }

    public void save() {
        Matcher m = Pattern.compile(mapping_pattern).matcher(this.getText());
        m.find();

        String new_text = this.getText().substring(0, m.end()) +
                buildPropertyMapping() +
                this.getText().substring(m.end());

        this.setText(new_text);
        super.save();
    }
}
