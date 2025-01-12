package binace;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jfree.data.json.impl.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;

public class BinanceRestAPIExample {
    // 替換為你的 API Key 和 Secret
    private static final String API_KEY = "";
    private static final String SECRET_KEY = "";
//    private static final String BASE_URL = "https://testnet.binance.vision/api";
//    private static final String API_URL = "https://testnet.binance.vision/api/v3/account";
//    private static final String API_URL = "https://testnet.binance.vision/api/v3/positionRisk";
    private static final String API_URL = "https://testnet.binance.vision//fapi/v1/order";


    public static void main(String[] args) {
        account("https://testnet.binance.vision/api/v3/account");
    }

    public static void account(String api_uri) {
        try {
            // 獲取當前時間戳
            long timestamp = System.currentTimeMillis();

            // 設置請求參數
            Map<String, String> params = new HashMap<>();
            params.put("timestamp", String.valueOf(timestamp));

            // 生成簽名
            String queryString = buildQueryString(params);
            String signature = generateSignature(queryString);
            params.put("signature", signature);

            // 創建 URL
            String url = api_uri + "?" + buildQueryString(params);

            // 發送 GET 請求
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpGet.setHeader("X-MBX-APIKEY", API_KEY);

            // 發送請求並獲取回應
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseString = EntityUtils.toString(response.getEntity());

            // 打印回應結果
            System.out.println("Account Info: " + responseString);

            // 關閉資源
            response.close();
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成簽名
    private static String generateSignature(String queryString) throws UnsupportedEncodingException {
        return HmacUtils.hmacSha256Hex(SECRET_KEY, queryString);
    }

    // 構建 URL 查詢字符串
    private static String buildQueryString(Map<String, String> params) {
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }
        return queryString.toString();
    }
}
