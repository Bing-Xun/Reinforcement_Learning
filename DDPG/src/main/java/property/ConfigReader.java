package property;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    public static void main(String[] args) {
        Properties config = loadConfig("config.properties");

        if (config != null) {
            String serverPort = config.getProperty("server.port");
            String databaseUrl = config.getProperty("database.url");
            String databaseUsername = config.getProperty("database.username");

            System.out.println("Server Port: " + serverPort);
            System.out.println("Database URL: " + databaseUrl);
            System.out.println("Database Username: " + databaseUsername);
        }
    }

    public static Properties loadConfig(String filename) {
        Properties properties = new Properties();
        File configFile = new File(filename);
        FileInputStream fis = null;

        try {
            // 檢查檔案是否存在
            if (configFile.exists() && configFile.isFile()) {
                fis = new FileInputStream(configFile);
                properties.load(fis);
                System.out.println("成功載入設定檔: " + configFile.getAbsolutePath());
                return properties;
            } else {
                System.err.println("設定檔不存在或不是檔案: " + configFile.getAbsolutePath());
                return null;
            }
        } catch (IOException e) {
            System.err.println("讀取設定檔時發生錯誤: " + e.getMessage());
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
