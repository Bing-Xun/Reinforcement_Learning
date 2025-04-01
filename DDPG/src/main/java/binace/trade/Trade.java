package binace.trade;

import binace.enums.OrderSide;
import binace.enums.OrderType;
import binace.req.NewOrderReq;
import binace.res.*;
import binace.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Trade {


    /**
     * 获取账户信息
     */
    public static AccountRes account() throws JsonProcessingException {
        Map<String, Object> parameters = new LinkedHashMap<>();
        String accountInfo = BinaceClient.client.createTrade().account(parameters);
//        System.out.println("账户信息: " + accountInfo);
        return Utils.mapper.readValue(accountInfo, AccountRes.class);
    }

    /**
     * 创建订单
     * @param req
     */
    public static NewOrderRes newOrder(NewOrderReq req) throws JsonProcessingException {
        String orderResponse = BinaceClient.client.createTrade().newOrder(req.getMap());
//        System.out.println("订单响应: " + orderResponse);
        return Utils.mapper.readValue(orderResponse, NewOrderRes.class);
    }

    /**
     * 查询订单（需要orderId）
     * @param symbol BTCUSDT
     * @param orderId 12346983
     */
    public static GetOrderRes getOrder(String symbol, String orderId) throws JsonProcessingException {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("symbol", symbol);
        queryParams.put("orderId", orderId);

        String orderStatus = BinaceClient.client.createTrade().getOrder(queryParams);
//        System.out.println("订单状态: " + orderStatus);
        return Utils.mapper.readValue(orderStatus, GetOrderRes.class);
    }

    /**
     * 查詢指定交易對的所有掛單(未成交訂單)
     * @param symbol 交易對，如 BTCUSDT
     */
    public static List<GetOpenOrders> getOpenOrders(String symbol) throws JsonProcessingException {
        // 查询特定交易对的挂单
        // 1. 查询BTCUSDT挂单
        Map<String, Object> queryParams = new LinkedHashMap<>();
        if(StringUtils.isNotEmpty(symbol)) {
            queryParams.put("symbol", symbol); // BTCUSDT
        }
        String openOrders = BinaceClient.client.createTrade().getOpenOrders(queryParams);
//        System.out.println(openOrders);
        List<GetOpenOrders> orders = Utils.mapper.readValue(openOrders, new TypeReference<>(){});
        return orders;
    }

    /**
     * 使用Map参数取消订单
     * @param params 必须包含:
     *               - symbol (String): 交易对，如"BTCUSDT"
     *               可选包含:
     *               - orderId (Long): 系统订单ID
     *               - origClientOrderId (String): 客户端自定义订单ID
     *               - newClientOrderId (String): 新自定义ID（可选）
     *               - recvWindow (Long): 接收窗口（毫秒）
     * @return 取消成功的订单信息（原始响应）
     * @throws IllegalArgumentException 参数缺失时抛出
     */
    public static CancelOrderRes cancelOrder(String symbol, Long orderId) throws JsonProcessingException {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("symbol", symbol);
        queryParams.put("orderId", orderId);
        String cancelOrder = BinaceClient.client.createTrade().cancelOrder(queryParams);
//        System.out.println(openOrders);
        return Utils.mapper.readValue(cancelOrder, CancelOrderRes.class);
    }

    /**
     * 取消挂单（Map参数版）
     * @param parameters 必须包含:
     *                  - symbol (String): 交易对 (如 BTCUSDT)
     *                  可选参数 (至少需要一个):
     *                  - orderId (Long): 系统订单ID
     *                  - origClientOrderId (String): 自定义订单ID
     *                  其他可选参数:
     *                  - newClientOrderId (String): 新自定义ID
     *                  - recvWindow (Long): 请求有效期(毫秒)
     * @return 取消成功的订单原始响应(JSON格式)
     * @throws IllegalArgumentException 参数缺失时抛出
     */
    public static List<CancelOpenOrderRes> cancelOpenOrders(String symbol) throws JsonProcessingException {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("symbol", symbol);
        String cancelOrders = BinaceClient.client.createTrade().cancelOpenOrders(queryParams);
        System.out.println(cancelOrders);
        return Utils.mapper.readValue(cancelOrders, new TypeReference<>(){});
    }

    public static void main(String[] args) throws Exception {
        // 获取账户信息
//        AccountRes accountRes = Trade.account();
//        System.out.println(accountRes);

        // 创建订单
//        NewOrderReq newOrderReq = NewOrderReq.builder()
//            .symbol("BTCUSDT")
//            .side(OrderSide.BUY)
//            .type(OrderType.LIMIT)
//            .quantity(new BigDecimal("0.001"))
//            .price(new BigDecimal("30000.00"))
//            .build();
//        Trade.newOrder(newOrderReq);
//        NewOrderRes newOrderRes = Trade.newOrder(newOrderReq);
//        System.out.println(newOrderRes);

        // 查询订单
//        GetOrderRes getOrderRes = Trade.getOrder("BTCUSDT", "11935967");
//        System.out.println(getOrderRes);

        // 查詢未成交訂單
//        List<GetOpenOrders> list = getOpenOrders("BTCUSDT");
//        List<GetOpenOrders> list = getOpenOrders(null);
//        System.out.println(list);

        // 取消掛單
//        cancelOrder("BTCUSDT", 12738294L);

//        List<CancelOpenOrderRes> cancelOpenOrderResList = cancelOpenOrders("BTCUSDT");
//        System.out.println(cancelOpenOrderResList);
    }
}
