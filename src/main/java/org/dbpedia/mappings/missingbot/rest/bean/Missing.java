package org.dbpedia.mappings.missingbot.rest.bean;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Created by peterr on 24.04.14.
 */
@XmlRootElement
public class Missing {
    private String label;
    private String title;

    private String translation;

    @XmlTransient
    private String language;

    public Missing() {}

    public Missing(String title, String label, String translation, String language) {
        this.label = label;
        this.title = title;
        this.translation = translation;
        this.language = language;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String id) {
        this.label = label;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
