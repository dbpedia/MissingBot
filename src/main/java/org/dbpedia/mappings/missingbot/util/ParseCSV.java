package org.dbpedia.mappings.missingbot.util;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;
import org.dbpedia.mappings.missingbot.create.Record;

import java.io.*;
import java.util.List;

/**
 * Created by peterr on 16.07.14.
 */
public class ParseCSV {

    public static List<Record> parseCreationFile(String path, char delimiter) throws IOException {
        ColumnPositionMappingStrategy<Record> strategy = new ColumnPositionMappingStrategy<Record>();
        strategy.setType(Record.class);

        String[] columns = new String[] {"category", "name", "template", "url"};
        strategy.setColumnMapping(columns);

        CSVReader reader = new CSVReader(new FileReader(path), delimiter, '\"', 1);
        CsvToBean<Record> csv = new CsvToBean<Record>();
        return csv.parse(strategy, reader);
    }

    public static List<Record> parseCreationCSV(String path) throws IOException {
       return parseCreationFile(path, ',');
    }

    public static List<Record> parseCreationTSV(String path) throws IOException {
        return parseCreationFile(path, '\t');
    }

}
