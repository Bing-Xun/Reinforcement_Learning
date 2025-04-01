package binace.socket;

import binace.config.BinaceConfig;
import binace.socket.event.AggTradeEvent;
import binace.util.Utils;
import com.binance.connector.client.WebSocketStreamClient;
import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;

public class WebSocket {

    public static void main(String[] args) {
        // 创建公共流客户端（不需要API密钥）
        WebSocketStreamClient streamClient = new WebSocketStreamClientImpl(BinaceConfig.BASE_URL);

        // 订阅聚合交易流
        streamClient.aggTradeStream("btcusdt", (event) -> {
//            System.out.println("交易事件: " + event);

            // 將 JSON 字串轉換為 AggTradeEvent 物件
            try {
                AggTradeEvent aggTradeEvent = Utils.mapper.readValue(event, AggTradeEvent.class);
                System.out.println("交易事件 aggTradeEvent: " + aggTradeEvent);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        // 保持连接
        try { Thread.sleep(Long.MAX_VALUE); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }
}
