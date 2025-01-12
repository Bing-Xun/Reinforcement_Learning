package binace;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class BinanceTestnetWebSocket {

    public static void main(String[] args) {
        try {
            // 定義 WebSocket URL
            String streamName = "btcusdt@trade"; // 訂閱的 stream 名稱，例如 @trade 表示訂閱交易數據
            String url = "wss://testnet.binance.vision/ws/" + streamName;

            // 創建 WebSocket 客戶端
            WebSocketClient client = new WebSocketClient(new URI(url)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("WebSocket Connected");
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("Received: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket Closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket Error: " + ex.getMessage());
                }
            };

            // 開始連接
            client.connect();

            // 等待 WebSocket 連接
            while (!client.isOpen()) {
                Thread.sleep(100);
            }

            System.out.println("WebSocket is ready!");

            // 保持運行，接收數據
            Thread.sleep(60 * 1000); // 保持連接 60 秒
            client.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
