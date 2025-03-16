package feature;

import java.util.ArrayList;
import java.util.List;

public class BollingerBandsTrading {

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
     * 計算標準差.
     *
     * @param data     輸入數據.
     * @param period   週期.
     * @param sma      對應週期的 SMA (用於優化，避免重複計算).
     * @return 標準差數組.
     */
    public static double[] calculateStdDev(double[] data, int period, double[] sma) {
        if (data == null || period <= 0 || sma == null || data.length - period + 1 != sma.length) {
            return null; // 或拋出異常
        }

        double[] stdDev = new double[sma.length];
        for (int i = 0; i < sma.length; i++) {
            double sumOfSquares = 0;
            for (int j = 0; j < period; j++) {
                double diff = data[i + j] - sma[i];
                sumOfSquares += diff * diff;
            }
            stdDev[i] = Math.sqrt(sumOfSquares / period);
        }
        return stdDev;
    }


    /**
     * 計算布林通道.
     *
     * @param data   輸入的數據數組 (例如，收盤價).
     * @param period 週期 (通常為 20).
     * @param k      標準差倍數 (通常為 2).
     * @return 包含上軌、中軌和下軌的三個數組的列表.
     */
    public static List<double[]> calculateBollingerBands(double[] data, int period, double k) {
        if (data == null || period <= 0) {
            return null; // 或拋出異常
        }

        double[] sma = calculateSMA(data, period);
        if (sma == null) {
            return null; // SMA 計算失敗
        }
        double[] stdDev = calculateStdDev(data, period, sma);


        double[] upperBand = new double[sma.length];
        double[] middleBand = new double[sma.length];
        double[] lowerBand = new double[sma.length];

        for (int i = 0; i < sma.length; i++) {
            middleBand[i] = sma[i];
            upperBand[i] = middleBand[i] + k * stdDev[i];
            lowerBand[i] = middleBand[i] - k * stdDev[i];
        }

        List<double[]> result = new ArrayList<>();
        result.add(upperBand);
        result.add(middleBand);
        result.add(lowerBand);
        return result;
    }

    /**
     * 根據布林通道策略生成交易信號.
     *
     * @param data    輸入的數據數組 (例如，收盤價).
     * @param period  週期.
     * @param k       標準差倍數.
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] data, int period, double k) {
        List<double[]> bands = calculateBollingerBands(data, period, k);
        if (bands == null) {
            return null;
        }

        double[] upperBand = bands.get(0);
        double[] middleBand = bands.get(1);
        double[] lowerBand = bands.get(2);

        List<String> signals = new ArrayList<>();

        // 因為布林通道需要 period - 1 個數據點才能計算，所以信號列表前面填充 "HOLD"
        for (int i = 0; i < period - 1; i++) {
            signals.add("HOLD");
        }

        for (int i = period - 1; i < data.length; i++) {
            String signal = "HOLD";

            // 價格從下軌附近反彈向上 (或突破下軌)
            // 這裡用一個簡化的條件：價格低於下軌，但上一期價格更高
            if (data[i] < lowerBand[i - (period - 1)] ) {
                signal = "BUY";
            }
            // 價格突破中軌向上
            else if (data[i - 1] < middleBand[i - (period - 1)] && data[i] > middleBand[i - (period - 1)]) {
                signal = "BUY";
            }
            // 價格從上軌附近回落向下 (或跌破上軌)
            // 這裡用一個簡化的條件：價格高於上軌，但上一期價格更低
            else if (data[i] > upperBand[i - (period - 1)] ) {
                signal = "SELL";
            }
            // 價格跌破中軌向下
            else if (data[i - 1] > middleBand[i - (period - 1)] && data[i] < middleBand[i - (period - 1)]) {
                signal = "SELL";
            }

            signals.add(signal);
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
        int period = 20;
        double k = 2.0; // 標準差倍數

        // 計算布林通道
        List<double[]> bands = calculateBollingerBands(prices, period, k);
        double[] upperBand = bands.get(0);
        double[] middleBand = bands.get(1);
        double[] lowerBand = bands.get(2);

        // 生成交易信號
        List<String> signals = generateSignals(prices, period, k);

        // 打印結果
        System.out.println("Price\tUpper\tMiddle\tLower\tSignal");
        for (int i = 0; i < prices.length; i++) {

            String upperStr, middleStr, lowerStr;
            if(i< period -1)
            {
                upperStr = "N/A";
                middleStr = "N/A";
                lowerStr = "N/A";
            }
            else
            {
                upperStr = String.format("%.2f", upperBand[i-(period - 1)]);
                middleStr = String.format("%.2f", middleBand[i-(period - 1)]);
                lowerStr = String.format("%.2f", lowerBand[i-(period - 1)]);
            }

            System.out.println(String.format("%.2f", prices[i]) + "\t" +
                upperStr + "\t" +
                middleStr + "\t" +
                lowerStr + "\t" +
                signals.get(i));
        }
    }
}