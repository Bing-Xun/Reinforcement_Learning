package binace.socket.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AggTradeEvent {
    @JsonProperty("e")
    private String eventType; // 事件類型

    @JsonProperty("E")
    private long eventTime;   // 事件時間

    @JsonProperty("s")
    private String symbol;    // 交易對符號

    @JsonProperty("a")
    private long aggTradeId;  // 聚合交易ID

    @JsonProperty("p")
    private String price;     // 價格

    @JsonProperty("q")
    private String quantity;  // 數量

    @JsonProperty("f")
    private long firstTradeId; // 第一筆交易ID

    @JsonProperty("l")
    private long lastTradeId;  // 最後一筆交易ID

    @JsonProperty("T")
    private long tradeTime;   // 交易時間

    @JsonProperty("m")
    private boolean buyerMarketMaker; // 是否為買方主動成交

    @JsonProperty("M")
    private boolean bestMatch; // 是否為最優匹配
}
