package org.dbpedia.mappings.missingbot.db;

import java.sql.*;

/**
 * Created by peterr on 04.06.14.
 */
public class Store {

    private Connection conn = null;
    private String jdbc_url;

    public Store(String jdbc_url) throws SQLException {
        // TODO: jedesmal eine neue Connection ist wohl sicherer, wenn mehrere Clients anfragen
//        this.conn = DriverManager.getConnection("jdbc:h2:~/db/missing_translation");
        this.jdbc_url = jdbc_url;
        initStore();
    }

    public void initStore(){
        // TODO: Missing Labels abgleichen
        try {
            Connection conn = DriverManager.getConnection(this.jdbc_url);
//            conn_pool = JdbcConnectionPool.create(JDBC_URL, "", "");
//            Connection conn = conn_pool.getConnection();
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
            Connection conn = DriverManager.getConnection(this.jdbc_url);
//            Connection conn = conn_pool.getConnection();
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

}
