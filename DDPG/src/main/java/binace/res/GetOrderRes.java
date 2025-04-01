package binace.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@JsonIgnoreProperties(ignoreUnknown = true) // 忽略 JSON 中的未知字段
@Data
public class GetOrderRes {

    /////////////////////////////// 1. 訂單基礎資訊
    /**
     * 交易對（例如 "BTCUSDT"）。
     */
    private String symbol;

    /**
     * 幣安系統生成的唯一訂單 ID。
     */
    private Long orderId;

    /**
     * 用戶自訂的訂單 ID（如未提供，幣安會自動生成）。
     */
    private String clientOrderId;

    /**
     * 用於 OCO 訂單（一鍵平倉），普通訂單為 -1。
     */
    private Long orderListId;


    /////////////////////////////// 2. 訂單狀態與數量
    /**
     * 訂單狀態：
     * - "NEW"（新訂單）
     * - "FILLED"（完全成交）
     * - "PARTIALLY_FILLED"（部分成交）
     * - "CANCELED"（已取消）
     * - "REJECTED"（被拒絕）
     *
     * if ("FILLED".equals(order.getStatus())) {
     *     System.out.println("訂單已完全成交");
     * }
     */
    private String status;

    /**
     * 原始訂單數量（例如 "0.00100000" BTC）。
     */
    private String origQty;

    /**
     * 已成交數量。
     *
     *
     * 計算成交均價：
     * BigDecimal executedQty = new BigDecimal(order.getExecutedQty());
     * BigDecimal quoteQty = new BigDecimal(order.getCummulativeQuoteQty());
     * BigDecimal avgPrice = quoteQty.divide(executedQty, 8, RoundingMode.HALF_UP);
     */
    private String executedQty;

    /**
     * 累計成交金額（以報價貨幣計算，例如 "30.00000000" USDT）。
     */
    private String cummulativeQuoteQty;

    /**
     * 訂單創建時間（Unix 時間戳，毫秒）。
     *
     *
     * 處理時間戳：
     * Instant createTime = Instant.ofEpochMilli(order.getTime());
     */
    private Long time;

    /**
     * 訂單最後更新時間（Unix 時間戳，毫秒）。
     */
    private Long updateTime;


    /////////////////////////////// 3. 訂單類型與條件
    /**
     * 訂單類型：
     * - "LIMIT"（限價單）
     * - "MARKET"（市價單）
     * - "STOP_LOSS"（止損單）
     * - "TAKE_PROFIT"（止盈單）等。
     */
    private String type;

    /**
     * 買賣方向：
     * - "BUY"（買入）
     * - "SELL"（賣出）。
     */
    private String side;

    /**
     * 委託價格（限價單專用，例如 "30000.00000000" USDT）。
     */
    private String price;

    /**
     * 觸發價（止損/止盈單專用，普通訂單為 "0.00000000"）。
     */
    private String stopPrice;

    /**
     * 有效時間：
     * - "GTC"（成交為止）
     * - "IOC"（立即成交或取消）
     * - "FOK"（全部成交或取消）。
     */
    private String timeInForce;


    /////////////////////////////// 4. 高級選項
    /**
     * 冰山訂單的顯示數量（普通訂單為 "0.00000000"）。
     */
    private String icebergQty;

    /**
     * 訂單是否仍在撮合中（例如掛單中）。
     */
    private Boolean isWorking;

    /**
     * 訂單開始工作的時間（Unix 時間戳，毫秒）。
     */
    private Long workingTime;

    /**
     * 原始報價訂單金額（市價買單專用，例如 "100.00000000" USDT）。
     */
    private String origQuoteOrderQty;

    /**
     * 自成交保護模式：
     * - "EXPIRE_MAKER"（使 Maker 訂單過期）
     * - "CANCEL_BOTH"（取消雙方）
     * - "CANCEL_TAKER"（取消 Taker 訂單）。
     */
    private String selfTradePreventionMode;
}
