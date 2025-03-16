package feature;

import java.util.ArrayList;
import java.util.List;

public class StochasticOscillatorTrading {

    /**
     * 計算隨機指標 (KD).
     *
     * @param high     最高價數組.
     * @param low      最低價數組.
     * @param close    收盤價數組.
     * @param kPeriod  %K 的週期 (通常為 9、14 等).
     * @param dPeriod  %D 的週期 (通常為 3).
     * @param slowing  慢速隨機指標的 smoothing 週期 (通常為 3，如果為 1 則是快速隨機指標).
     * @return 包含 %K 線和 %D 線的兩個數組的列表.
     */
    public static List<double[]> calculateStochasticOscillator(double[] high, double[] low, double[] close, int kPeriod, int dPeriod, int slowing) {
        if (high == null || low == null || close == null || high.length != low.length || high.length != close.length ||
            kPeriod <= 0 || dPeriod <= 0 || slowing <= 0 || kPeriod > high.length) {
            return null; // 或拋出異常
        }

        int n = close.length;
        double[] kValues = new double[n];
        double[] dValues = new double[n];

        // 計算 %K
        for (int i = kPeriod - 1; i < n; i++) {
            double lowestLow = low[i];
            double highestHigh = high[i];
            for (int j = i - kPeriod + 1; j <= i; j++) {
                lowestLow = Math.min(lowestLow, low[j]);
                highestHigh = Math.max(highestHigh, high[j]);
            }

            if (highestHigh - lowestLow == 0) {
                kValues[i] = 100; // 避免除以零
            } else {
                kValues[i] = ((close[i] - lowestLow) / (highestHigh - lowestLow)) * 100;
            }
        }

        // 計算 Slow %K (對 %K 進行 smoothing)
        // 這裡為了保持和kPeriod長度一樣, 前面會補0
        double[] slowK = calculateSMA(kValues, slowing, kPeriod -1 );


        // 計算 %D (對 Slow %K 進行 smoothing)
        for (int i = kPeriod - 1 + slowing -1; i < n; i++)
        {
            double sum = 0;
            for (int j = i - dPeriod + 1; j <= i; j++) {
                sum += slowK[j];
            }
            dValues[i] = sum / dPeriod;
        }


        List<double[]> result = new ArrayList<>();
        result.add(slowK);
        result.add(dValues);
        return result;
    }

    /**
     *  計算SMA
     * @param data 原始數據
     * @param period sma週期
     * @param paddingLength  前面補0長度
     * @return
     */
    public static double[] calculateSMA(double[] data, int period, int paddingLength) {
        if (data == null || data.length < period + paddingLength || period <= 0) {
            return null; // 或拋出異常，根據您的需求
        }
        //前面補0的sma
        double[] sma = new double[data.length];

        //從可以算出sma的地方開始算
        for (int i = paddingLength + period - 1; i < sma.length; i++) {
            double sum = 0;
            for (int j = 0; j < period; j++) {
                sum += data[i - period + 1 + j];
            }
            sma[i] = sum / period;
        }
        return sma;
    }


    /**
     * 根據 KD 指標的金叉/死叉、超買/超賣和簡化版背離策略生成交易信號.
     *
     * @param high     最高價數組.
     * @param low      最低價數組.
     * @param close    收盤價數組.
     * @param kPeriod  %K 的週期.
     * @param dPeriod  %D 的週期.
     * @param slowing  慢速隨機指標的 smoothing 週期.
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] high, double[] low, double[] close, int kPeriod, int dPeriod, int slowing) {
        List<double[]> kd = calculateStochasticOscillator(high, low, close, kPeriod, dPeriod, slowing);
        if (kd == null) {
            return null;
        }

        double[] kValues = kd.get(0); // Slow %K
        double[] dValues = kd.get(1); // %D

        List<String> signals = new ArrayList<>();
        //前面沒信號
        for(int i = 0; i< kPeriod + slowing -1; ++i)
            signals.add("HOLD");

        // 用於簡化版背離判斷的變數
        double prevPriceLow = close[0];
        double prevKLow = kValues[0];
        double prevPriceHigh = close[0];
        double prevKHigh = kValues[0];

        for (int i = kPeriod + slowing -1 ; i < close.length; i++) {
            String signal = "HOLD";

            // 金叉 (K 線上穿 D 線) 且在超賣區
            if (kValues[i - 1] < dValues[i - 1] && kValues[i] > dValues[i] && kValues[i] < 20 && dValues[i] < 20) {
                signal = "BUY";
            }
            // 死叉 (K 線下穿 D 線) 且在超買區
            else if (kValues[i - 1] > dValues[i - 1] && kValues[i] < dValues[i] && kValues[i] > 80 && dValues[i] > 80) {
                signal = "SELL";
            }

            // 簡化版底背離 (價格創新低，K 線不創新低)
            if (close[i] < prevPriceLow && kValues[i] > prevKLow) {
                //signal = "BUY"; // 可選
            }
            // 簡化版頂背離 (價格創新高，K 線不創新高)
            if (close[i] > prevPriceHigh && kValues[i] < prevKHigh) {
                //signal = "SELL"; // 可選
            }

            // 更新前低/前高
            if (close[i] < prevPriceLow) {
                prevPriceLow = close[i];
                prevKLow = kValues[i];
            }
            if (close[i] > prevPriceHigh) {
                prevPriceHigh = close[i];
                prevKHigh = kValues[i];
            }

            signals.add(signal);
        }

        return signals;
    }

    public static void main(String[] args) {
        // 示例數據
        double[] highPrices = {25, 26, 27, 26, 28, 29, 30, 29, 31, 32, 31, 30, 29, 28, 27, 28, 29, 30, 31, 32};
        double[] lowPrices = {23, 24, 25, 24, 26, 27, 28, 27, 29, 30, 29, 28, 27, 26, 25, 26, 27, 28, 29, 30};
        double[] closePrices = {24, 25.5, 26.5, 25.5, 27, 28.5, 29.5, 28.5, 30, 31.5, 30.5, 29.5, 28.5, 27.5, 26.5, 27.5, 28.5, 29.5, 30.5, 31.5};

        int kPeriod = 9;
        int dPeriod = 3;
        int slowing = 3; // 1 for fast stochastic, > 1 for slow stochastic

        // 計算 KD
        List<double[]> kd = calculateStochasticOscillator(highPrices, lowPrices, closePrices, kPeriod, dPeriod, slowing);
        double[] kValues = kd.get(0);
        double[] dValues = kd.get(1);

        // 生成交易信號
        List<String> signals = generateSignals(highPrices, lowPrices, closePrices, kPeriod, dPeriod, slowing);

        // 打印結果
        System.out.println("High\tLow\tClose\t%K\t%D\tSignal");
        for (int i = 0; i < closePrices.length; i++) {
            String kStr, dStr;
            if(i < kPeriod + slowing - 2)
            {
                kStr = "N/A";
                dStr = "N/A";
            }
            else
            {
                kStr = String.format("%.2f", kValues[i]);
                dStr = String.format("%.2f", dValues[i]);
            }
            System.out.println(String.format("%.2f", highPrices[i]) + "\t" +
                String.format("%.2f", lowPrices[i]) + "\t" +
                String.format("%.2f", closePrices[i]) + "\t" +
                kStr + "\t" +
                dStr + "\t" +
                signals.get(i));
        }
    }
}
