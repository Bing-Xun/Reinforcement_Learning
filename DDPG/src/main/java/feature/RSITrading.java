package feature;

import java.util.ArrayList;
import java.util.List;

public class RSITrading {

    /**
     * 計算 RSI (Relative Strength Index).
     *
     * @param data   輸入的數據數組 (例如，收盤價).
     * @param period RSI 的週期 (通常為 14).
     * @return RSI 數組.
     */
    public static double[] calculateRSI(double[] data, int period) {
        if (data == null || data.length <= period || period <= 0) {
            return null; // 或拋出異常
        }

        double[] rsi = new double[data.length];
        double[] gain = new double[data.length];
        double[] loss = new double[data.length];

        // 計算初始的平均漲幅和平均跌幅 (使用 SMA)
        double sumGain = 0;
        double sumLoss = 0;
        for (int i = 1; i <= period; i++) {
            double change = data[i] - data[i - 1];
            if (change > 0) {
                sumGain += change;
            } else {
                sumLoss += Math.abs(change);
            }
        }
        gain[period] = sumGain / period;
        loss[period] = sumLoss / period;
        rsi[period] = 100.0 - (100.0 / (1.0 + (gain[period] / loss[period])));


        // 計算後續的 RSI 值 (使用 Wilder's smoothing method)
        for (int i = period + 1; i < data.length; i++) {
            double change = data[i] - data[i - 1];
            if (change > 0) {
                gain[i] = (gain[i - 1] * (period - 1) + change) / period;
                loss[i] = (loss[i - 1] * (period - 1)) / period;
            } else {
                gain[i] = (gain[i - 1] * (period - 1)) / period;
                loss[i] = (loss[i - 1] * (period - 1) + Math.abs(change)) / period;
            }

            double rs = (loss[i] == 0) ? 100 : gain[i] / loss[i]; // 避免除以零
            rsi[i] = 100.0 - (100.0 / (1.0 + rs));
        }

        return rsi;
    }



    /**
     * 根據 RSI 超買/超賣和簡化版背離策略生成交易信號.
     *
     * @param data   輸入的數據數組 (例如，收盤價).
     * @param period RSI 的週期.
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] data, int period) {
        double[] rsi = calculateRSI(data, period);
        if (rsi == null) {
            return null;
        }

        List<String> signals = new ArrayList<>();
        //前幾個沒信號
        for(int i = 0; i< period; ++i)
            signals.add("HOLD");

        // 用於簡化版背離判斷的變數
        // （注意：這只是一個非常簡化的版本，真正的背離判斷更複雜）
        double prevPriceLow = data[0];
        double prevRSILow = rsi[0];
        double prevPriceHigh = data[0];
        double prevRSIHigh = rsi[0];

        for (int i = period; i < data.length; i++) {
            String signal = "HOLD";

            // 超賣區域向上回升 (買入)
            if (rsi[i - 1] < 30 && rsi[i] >= 30) {
                signal = "BUY";
            }
            // 超買區域向下回落 (賣出)
            else if (rsi[i - 1] > 70 && rsi[i] <= 70) {
                signal = "SELL";
            }

            // 簡化版底背離 (價格創新低，RSI 不創新低)
            if (data[i] < prevPriceLow && rsi[i] > prevRSILow) {
                //signal = "BUY"; // 可以選擇是否覆蓋
            }

            // 簡化版頂背離 (價格創新高，RSI 不創新高)
            if (data[i] > prevPriceHigh && rsi[i] < prevRSIHigh) {
                //signal = "SELL"; // 可以選擇是否覆蓋
            }

            // 更新前低/前高 (簡化版)
            if (data[i] < prevPriceLow) {
                prevPriceLow = data[i];
                prevRSILow = rsi[i];
            }
            if (data[i] > prevPriceHigh) {
                prevPriceHigh = data[i];
                prevRSIHigh = rsi[i];
            }

            signals.add(signal);
        }

        return signals;
    }

    public static void main(String[] args) {
        // 示例數據 (收盤價)
        double[] prices = {
            45.1, 46.2, 44.8, 45.5, 47.0, 46.5, 48.2, 49.0, 48.5, 49.8,
            50.2, 49.5, 48.8, 47.2, 46.0, 47.5, 48.8, 49.2, 50.5, 51.2,
            50.8, 52.0, 53.5, 53.0, 52.5, 51.8, 52.2, 53.0, 52.8, 53.5
        };
        int period = 14;

        // 計算 RSI
        double[] rsi = calculateRSI(prices, period);

        // 生成交易信號
        List<String> signals = generateSignals(prices, period);

        // 打印結果
        System.out.println("Price\tRSI\tSignal");
        for (int i = 0; i < prices.length; i++) {
            String rsiStr = (i < period) ? "N/A" : String.format("%.2f", rsi[i]);
            System.out.println(String.format("%.2f", prices[i]) + "\t" +
                rsiStr + "\t" +
                signals.get(i));
        }
    }
}