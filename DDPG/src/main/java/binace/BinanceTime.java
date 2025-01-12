package binace;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.Map;

public class BinanceTime {

    public static void main(String[] args) {
        getTime();
    }

    public static Long getTime() {
        Long l = null;

        try {
            // 使用幣安的API來獲取伺服器時間
            String apiUrl = "https://testnet.binance.vision/api/v3/time";

            // 創建 HTTP 請求
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(apiUrl);
            CloseableHttpResponse response = httpClient.execute(httpGet);

            // 解析響應
            String responseString = EntityUtils.toString(response.getEntity());
            System.out.println("Server Time Response: " + responseString);

            Map<String, Object> map = new ObjectMapper().readValue(responseString, Map.class);
            l = (Long) map.get("serverTime");


            // 打印伺服器時間
            response.close();
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return l;
    }
}

