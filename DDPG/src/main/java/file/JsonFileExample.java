package file;


import binace.vo.QuoteVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import db.config.MyBatisConfig;
import db.entity.QuoteEntity;
import db.mapper.QuoteMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonFileExample {

    public static void main(String[] args) {
//        int quoteCnt = 107733;
//        List<QuoteEntity> list = getQuoteList("quote_btc_1m", quoteCnt);
//        String filePath = "DDPG/data1m.json";

//        int quoteCnt = 38694;
//        List<QuoteEntity> list = getQuoteList("quote_btc_3m", quoteCnt);
//        String filePath = "DDPG/data3m.json";

//        int quoteCnt = 3105;
//        List<QuoteEntity> list = getQuoteList("quote_btc_1h", quoteCnt);
//        String filePath = "DDPG/data1h.json";

//        int quoteCnt = 112437;
//        List<QuoteEntity> list = getQuoteList("quote_btc_1m", quoteCnt);
//        String filePath = "DDPG/data_btc_1m.json";

//        int quoteCnt = 39709;
//        List<QuoteEntity> list = getQuoteList("quote_paxg_1m", quoteCnt);
//        String filePath = "DDPG/data_paxg_1m.json";

//        int quoteCnt = 37731;
//        List<QuoteEntity> list = getQuoteList("quote_xrp_1m", quoteCnt);
//        String filePath = "DDPG/data_xrp_1m.json";

        int quoteCnt = 37731;
        List<QuoteEntity> list = getQuoteList("quote_link_1m", quoteCnt);
        String filePath = "DDPG/data_link_1m.json";

        // 創建 ObjectMapper 實例
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 將 List<QuoteVO> 寫入檔案
            objectMapper.writeValue(new File(filePath), list);
            System.out.println("資料已成功寫入檔案: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) {
//        String filePath = "path/to/your/file.json";
//
//        // 創建 ObjectMapper 實例
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        try {
//            // 從檔案中讀取 List<QuoteVO>
//            List<QuoteVO> list = objectMapper.readValue(new File(filePath), new TypeReference<>() {});
//            System.out.println("資料已成功從檔案讀取: " + filePath);
//            // 處理讀取到的資料
//            for (QuoteVO quote : list) {
//                System.out.println(quote);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static List<QuoteEntity> getQuoteList(String tableName, int limit) {
        List<QuoteEntity> quotes = new ArrayList<>();

        // 使用 Java 配置创建 SqlSessionFactory
        MyBatisConfig myBatisConfig = new MyBatisConfig();
        SqlSessionFactory sqlSessionFactory = myBatisConfig.sqlSessionFactory();

        try (SqlSession session = sqlSessionFactory.openSession()) {
            QuoteMapper quoteMapper = session.getMapper(QuoteMapper.class);

            quotes = quoteMapper.getQuotes(tableName, limit);
//            for (QuoteEntity quote : quotes) {
//                System.out.println(quote);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return quotes;
    }
}