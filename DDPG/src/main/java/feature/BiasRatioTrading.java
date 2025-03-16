package feature;

import java.util.ArrayList;
import java.util.List;

public class BiasRatioTrading {

    /**
     * 計算簡單移動平均線 (SMA).
     */
    public static double[] calculateSMA(double[] data, int period) {
        // (與之前程式碼中的 calculateSMA 函數相同，此處省略)
        if (data == null || data.length < period || period <= 0) {
            return null; // 或拋出異常，根據您的需求
        }

        double[] sma = new double[data.length - period + 1];
        for (int i = 0; i < sma.length; i++) {
            double sum = 0;
            for (int j = 0; j < period; j++) {
                sum += data[i + j];
            }
            sma[i] = sum / period;
        }
        return sma;
    }

    /**
     * 計算乖離率 (BIAS).
     *
     * @param close  收盤價數組.
     * @param period 週期 (例如，6, 12, 24).
     * @return BIAS 數組.
     */
    public static double[] calculateBias(double[] close, int period) {
        if (close == null || period <= 0 || close.length < period) {
            return null; // 或拋出異常
        }

        double[] sma = calculateSMA(close, period);
        if(sma == null) return null;

        double[] bias = new double[close.length - period + 1];
        for (int i = 0; i < bias.length; i++) {
            bias[i] = ((close[i + period - 1] - sma[i]) / sma[i]) * 100;
        }

        return bias;
    }

    /**
     * 根據乖離率 (BIAS) 策略生成交易信號.
     *  這裡假設負乖離過大時買入，正乖離過大時賣出。
     *  具體的閾值需要根據市場情況和回測結果確定。
     *  並且加入"反轉"的判斷
     *
     * @param close       收盤價數組.
     * @param period      週期.
     * @param buyThreshold 買入閾值 (負值，例如 -5 表示 -5%).
     * @param sellThreshold 賣出閾值 (正值，例如 5 表示 5%).
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] close, int period, double buyThreshold, double sellThreshold) {
        double[] bias = calculateBias(close, period);
        if (bias == null) {
            return null;
        }

        List<String> signals = new ArrayList<>();
        for(int i = 0; i < period - 1; ++i)
            signals.add("HOLD");
        for (int i = 1; i < bias.length; i++) { // 從 1 開始，因為要比較前一個 BIAS
            // 負乖離過大且向上反轉 (買入)
            if (bias[i - 1] < buyThreshold && bias[i] >= buyThreshold) {
                signals.add("BUY");
            }
            // 正乖離過大且向下反轉 (賣出)
            else if (bias[i - 1] > sellThreshold && bias[i] <= sellThreshold) {
                signals.add("SELL");
            }
            // 其他情況，保持
            else {
                signals.add("HOLD");
            }
        }

        return signals;
    }

    public static void main(String[] args) {
        // 示例數據 (收盤價)
        double[] closePrices = {
            25, 26, 27, 26, 28, 29, 30, 29, 31, 32,
            31, 30, 29, 28, 27, 28, 29, 30, 31, 32,
            33, 34, 35, 34, 33, 32, 31, 30, 29, 28
        };
        int period = 12;
        double buyThreshold = -4; // 根據市場情況和回測調整
        double sellThreshold = 4;  // 根據市場情況和回測調整

        // 計算 BIAS
        double[] bias = calculateBias(closePrices, period);

        // 生成 BIAS 交易信號
        List<String> signals = generateSignals(closePrices, period, buyThreshold, sellThreshold);

        // 打印結果
        System.out.println("Price\tBIAS\t\tSignal");

        for (int i = 0; i < closePrices.length; i++) {

            String biasStr;

            if(i< period - 1)
                biasStr = "N/A";
            else
                biasStr = String.format("%.2f", bias[i-(period-1)]);

            System.out.println(String.format("%.2f", closePrices[i]) + "\t" +
                biasStr + "\t\t" +
                signals.get(Math.min(i,signals.size()-1)));
        }
    }
}