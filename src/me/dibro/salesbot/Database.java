package me.dibro.salesbot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Database {
    private HikariDataSource dataSource;

    public Database(SalesBot salesBot) {
        HikariConfig config = new HikariConfig();

        Properties properties = salesBot.getProperties();

        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s",
                properties.getProperty("dataBaseHost"),
                properties.getProperty("dataBasePort"),
                properties.getProperty("dataBaseName")
        ));

        config.setUsername(properties.getProperty("dataBaseUser"));
        config.setPassword(properties.getProperty("dataBasePassword"));

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);

        createTable();
    }

    private void createTable() {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `sales` (\n" +
                        "`id` INT NOT NULL AUTO_INCREMENT,\n" +
                        "`name` VARCHAR(128),\n" +
                        "`descFull` VARCHAR(256),\n" +
                        "`descShort` VARCHAR(128),\n" +
                        "`sourceUrl` VARCHAR(128),\n" +
                        "`sourceName` VARCHAR(128),\n" +
                        "`inStock` TINYINT(1),\n" +
                        "`discount` SMALLINT,\n" +
                        "`price` SMALLINT,\n" +
                        "`started` VARCHAR(32),\n" +
                        "`duration` SMALLINT,\n" +
                        "PRIMARY KEY ( `id` )\n" +
                        ") DEFAULT CHARSET=utf8;"
                );
            }
        } catch (SQLException e) {
            SalesBot.info("Exception caught while creating table");
            e.printStackTrace();
        }
    }

    public Result getProducts(String query) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM `sales` WHERE `name`=?;")) {
                statement.setString(1, query);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        List<Product> list = new ArrayList<>();
                        do {
                            int idx = 2;
                            list.add(new Product(
                                    resultSet.getString(idx++),
                                    resultSet.getString(idx++),
                                    resultSet.getString(idx++),
                                    resultSet.getString(idx++),
                                    resultSet.getString(idx++),
                                    resultSet.getBoolean(idx++),
                                    resultSet.getInt(idx++),
                                    resultSet.getInt(idx++),
                                    resultSet.getString(idx++),
                                    resultSet.getInt(idx)
                            ));
                        } while (resultSet.next());
                        return new Result(list.toArray(new Product[list.size()]));
                    }
                }
            }
        } catch (SQLException e) {
            SalesBot.info("Exception caught while getting products");
            e.printStackTrace();
        }

        return null;
    }
}