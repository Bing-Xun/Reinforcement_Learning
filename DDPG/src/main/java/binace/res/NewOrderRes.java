package binace.res;

import binace.enums.OrderSide;
import binace.enums.OrderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略 JSON 中的未知字段
@Data
public class NewOrderRes {

    /**
     * 交易对（如 "BTCUSDT"）。
     */
    private String symbol;

    /**
     * 币安系统生成的唯一订单 ID。
     */
    private Long orderId;

    /**
     * 普通订单为 -1
     */
    private Long orderListId;

    /**
     * 用户自定义的订单 ID（若未提供，币安会自动生成）。
     */
    private String clientOrderId;

    /**
     * 委托价格（例如 "30000.00000000" USDT）。
     */
    private String price;

    /**
     * 原始订单数量（例如 "0.00100000" BTC）。
     */
    private String origQty;

    /**
     * 已成交数量（新订单通常为 "0.00000000"）。
     */
    private String executedQty;

    /**
     * 累计成交金额（以报价货币计算，例如 "0.00000000" USDT）。
     */
    private String cummulativeQuoteQty;

    /**
     * 订单状态：
     * - "NEW"（新订单）
     * - "FILLED"（完全成交）
     * - "PARTIALLY_FILLED"（部分成交）。
     */
    private String status;

    /**
     * 订单有效期："GTC"（默认）、"IOC"、"FOK"。
     */
    private String timeInForce;

    /**
     * 订单类型："LIMIT"、"MARKET"、"STOP_LOSS" 等。
     */
    private String type;

    /**
     * 买卖方向："BUY" 或 "SELL"。
     */
    private String side;

    /**
     * 订单创建时间（Unix 时间戳，毫秒）。
     */
    private Long transactTime;

    /**
     * 订单开始工作的时间（通常与 transactTime 相同）。
     */
    private Long workingTime;

    /**
     * 成交明细列表（新订单为空数组 []）。
     */
    private List<Fill> fills; // 成交明细（需定义 Fill 类）

    /**
     * 自成交保护模式："EXPIRE_MAKER"、"CANCEL_BOTH" 等。
     */
    private String selfTradePreventionMode;

    /**
     *     用户下单时指定的 报价货币总金额（例如 "100.00000000" USDT）。
     *     仅对 MARKET 订单有效，限价单（LIMIT）通常为 "0.00000000"。
     */
    private String origQuoteOrderQty;

    @Data
    public static class Fill {
        /**
         * 成交价格
         */
        private String price;      // 成交价格

        /**
         * 成交数量
         */
        private String qty;       // 成交数量

        /**
         * 手续费
         */
        private String commission; // 手续费

        /**
         * 手续费币种
         */
        private String commissionAsset; // 手续费币种
    }
}
