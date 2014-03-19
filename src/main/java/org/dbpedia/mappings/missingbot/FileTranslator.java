package org.dbpedia.mappings.missingbot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileTranslator implements Translator {

    private String filename;
    private Map<String, String> map = new HashMap<String, String>();

    public FileTranslator(String filename) throws IOException {
        this.filename = filename;
        this.readFile();
    }

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
