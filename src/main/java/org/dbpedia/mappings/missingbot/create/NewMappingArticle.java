package org.dbpedia.mappings.missingbot.create;

import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

/**
 * Created by peterr on 16.07.14.
 */
public class NewMappingArticle extends Article {

    private String category;
    private String url;

    private String template = "{{TemplateMapping\n" +
            "| mapToClass = Document\n" +
            "| mappings =\n" +
            "\t{{ConstantMapping | ontologyProperty = license | value = %s}}\n" +
            "\n" +
            "<!-- Please remove the following mapping once you verified the licence URL-->\n" +
            "\t{{ConstantMapping | ontologyProperty = license | value = http://mappings.dbpedia.org/index.php/Category:Unverified_Commons_media_license}}\n" +
            "}}\n" +
            "\n" +
            "[[Category:%s]]\n" +
            "\n" +
            "<!-- Please remove the following category once you verified the licence URL-->\n" +
            "[[Category:Unverified Commons media license]]";

    public NewMappingArticle(MediaWikiBot bot,
                             String title,
                             String category,
                             String url
    ) {
        super(bot, title);
        this.category = category;
        this.url = url;
    }

    public boolean exists() {
        String txt = super.getText();
        return txt.length() != 0;
    }

    public void save() {
        String newText = String.format(this.template, this.url, this.category);
        this.setText(newText);
        super.save();
    }
}
