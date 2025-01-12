package binace;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BinanceTestnetOrder {

    private static final String API_URL = "https://testnet.binance.vision/api/v3/order"; // 測試網現貨訂單 API
    private static final String API_KEY = "";
    private static final String SECRET_KEY = "";

    public static void main(String[] args) {
        try {
            // 設置訂單參數
            String symbol = "BTCUSDT";
            String side = "BUY";
            String type = "MARKET";
            double quantity = 0.003;

            // 獲取時間戳
//            long timestamp = System.currentTimeMillis();
//            long timestamp = 1735975130459L;
            long timestamp = BinanceTime.getTime();

            // 創建請求參數
            Map<String, String> params = new HashMap<>();
            params.put("symbol", symbol);
            params.put("side", side);
            params.put("type", type);
            params.put("quantity", String.valueOf(quantity));
            params.put("timestamp", String.valueOf(timestamp));

            // 打印原始請求參數
            System.out.println("Original Params: " + params);

            // 生成查詢字串並排序
            String queryString = buildQueryString(params);
            System.out.println("Query String before Signature: " + queryString);

            // 生成簽名
            String signature = generateSignature(queryString, SECRET_KEY);
            System.out.println("Generated Signature: " + signature);

            // 添加簽名到參數中
            params.put("signature", signature);

            // 創建完整的請求 URL
            String url = API_URL + "?" + buildQueryString(params);

            // 發送請求
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            httpPost.setHeader("X-MBX-APIKEY", API_KEY);

            // 發送請求並獲取回應
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity());

            // 打印回應結果
            System.out.println("Order Response: " + responseString);

            // 關閉資源
            response.close();
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 按字母順序排列參數並生成查詢字串
    private static String buildQueryString(Map<String, String> params) {
        Set<Map.Entry<String, String>> entrySet = params.entrySet();
        String queryString = entrySet.stream()
                .sorted(Map.Entry.comparingByKey())  // 按字母順序排序
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        return queryString;
    }

    // 使用HMAC-SHA256算法生成簽名
    public static String generateSignature(String data, String apiSecret) throws Exception {
        // 创建 HMACSHA256 签名
        SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);

        // 生成签名并转换为十六进制字符串
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);  // 返回十六进制的签名
    }

    // 将字节数组转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();  // 转为大写
    }
}
