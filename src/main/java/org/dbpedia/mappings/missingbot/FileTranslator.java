package org.dbpedia.mappings.missingbot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements Translator interface use a File
 * for translation lookups.
 */
public class FileTranslator implements Translator {

    private String filename;

    /**
     * Translations in a dictionary-like object.
     */
    private Map<String, String> map = new HashMap<String, String>();

    /**
     * Creates Translator Object that translates only words
     * given in the translation file.
     *
     * Tab-seperated translation file with two columns.
     * The first column contains source language and the second
     * column contains the destination/translated language of the first.
     *
     * @param filename the translation file
     * @throws IOException if file is not found
     */
    public FileTranslator(String filename) throws IOException {
        this.filename = filename;
        this.readFile();
    }

    /**
     * Read a tab seperated file with two columns.
     * First column should be the orignal language
     * and in the second column should be translated
     * language.
     *
     * @throws IOException if file not found
     */
    private void readFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(this.filename));

        String line;

        while((line = reader.readLine()) != null) {
            String values[] = line.split("\t");
            String label = values[0];
            String translation = values[1];
            map.put(label, translation);
        }
        reader.close();

    }

    @Override
    public String translate(String src) {
        return map.get(src);
    }
}
