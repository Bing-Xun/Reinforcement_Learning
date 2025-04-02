package db.config;

import db.mapper.QuoteMapper;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.yaml.snakeyaml.Yaml;
import property.ConfigReader;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MyBatisConfig {

    private static PooledDataSource dataSource;

    static {
//        // resource 加載
//        try {
//            // 使用 Resources 从类路径加载 mybatis-config.yaml 配置文件
//            InputStream inputStream = Resources.getResourceAsStream("mybatis-config.yaml");
//            Yaml yaml = new Yaml();
//            Map<String, Object> config = yaml.load(inputStream);
//            Map<String, String> databaseConfig = (Map<String, String>) config.get("database");
//
//            // 配置数据源
//            PooledDataSource dataSource = new PooledDataSource();
//            dataSource.setDriver(databaseConfig.get("driver"));
//            dataSource.setUrl(databaseConfig.get("url"));
//            dataSource.setUsername(databaseConfig.get("username"));
//            dataSource.setPassword(String.valueOf(databaseConfig.get("password")));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            Properties config = ConfigReader.loadConfig("config.properties");

            if (config != null) {
                PooledDataSource dataSource = new PooledDataSource();
                dataSource.setDriver((String) config.get("database.driver"));
                dataSource.setUrl((String) config.get("database.url"));
                dataSource.setUsername((String) config.get("database.username"));
                dataSource.setPassword(String.valueOf(config.get("database.password")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SqlSessionFactory sqlSessionFactory() {
        try {
            // 创建 MyBatis 配置
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
            configuration.setEnvironment(new org.apache.ibatis.mapping.Environment("development", new org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory(), dataSource));
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setCacheEnabled(true);

            // 手动注册 Mapper 接口
            configuration.addMapper(QuoteMapper.class);

            return new SqlSessionFactoryBuilder().build(configuration);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


//    // 使用 Java 配置创建 SqlSessionFactory
//    MyBatisConfig myBatisConfig = new MyBatisConfig();
//    SqlSessionFactory sqlSessionFactory = myBatisConfig.sqlSessionFactory();
//
//                try (SqlSession session = sqlSessionFactory.openSession()) {
//        QuoteMapper quoteMapper = session.getMapper(QuoteMapper.class);
//
//        for(String interval : intervalList) {
//            List<QuoteVO> list = BinanceAPI.getQuote("BTCUSDT", interval);
//            for(QuoteVO quoteVO : list) {
//                quoteMapper.insertQuote(String.format("quote_btc_%s", interval), quoteVO);
//            }
//            session.commit();
//        }
//
//    } catch (Exception e) {
//        e.printStackTrace();
//    }
}
