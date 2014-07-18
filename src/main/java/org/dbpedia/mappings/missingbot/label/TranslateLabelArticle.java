package org.dbpedia.mappings.missingbot;

import net.sourceforge.jwbf.core.actions.util.ProcessException;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslateLabelArticle extends Article {

    /**
     * Start of the original wiki text.
     */
    private String first;

    /**
     * Rest of the original wiki text.
     */
    private String last;

    /**
     * the english label from the article, if found
     */
    public String en_label;
    public String translated_label;

    /**
     * Format that is used in the article for labels.
     */
    private String label_format;

    /**
     * 2-letter language code for the new label.
     */
    private String language;


    public TranslateLabelArticle(MediaWikiBot bot, String title, String language) {
        super(bot, title);

        // TODO: removed for TokenBot without login
//        if(!bot.isLoggedIn()) {
//            throw new ProcessException("User is not logged in.");
//        }

        this.language = language;
        try {
            this.parse();
        } catch(NoSuchElementException e) {
            // ignore jwbf bug, if response is to slow?
        }
    }

    /**
     * Parsing the wiki text and find english label.
     * Also splits old wiki text in two parts.
     * The new label will be between these two parts.
     *
     */
    private void parse() {
        // label1 und label2 are named groups for the english label
        String p1 = "\\{\\{label\\|en\\|(?<label1>.*)\\}\\}\n";

        String p2 = "\\| rdfs:label@en = (?<label2>.*)\n";

        String pattern = p1 + "|" + p2;

        // TODO wirft unerwartete NoSuchElementException
        Matcher m = Pattern.compile(pattern).matcher(this.getText());

        if(!m.find()) {
            return;
        }

        // differentiate between pattern formats
        if(m.group().matches(p1)) {
            this.label_format = "{{label|" + this.language + "|%s}}\n";

            // get english label
            this.en_label = m.group("label1").trim();
        } else if (m.group().matches(p2)) {
            this.label_format = "| rdfs:label@" + this.language + " = %s\n";

            // get english label
            this.en_label = m.group("label2").trim();
        }


        this.first = this.getText().substring(0, m.end());
        this.last = this.getText().substring(m.end());
    }


    /**
     * Check if english label is found.
     *
     * @return boolean, true if found else false
     */
    public boolean foundLabel() {
        return this.en_label != null;
    }

    /**
     * Check if translation exists for given language code.
     *
     * @return boolean, true if found else false
     */
    public boolean translationAlreadyExists() {
        String p1 = "\\{\\{label\\|" + this.language + "\\|(?<label1>.*)\\}\\}\n";
        String p2 = "\\| rdfs:label@" + this.language + " = (?<label2>.*)\n";
        String pattern = p1 + "|" + p2;

        Matcher m = Pattern.compile(pattern).matcher(this.getText());

        return m.find();
    }

    /**
     * Build new wiki page with translated label.
     *
     * @return string with new wiki text
     */
    public String build_new_text() {
        // TODO Test, ob bereits LANGUAGE labe exisitert
        StringBuilder builder = new StringBuilder();

        builder.append(this.first);
        builder.append(String.format(this.label_format, this.translated_label));
        builder.append(this.last);
        return builder.toString();
    }

    @Override
    public void save() {
        if(!this.foundLabel()) {
            return;
        }

        if(this.translationAlreadyExists()) {
            return;
        }

        if(this.translated_label == null) {
            return;
        }

        String newWikiText = build_new_text();
        super.setText(newWikiText);
        super.save();
    }
}
