package Indicators;

public class RSI {
    // 計算 RSI (Relative Strength Index) 根據價格數據
    public static double calculateRSIFromPrices(double[] prices, int period) {
        // 計算每日漲跌幅
        double[] changes = new double[prices.length - 1];
        for (int i = 1; i < prices.length; i++) {
            changes[i - 1] = prices[i] - prices[i - 1]; // 計算每日漲跌幅
        }

        return calculateRSI(changes, period); // 使用原來的 RSI 計算方法
    }

    // 計算 RSI (Relative Strength Index) 根據漲跌幅數據
    public static double calculateRSI(double[] changes, int period) {
        double avgGain = 0.0;
        double avgLoss = 0.0;

        // 計算最初的平均漲幅和平均跌幅
        for (int i = 0; i < period; i++) {
            if (changes[i] > 0) {
                avgGain += changes[i];
            } else {
                avgLoss -= changes[i]; // 因為是負數，所以加上
            }
        }
        avgGain /= period;
        avgLoss /= period;

        // 計算 RS 和 RSI
        double rs = avgGain / avgLoss;
        double rsi = 100 - (100 / (1 + rs));

        // 計算後續周期的 RSI（使用平滑公式）
        for (int i = period; i < changes.length; i++) {
            if (changes[i] > 0) {
                avgGain = (avgGain * (period - 1) + changes[i]) / period;
                avgLoss = (avgLoss * (period - 1)) / period;
            } else {
                avgLoss = (avgLoss * (period - 1) - changes[i]) / period;
                avgGain = (avgGain * (period - 1)) / period;
            }
            rs = avgGain / avgLoss;
            rsi = 100 - (100 / (1 + rs));
        }

        return rsi;
    }

    public static void main(String[] args) {
        double[] prices = {10, 12, 11, 14, 13, 15, 14}; // 價格數據
        int period = 5; // 設定 RSI 計算的周期為 5 天
        System.out.println("RSI: " + calculateRSIFromPrices(prices, period));
    }
}
