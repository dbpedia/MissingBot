package org.dbpedia.mappings.missingbot.create;

import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

/**
 * Created by peterr on 16.07.14.
 */
public class NewMappingArticle extends Article {

    private String category;
    private String[] urls;

    private String template = "{{TemplateMapping\n" +
            "| mapToClass = Document\n" +
            "| mappings =\n" +
            "%s" +
            "\n" +
            "<!-- Please remove the following mapping once you verified the licence URL-->\n" +
            "\t{{ConstantMapping | ontologyProperty = license | value = http://mappings.dbpedia.org/index.php/Category:Unverified_Commons_media_license}}\n" +
            "}}\n" +
            "\n" +
            "[[Category:%s]]\n" +
            "\n" +
            "<!-- Please remove the following category once you verified the licence URL-->\n" +
            "[[Category:Unverified Commons media license]]";

    private String constant_mapping = "\t{{ConstantMapping | ontologyProperty = license | value = %s}}\n";

    public NewMappingArticle(MediaWikiBot bot,
                             String title,
                             String category,
                             String[] urls
    ) {
        super(bot, title);
        this.category = category;
        this.urls = urls;
    }

    public boolean exists() {
        String txt = super.getText();
        return txt.length() != 0;
    }

    private String build_text() {
        String mapping = "";

        for(String url : urls) {
            mapping += String.format(constant_mapping, url);
        }

        return String.format(this.template, mapping, this.category);
    }

    public void save() {
        String newText = build_text();
        this.setText(newText);
        super.save();
    }
}
