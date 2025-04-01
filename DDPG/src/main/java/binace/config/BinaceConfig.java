package binace.config;

import property.ConfigReader;

import java.util.Properties;

public class BinaceConfig {

    public static String BASE_URL;
    public static String API_KEY;
    public static String SECRET_KEY;

    static {
        Properties config = ConfigReader.loadConfig("config.properties");

        if (config != null) {
            BASE_URL = config.getProperty("binace.baseurl");
            API_KEY = config.getProperty("binace.apikey");
            SECRET_KEY = config.getProperty("binace.secretkey");
        }
    }
}
