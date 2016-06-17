package me.dibro.salesbot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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

        dataSource = new HikariDataSource(config);
    }

    public Product[] getProducts(String query) {
        return null;
    }
}