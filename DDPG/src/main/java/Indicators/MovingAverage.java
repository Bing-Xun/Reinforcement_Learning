package Indicators;

public class MovingAverage {
    public static double calculateMA(double[] prices, int period) {
        if (prices.length < period) {
            throw new IllegalArgumentException("Insufficient data for the given period.");
        }
        double sum = 0.0;
        for (int i = prices.length - period; i < prices.length; i++) {
            sum += prices[i];
        }
        return sum / period;
    }

    /**
     * prices 是每日的價格數據，長度為 7。
     * period 設定為 3，表示計算最近 3 天的移動平均。
     * calculateMA 方法將計算最近 3 天（prices 陣列的最後 3 個數據）的平均值。
     * @param args
     */
    public static void main(String[] args) {
        double[] prices = {10, 12, 11, 14, 13, 15, 14};
        int period = 3;
        System.out.println("Moving Average (MA): " + calculateMA(prices, period));
    }
}

