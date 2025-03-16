package feature;

import java.util.ArrayList;
import java.util.List;

public class EMATrading {

    /**
     * 計算指數移動平均線 (EMA).  從頭計算
     *
     * @param data   輸入的數據數組 (例如，收盤價).
     * @param period 移動平均的週期.
     * @return EMA 數組，長度與輸入數據相同.
     */
    public static double[] calculateEMA(double[] data, int period) {
        if (data == null || period <= 0) {
            return null;
        }

        double[] ema = new double[data.length];
        double alpha = 2.0 / (period + 1);

        // 第一個 EMA 等於第一個數據點
        ema[0] = data[0];

        // 從 1 開始計算後續的 EMA
        for (int i = 1; i < data.length; i++) {
            ema[i] = (data[i] * alpha) + (ema[i - 1] * (1 - alpha));
        }

        return ema;
    }

    /**
     * 根據 EMA 金叉/死叉策略生成交易信號.
     *
     * @param data     輸入的數據數組 (例如，收盤價).
     * @param shortPeriod 短期 EMA 週期.
     * @param longPeriod  長期 EMA 週期.
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").  列表長度與 data 相同
     */
    public static List<String> generateSignals(double[] data, int shortPeriod, int longPeriod) {
        if (data == null || shortPeriod <= 0 || longPeriod <= 0 || shortPeriod >= longPeriod) {
            return null; // 或拋出異常
        }

        double[] shortEMA = calculateEMA(data, shortPeriod);
        double[] longEMA = calculateEMA(data, longPeriod);
        List<String> signals = new ArrayList<>();

        // 初始信號為 HOLD
        signals.add("HOLD");

        // 從 longPeriod 開始，因為我們需要足夠的 EMA 數據
        for (int i = 1; i < data.length; i++) {
            if (shortEMA[i - 1] < longEMA[i - 1] && shortEMA[i] > longEMA[i]) {
                signals.add("BUY"); // 金叉
            } else if (shortEMA[i - 1] > longEMA[i - 1] && shortEMA[i] < longEMA[i]) {
                signals.add("SELL"); // 死叉
            } else {
                signals.add("HOLD"); // 保持
            }
        }

        return signals;
    }

    public static void main(String[] args) {
        // 示例數據 (收盤價)
        double[] prices = {
            10, 12, 15, 14, 16, 18, 20, 19, 22, 25,
            24, 26, 28, 27, 29, 31, 30, 28, 26, 24,
            25, 23, 21, 22, 20, 19, 17, 18, 16, 15
        };
        int shortPeriod = 5;
        int longPeriod = 12;

        // 計算 EMA
        double[] ema5 = calculateEMA(prices, shortPeriod);
        double[] ema12 = calculateEMA(prices, longPeriod);

        // 生成交易信號
        List<String> signals = generateSignals(prices, shortPeriod, longPeriod);

        // 打印結果
        System.out.println("Price\tEMA5\tEMA12\tSignal");
        for (int i = 0; i < prices.length; i++) {
            //為了輸出對齊, 只好這麼做
            String ema5Str;
            String ema12Str;
            if(i<shortPeriod-1)
                ema5Str = "N/A";
            else
                ema5Str = String.format("%.2f", ema5[i]);
            if(i<longPeriod-1)
                ema12Str = "N/A";
            else
                ema12Str = String.format("%.2f", ema12[i]);

            System.out.println(String.format("%.2f", prices[i]) + "\t" +
                ema5Str + "\t" +
                ema12Str + "\t" +
                signals.get(i));
        }
    }
}