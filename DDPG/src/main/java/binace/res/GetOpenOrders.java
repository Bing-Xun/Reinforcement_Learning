package binace.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略 JSON 中的未知字段
@Data
public class GetOpenOrders {

    /**
     * 交易對 (e.g. BTCUSDT)
     */
    private String symbol;                // 交易對 (e.g. BTCUSDT)

    /**
     * 系統訂單ID
     */
    private Long orderId;                 // 系統訂單ID

    /**
     * OCO訂單組ID (普通單為-1)
     */
    private Long orderListId;             // OCO訂單組ID (普通單為-1)

    /**
     * 客戶端自訂訂單ID
     */
    private String clientOrderId;         // 客戶端自訂訂單ID

    /**
     * 委託價格 (使用BigDecimal避免精度問題)
     */
    private BigDecimal price;             // 委託價格 (使用BigDecimal避免精度問題)

    /**
     * 原始委託數量
     */
    private BigDecimal origQty;           // 原始委託數量

    /**
     *  已成交數量
     */
    private BigDecimal executedQty;       // 已成交數量

    /**
     * 累計成交金額
     */
    private BigDecimal cummulativeQuoteQty; // 累計成交金額

    /**
     * 訂單狀態
     * 幣安訂單狀態 (Order Status) 完整列表
     * 狀態值	說明	是否活躍狀態
     * NEW	新創建的未成交訂單	✔️
     * PARTIALLY_FILLED	部分成交的訂單	✔️
     * FILLED	已完全成交	✖️
     * CANCELED	已取消	✖️
     * PENDING_CANCEL	取消中 (現貨交易用不到)	✖️
     * REJECTED	被系統拒絕	✖️
     * EXPIRED	已過期 (時間條件觸發)	✖️
     *
     * [NEW] → [PARTIALLY_FILLED] → [FILLED]
     *   │         │
     *   ↓         ↓
     * [CANCELED] [REJECTED]
     *   │
     *   ↓
     * [EXPIRED]
     */
    private String status;

    /**
     * 有效時間 (GTC/IOC/FOK)
     */
    private String timeInForce;           // 有效時間 (GTC/IOC/FOK)

    /**
     * 訂單類型
     *
     * 現貨交易 (Spot)
     * 類型值	說明	是否需要價格	適用方向
     * LIMIT	限價單	✔️	BUY/SELL
     * MARKET	市價單	✖️	BUY/SELL
     * STOP_LOSS	止損市價單	✖️	SELL
     * STOP_LOSS_LIMIT	止損限價單	✔️	SELL
     * TAKE_PROFIT	止盈市價單	✖️	SELL
     * TAKE_PROFIT_LIMIT	止盈限價單	✔️	SELL
     * LIMIT_MAKER	只做Maker單	✔️	BUY/SELL
     *
     * tateDiagram-v2
     *     [*] --> LIMIT: 創建
     *     LIMIT --> FILLED: 成交
     *     LIMIT --> CANCELED: 取消
     *     LIMIT --> PARTIALLY_FILLED: 部分成交
     *
     *     [*] --> MARKET: 創建
     *     MARKET --> FILLED: 立即成交
     *
     *     [*] --> STOP_LOSS_LIMIT: 創建
     *     STOP_LOSS_LIMIT --> LIMIT: 觸發條件
     */
    private String type;

    /**
     * 買賣方向 (BUY/SELL)
     */
    private String side;                  // 買賣方向 (BUY/SELL)

    /**
     * 觸發價 (止損/止盈單用)
     */
    private BigDecimal stopPrice;         // 觸發價 (止損/止盈單用)

    /**
     * 冰山訂單顯示數量
     */
    private BigDecimal icebergQty;        // 冰山訂單顯示數量

    /**
     * 訂單創建時間(毫秒時間戳)
     */
    private Long time;                    // 訂單創建時間(毫秒時間戳)

    /**
     * 最後更新時間(毫秒時間戳)
     */
    private Long updateTime;              // 最後更新時間(毫秒時間戳)

    /**
     * 是否在撮合中
     */
    private Boolean isWorking;            // 是否在撮合中

    /**
     * 開始工作時間
     */
    private Long workingTime;             // 開始工作時間

    /**
     * 原始報價金額(市價單用)
     */
    private BigDecimal origQuoteOrderQty; // 原始報價金額(市價單用)

    /**
     * 自成交保護模式
     */
    private String selfTradePreventionMode; // 自成交保護模式

    /**
     * 判斷是否活躍訂單 (NEW或部分成交)
     */
    public boolean isActive() {
        return "NEW".equals(status) || "PARTIALLY_FILLED".equals(status);
    }

    /**
     * 計算未成交數量
     */
    public BigDecimal getRemainingQty() {
        return origQty.subtract(executedQty);
    }
}
