package binace;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class BinanceHistoricalKlines {

    private static final String API_URL = "https://testnet.binance.vision/api/v3/klines";

    public static void main(String[] args) {
        try {
            // 設置請求參數
            String symbol = "BTCUSDT";
            String interval = "1m"; // 1 分鐘 K 線
            long startTime = System.currentTimeMillis() - 60 * 60 * 1000; // 一小時前
            long endTime = System.currentTimeMillis();

            String urlStr = API_URL + "?symbol=" + symbol + "&interval=" + interval +
                    "&startTime=" + startTime + "&endTime=" + endTime + "&limit=1000";

            // 發送 HTTP GET 請求
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            // 讀取回應
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.lines().collect(Collectors.joining());
            in.close();

            // 打印返回的 K 線數據
            System.out.println("Klines Data: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
