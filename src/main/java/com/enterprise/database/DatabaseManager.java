package com.enterprise.database;

import com.enterprise.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database Manager to handle PostgreSQL connection pooling via HikariCP and database migrations via Flyway.
 */
public class DatabaseManager {

    private static final Logger log = LogManager.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;

    // Static block to initialize the connection pool and run Flyway migrations
    static {
        initialize();
    }

    private static synchronized void initialize() {
        if (dataSource == null) {
            log.info("Initializing Database Connection Pool...");
            try {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(ConfigManager.get("db.url"));
                config.setUsername(ConfigManager.get("db.username"));
                config.setPassword(ConfigManager.get("db.password"));
                config.setDriverClassName(ConfigManager.get("db.driver", "org.postgresql.Driver"));

                // Pool Configuration
                config.setMaximumPoolSize(ConfigManager.getInt("db.pool.max", 10));
                config.setMinimumIdle(ConfigManager.getInt("db.pool.idle", 2));
                config.setIdleTimeout(30000);
                config.setConnectionTimeout(30000);

                dataSource = new HikariDataSource(config);
                log.info("Database Connection Pool successfully initialized.");

                // Run Database Migrations using Flyway
                runMigrations();

            } catch (Exception e) {
                log.error("Failed to initialize Database Connection Pool.", e);
                throw new RuntimeException("DB Initialization failed.", e);
            }
        }
    }

    /**
     * Executes Flyway database migrations to ensure the local DB is up-to-date.
     */
    private static void runMigrations() {
        log.info("Running Flyway Database Migrations...");
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load();
            
            flyway.migrate();
            log.info("Database migrations executed successfully.");
        } catch (Exception e) {
            log.error("Flyway Database Migrations failed.", e);
            // We log but don't halt execution, as DB might be pre-configured or read-only in some envs
        }
    }

    /**
     * Get a connection from the pool.
     */
    public static Connection getConnection() {
        try {
            if (dataSource == null || dataSource.isClosed()) {
                initialize();
            }
            return dataSource.getConnection();
        } catch (Exception e) {
            log.error("Failed to get database connection from pool.", e);
            throw new RuntimeException("Database connection failure.", e);
        }
    }

    /**
     * Executes a SELECT query and returns the results as a List of Maps.
     * Each Map represents a row with column name as key and value as object.
     *
     * @param sql SELECT SQL query
     * @return List of rows as Maps
     */
    public static List<Map<String, Object>> executeQuery(String sql) {
        log.info("Executing DB Query: {}", sql);
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object columnValue = rs.getObject(i);
                    row.put(columnName, columnValue);
                }
                results.add(row);
            }
            log.info("Query returned {} rows.", results.size());

        } catch (Exception e) {
            log.error("Database query execution failed: {}", sql, e);
            throw new RuntimeException("SQL Query execution failed.", e);
        }
        return results;
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE query.
     *
     * @param sql INSERT/UPDATE/DELETE SQL query
     * @return Number of rows affected
     */
    public static int executeUpdate(String sql) {
        log.info("Executing DB Update: {}", sql);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(sql);
            log.info("Update successful, rows affected: {}", rowsAffected);
            return rowsAffected;

        } catch (Exception e) {
            log.error("Database update execution failed: {}", sql, e);
            throw new RuntimeException("SQL Update execution failed.", e);
        }
    }

    /**
     * Safely closes the database connection pool.
     */
    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            log.info("Closing Database Connection Pool...");
            dataSource.close();
            log.info("Database Connection Pool closed.");
        }
    }
}
