package binace.req;

import binace.enums.OrderSide;
import binace.enums.OrderType;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Builder
@Data
public class NewOrderReq {

    @NonNull
    private String symbol; // BTCUSDT

    @NonNull
    private OrderSide side; // BUY

    @NonNull
    private OrderType type; // LIMIT

    @Builder.Default
    private String timeInForce = "GTC";

    @NonNull
    private BigDecimal quantity; // 0.001

    @NonNull
    private BigDecimal price; // 30000

    public Map<String, Object> getMap() {
        Map<String, Object> orderParams = new LinkedHashMap<>();
        orderParams.put("symbol", symbol);
        orderParams.put("side", side.name());
        orderParams.put("type", type.name());
        orderParams.put("timeInForce", timeInForce);
        orderParams.put("quantity", quantity.stripTrailingZeros().toPlainString());
        orderParams.put("price", price.stripTrailingZeros().toPlainString());

        return orderParams;
    }
}
