package binace.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略 JSON 中的未知字段
@Data
public class CancelOrderRes {

    /**
     * 交易对 "BTCUSDT"
     */
    private String symbol;

    /**
     * 系统订单ID
     */
    private Long orderId;

    /**
     * 原始自定义订单ID
     */
    private String origClientOrderId;

    /**
     * 新生成的客户端ID
     */
    private String clientOrderId;

    private Long orderListId; // 普通订单为-1

    /**
     * 委托价格
     */
    private BigDecimal price;

    /**
     * 原始数量
     */
    private BigDecimal origQty;

    /**
     * 已成交数量
     */
    private BigDecimal executedQty;

    /**
     * 累计成交金额（以报价货币计算，例如 "0.00000000" USDT）。
     */
    private BigDecimal cummulativeQuoteQty;

    /**
     * 订单状态（已取消）
     */
    private String status;

    /**
     * 有效時間：
     * - "GTC"（成交為止）
     * - "IOC"（立即成交或取消）
     * - "FOK"（全部成交或取消）。
     */
    private String timeInForce;

    /**
     * 订单类型
     */
    private String type;

    /**
     * 买卖方向
     */
    private String side;

    /**
     * 操作时间戳（毫秒）
     */
    private Long transactTime;

    /**
     * 自成交保護模式：
     * - "EXPIRE_MAKER"（使 Maker 訂單過期）
     * - "CANCEL_BOTH"（取消雙方）
     * - "CANCEL_TAKER"（取消 Taker 訂單）。
     */
    private String selfTradePreventionMode;

    /**
     * 原始報價金額(市價單用)
     */
    private BigDecimal origQuoteOrderQty; // 原始報價金額(市價單用)

    /**
     * 获取可读的取消时间
     */
    public LocalDateTime getCancelTime() {
        return Instant.ofEpochMilli(transactTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }

    /**
     * 判断是否完全取消
     */
    public boolean isFullyCancelled() {
        return "CANCELED".equals(status) &&
            executedQty.compareTo(BigDecimal.ZERO) == 0;
    }
}
