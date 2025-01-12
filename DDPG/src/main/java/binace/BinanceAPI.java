package binace;

import binace.vo.QuoteVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BinanceAPI {

    /**
     *
     * [0]	开盘时间 (Open Time)	当前 K 线的开盘时间，时间戳（毫秒）。	Unix Timestamp (ms)
     * [1]	开盘价 (Open Price)	当前 K 线的开盘价格。	计价货币
     * [2]	最高价 (High Price)	当前 K 线的最高价格。	计价货币
     * [3]	最低价 (Low Price)	当前 K 线的最低价格。	计价货币
     * [4]	收盘价 (Close Price)	当前 K 线的收盘价格。	计价货币
     * [5]	成交量 (Volume)	当前 K 线的成交量。	基础货币
     * [6]	收盘时间 (Close Time)	当前 K 线的收盘时间，时间戳（毫秒）。	Unix Timestamp (ms)
     * [7]	成交额 (Quote Asset Volume)	当前 K 线的成交额（以计价货币计算）。	计价货币
     * [8]	成交笔数 (Number of Trades)	当前 K 线内的成交笔数。	笔数
     * [9]	主动买入成交量 (Taker Buy Base Asset Volume)	当前 K 线内主动买入的成交量（基础货币）。	基础货币
     * [10]	主动买入成交额 (Taker Buy Quote Asset Volume)	当前 K 线内主动买入的成交额（计价货币）。	计价货币
     * [11]	忽略字段 (Ignore)	保留字段，无实际意义，值固定为 "0"。	-
     *
     * [
     *     [
     *         1499040000000,      // [0] 开盘时间 (Timestamp)
     *         "0.01634790",       // [1] 开盘价 (Open Price)
     *         "0.80000000",       // [2] 最高价 (High Price)
     *         "0.01575800",       // [3] 最低价 (Low Price)
     *         "0.01577100",       // [4] 收盘价 (Close Price)
     *         "148976.11427815",  // [5] 成交量 (Volume)
     *         1499644799999,      // [6] 收盘时间 (Timestamp)
     *         "2434.19055334",    // [7] 成交额 (Quote Asset Volume)
     *         308,                // [8] 成交笔数 (Number of Trades)
     *         "1756.87402397",    // [9] 主动买入的成交量 (Taker Buy Base Asset Volume)
     *         "28.46694368",      // [10] 主动买入的成交额 (Taker Buy Quote Asset Volume)
     *         "0"                 // [11] 忽略 (Ignore, Reserved Field)
     *     ],
     *     ...
     * ]
     *
     * 开盘时间：1698211200000 → Unix 时间戳 (ms)。
     * 开盘价：100.00 → 开盘时的价格。
     * 最高价：110.00 → 该周期的最高价。
     * 最低价：95.00 → 该周期的最低价。
     * 收盘价：105.00 → 收盘时的价格。
     * 成交量：500.00 → 成交了 500 个基础货币（如 BTC）。
     * 收盘时间：1698214800000 → Unix 时间戳 (ms)。
     * 成交额：52500.00 → 该周期内总成交额，以计价货币（如 USDT）。
     * 成交笔数：100 → 该周期内发生了 100 笔交易。
     * 主动买入成交量：300.00 → 主动买入成交了 300 个基础货币。
     * 主动买入成交额：31500.00 → 主动买入成交总额为 31,500 计价货币。
     * 忽略字段："0" → 固定值。
     *
     * 1m	1 分钟	60 秒
     * 3m	3 分钟	180 秒
     * 5m	5 分钟	300 秒
     * 15m	15 分钟	900 秒
     * 30m	30 分钟	1800 秒
     * 1h	1 小时	3600 秒
     * 2h	2 小时	7200 秒
     * 4h	4 小时	14400 秒
     * 6h	6 小时	21600 秒
     * 8h	8 小时	28800 秒
     * 12h	12 小时	43200 秒
     * 1d	1 天	86400 秒
     * 3d	3 天	259200 秒
     * 1w	1 周	604800 秒
     * 1M	1 月	2592000 秒
     *
     * // 获取最近 1000 条 15 分钟的 K 线数据
     * String urlString = "https://api.binance.com/api/v1/klines?symbol=BTCUSDT&interval=15m&limit=1000";
     *
     * // 获取指定时间范围内的 K 线数据
     * String urlWithTime = "https://api.binance.com/api/v1/klines?symbol=BTCUSDT&interval=1h&startTime=1698211200000&endTime=1698214800000";
     * @param args
     */
    public static void main(String[] args) {
//        String symbol = "BTCUSDT";  // 交易对，例如 BTC/USDT
//        String interval = "1h";     // K线时间间隔，1小时
        List<QuoteVO> list = getQuote("BTCUSDT", "1m");
        System.out.println(list);
    }

    public static List<QuoteVO> getQuote(String symbol, String interval) {
        String urlString = "https://api.binance.com/api/v1/klines?symbol=" + symbol + "&interval=" + interval + "&limit=1000";
        List<QuoteVO> quoteVOList = new ArrayList<>();

        try {
            // 连接到币安API
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 读取响应数据
            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            // 使用 Jackson 解析响应数据
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.toString());

            // 遍历 K线数据并输出所有字段

            for (JsonNode kline : jsonResponse) {
                long openTime = kline.get(0).asLong();          // 开盘时间 (Unix 时间戳)
                String open = kline.get(1).asText();            // 开盘价
                String high = kline.get(2).asText();            // 最高价
                String low = kline.get(3).asText();             // 最低价
                String close = kline.get(4).asText();           // 收盘价
                String volume = kline.get(5).asText();          // 成交量
                long closeTime = kline.get(6).asLong();         // 收盘时间 (Unix 时间戳)
                String quoteAssetVolume = kline.get(7).asText();// 成交额 (计价货币)
                long trades = kline.get(8).asLong();            // 成交笔数
                String takerBuyBaseAssetVolume = kline.get(9).asText(); // 主动买入成交量 (基础货币)
                String takerBuyQuoteAssetVolume = kline.get(10).asText(); // 主动买入成交额 (计价货币)

//                System.out.println("Open Time: " + openTime + ", Open: " + open + ", High: " + high +
//                        ", Low: " + low + ", Close: " + close + ", Volume: " + volume + ", Close Time: " + closeTime +
//                        ", Quote Asset Volume: " + quoteAssetVolume + ", Trades: " + trades +
//                        ", Taker Buy Base Asset Volume: " + takerBuyBaseAssetVolume +
//                        ", Taker Buy Quote Asset Volume: " + takerBuyQuoteAssetVolume);

                QuoteVO quoteVO = QuoteVO.builder()
                        .openTime(openTime)
                        .open(new BigDecimal(open))
                        .high(new BigDecimal(high))
                        .low(new BigDecimal(low))
                        .close(new BigDecimal(close))
                        .volume(new BigDecimal(volume))
                        .closeTime(closeTime)
                        .quoteAssetVolume(new BigDecimal(quoteAssetVolume))
                        .trades(trades)
                        .takerBuyBaseAssetVolume(new BigDecimal(takerBuyBaseAssetVolume))
                        .takerBuyQuoteAssetVolume(new BigDecimal(takerBuyQuoteAssetVolume))
                        .build();

                quoteVOList.add(quoteVO);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return quoteVOList;
    }
}
