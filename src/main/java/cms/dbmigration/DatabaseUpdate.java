package cms.dbmigration;

import org.flywaydb.core.Flyway;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;


/**
 * Runs the db update tool.
 *
 * Database connection properties are taken from portal.properties (from PORTAL_CONFIG env var)
 * Flyway specific properties are taken from flyway.properties(from classpath)
 * Supports command line properties that can override the existing ones.
 * For full list of properties please check http://flywaydb.org/documentation/commandline/migrate.html
 *
 * usage : java -jar db-update [-option1 value1] [-option2 value2] ...
 *
 * examples:
 *      java -jar db-update
 *      java -jar db-update -locations sql,/path_to_external_scripts
 */
public class DatabaseUpdate {
    public static final String CONFIG_DIR_ENV = "PORTAL_CONFIG";
    public static final String APP_PROPERTIES_FILE = "portal.properties";
    public static final String FLYWAY_PROPERTIES_FILE = "flyway.properties";

    public static void main(String[] args) throws IOException {
        Flyway flyway = new Flyway();
        flyway.configure(createProperties(args));
        flyway.migrate();
    }

    private static Properties createProperties(String[] args) throws IOException {
        Properties flywayProperties = new Properties();

        //load all the properties from flyway.properties
        flywayProperties.load(loadFlywayProperties());

        //load properties from cycleon.properties and set accordingly the flyway properties
        Properties appConfig = readPropertyFile(createAppConfigFileReader());
        flywayProperties.setProperty("flyway.url", appConfig.getProperty("jdbc.url"));
        flywayProperties.setProperty("flyway.user", appConfig.getProperty("jdbc.username"));
        flywayProperties.setProperty("flyway.password", appConfig.getProperty("jdbc.password"));

        //load properties from cmd at the end
        flywayProperties.putAll(createPropertiesFromCmdArgs(args));

        return flywayProperties;
    }

    private static Properties readPropertyFile(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }

    private static InputStream loadFlywayProperties() {
        return DatabaseUpdate.class.getClassLoader().getResourceAsStream(FLYWAY_PROPERTIES_FILE);
    }

    private static Reader createAppConfigFileReader() throws FileNotFoundException {
        String propertyDirectory = getProperty(CONFIG_DIR_ENV, getenv(CONFIG_DIR_ENV));
        return new FileReader(propertyDirectory + "/" + APP_PROPERTIES_FILE);
    }

    private static Map<String, String> createPropertiesFromCmdArgs(String[] args) {
        if(args == null || args.length == 0) {
            return Collections.emptyMap();
        }
        if(args.length % 2 == 1) {
            throw new RuntimeException("Invalid argument usage. The right usage is 'java -jar db-update [-option1 value1] [-option2 value2] ...'");
        }

        Map<String, String> properties = new HashMap<String, String>();
        for(int i = 0; i < args.length; i += 2) {
            String option = args[i];
            String value = args[i + 1];
            properties.put(convertToFlywayProperty(option), value);
        }
        return properties;
    }

    private static String convertToFlywayProperty(String option) {
        if(!option.startsWith("-")) {
            throw new RuntimeException("Invalid option '" + option +"'. Maybe you wanted '-" + option + "'?");
        }
        return "flyway." + option.substring(1);
    }
}
