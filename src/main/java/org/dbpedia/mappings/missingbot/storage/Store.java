package org.dbpedia.mappings.missingbot.storage;

import org.dbpedia.mappings.missingbot.rest.bean.Missing;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by peterr on 04.06.14.
 */
public class Store {

    private Connection conn = null;
    private static String JDBC_URL;

    public static void initStore(String jdbc_url) {
        JDBC_URL = jdbc_url;

        try {
            Connection conn = DriverManager.getConnection(JDBC_URL);
            Statement stat = conn.createStatement();
//            stat.execute("DROP TABLE missing IF EXISTS");

            stat.execute("CREATE TABLE IF NOT EXISTS missing(" +
                    "title varchar(255)," +
                    "label varchar(255)," +
                    "translation varchar(255)," +
                    "language varchar(2)," +
                    "PRIMARY KEY(title, language))");

            stat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void put(String title, String label, String translation, String language) {
        try {
            Connection conn = DriverManager.getConnection(JDBC_URL);
            PreparedStatement stat = conn.prepareStatement("MERGE INTO missing VALUES(?, ?, ?, ?)");

            stat.setString(1, title);
            stat.setString(2, label);
            stat.setString(3, translation);
            stat.setString(4, language);
            stat.execute();

            stat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove(String title, String language) {
        try {
            Connection conn = DriverManager.getConnection(JDBC_URL);
            PreparedStatement stat = conn.prepareStatement("DELETE FROM missing WHERE title=? and language=?");
            stat.setString(1, title);
            stat.setString(2, language);

            stat.execute();

            stat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Map<String, Missing> getAllByLang(String language) throws IOException {
        Map<String, Missing> store = new TreeMap<>();

        try {
            Connection conn = DriverManager.getConnection(JDBC_URL);
            PreparedStatement stat = conn.prepareStatement("SELECT * FROM missing WHERE language=?");
            stat.setString(1, language);
            ResultSet rs = stat.executeQuery();

            while(rs.next()) {
                store.put(rs.getString("title"), new Missing(rs.getString("title"),
                        rs.getString("label"),
                        rs.getString("translation"),
                        language));
            }

            rs.close();
            stat.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return store;

    }
}
