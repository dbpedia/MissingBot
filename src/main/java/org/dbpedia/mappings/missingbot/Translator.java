package org.dbpedia.mappings.missingbot;

/**
 * Interface for translating a String to a specific language.
 */
public interface Translator {
    public String translate(String src);
}
