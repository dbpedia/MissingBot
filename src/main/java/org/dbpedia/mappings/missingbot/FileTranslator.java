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
     * Tab-seperated translation file with three columns.
     * The second column contains source language and the third
     * column contains the destination/translated language of the second.
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
     * Secong column should be the orignal language
     * and in the third column should be translated
     * language.
     *
     * @throws IOException if file not found
     */
    private void readFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(this.filename));

        String line;

        while((line = reader.readLine()) != null) {
            String values[] = line.split("\t");
            // ignore first column (article name)
            String label = values[1];
            String translation = values[2];
            map.put(label, translation);
        }
        reader.close();

    }

    @Override
    public String translate(String src) {
        return map.get(src);
    }
}
