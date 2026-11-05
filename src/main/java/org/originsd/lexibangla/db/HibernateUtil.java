package org.originsd.lexibangla.db;

import java.nio.file.Path;
import java.util.Properties;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.originsd.lexibangla.config.AppConfig;
import org.originsd.lexibangla.model.Favorite;
import org.originsd.lexibangla.model.PersonalNote;
import org.originsd.lexibangla.model.SearchHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstrap and lifecycle management for Hibernate ORM SessionFactory
 * connecting to the writable user database (lexibangla-user.sqlite).
 */
public final class HibernateUtil {

    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);
    private static volatile SessionFactory sessionFactory;
    private static Path customDatabasePath;

    private HibernateUtil() {}

    /**
     * Allows overriding the database path (useful for automated testing with isolated or in-memory sqlite).
     */
    public static synchronized void setCustomDatabasePath(Path path) {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            sessionFactory = null;
        }
        customDatabasePath = path;
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    sessionFactory = buildSessionFactory(customDatabasePath != null ? customDatabasePath : AppConfig.getUserDatabasePath());
                }
            }
        }
        return sessionFactory;
    }

    public static SessionFactory buildSessionFactory(Path dbPath) {
        try {
            log.info("Bootstrapping Hibernate SessionFactory for user database: {}", dbPath);

            Configuration configuration = new Configuration();

            Properties settings = new Properties();
            settings.put(AvailableSettings.JAKARTA_JDBC_DRIVER, "org.sqlite.JDBC");
            settings.put(AvailableSettings.JAKARTA_JDBC_URL, "jdbc:sqlite:" + dbPath.toAbsolutePath().toString().replace('\\', '/'));
            settings.put(AvailableSettings.DIALECT, "org.hibernate.community.dialect.SQLiteDialect");
            settings.put(AvailableSettings.HBM2DDL_AUTO, "update");
            settings.put(AvailableSettings.SHOW_SQL, "false");
            settings.put(AvailableSettings.FORMAT_SQL, "false");
            settings.put(AvailableSettings.AUTOCOMMIT, "false");
            settings.put("hibernate.connection.journal_mode", "WAL");
            settings.put("hibernate.connection.busy_timeout", "5000");

            configuration.setProperties(settings);

            // Register JPA Entities
            configuration.addAnnotatedClass(Favorite.class);
            configuration.addAnnotatedClass(SearchHistory.class);
            configuration.addAnnotatedClass(PersonalNote.class);

            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties());

            return configuration.buildSessionFactory(registryBuilder.build());
        } catch (Exception e) {
            log.error("Failed to build Hibernate SessionFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            log.info("Shutting down Hibernate SessionFactory");
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
