package binace.config;

import property.ConfigReader;

import java.util.Properties;

public class BinaceConfig {

    public static String BASE_Url;
    public static String API_KEY;
    public static String SECRET_KEY;

    static {
        Properties config = ConfigReader.loadConfig("binace.properties");

        if (config != null) {
            BASE_Url = config.getProperty("BASE_Url");
            API_KEY = config.getProperty("API_KEY");
            SECRET_KEY = config.getProperty("SECRET_KEY");
        }
    }
}
