package binace.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略 JSON 中的未知字段
@Data
public class CancelOpenOrderRes {
    private String symbol;
    private String origClientOrderId;
    private Long orderId;
    private Long orderListId;
    private String clientOrderId;
    private Long transactTime;
    private String price;
    private String origQty;
    private String executedQty;
    private String cummulativeQuoteQty;
    private String status;
    private String timeInForce;
    private String type;
    private String side;
    private String selfTradePreventionMode;
    private BigDecimal origQuoteOrderQty;
}
