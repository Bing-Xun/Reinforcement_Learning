package Indicators;

public class MACD {
    public static double calculateEMA(double[] prices, int period) {
        double multiplier = 2.0 / (period + 1);
        double ema = prices[0];
        for (int i = 1; i < prices.length; i++) {
            ema = (prices[i] - ema) * multiplier + ema;
        }
        return ema;
    }


    /**
     * calculateEMA 方法：該方法計算給定價格數據的 EMA 值。這裡是從價格數組的第一個元素開始計算，每個新的 EMA 都依賴於上一個 EMA 值。
     * fastEMA 和 slowEMA 分別代表快線和慢線，對應於 12 天和 26 天的指數移動平均。
     * macd 是快線和慢線之間的差異，這就是 MACD 值。
     * @param args
     */
    public static void main(String[] args) {
        double[] prices = {10, 12, 11, 14, 13, 15, 14};
        double fastEMA = calculateEMA(prices, 12); // 快線
        double slowEMA = calculateEMA(prices, 26); // 慢線
        double macd = fastEMA - slowEMA;
        System.out.println("MACD: " + macd);
    }
}

