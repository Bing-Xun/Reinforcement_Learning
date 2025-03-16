package feature;

import java.util.ArrayList;
import java.util.List;

public class MACDTrading {

    /**
     * 計算指數移動平均線 (EMA).
     */
    public static double[] calculateEMA(double[] data, int period) {
        // (與之前程式碼中的 calculateEMA 函數相同，此處省略)
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
     * 計算 MACD.
     *
     * @param data      輸入的數據數組 (例如，收盤價).
     * @param fastPeriod  快線週期.
     * @param slowPeriod  慢線週期.
     * @param signalPeriod 信號線週期.
     * @return  包含 MACD 線、信號線和柱狀圖的三個數組的列表。
     */
    public static List<double[]> calculateMACD(double[] data, int fastPeriod, int slowPeriod, int signalPeriod) {
        if (data == null || fastPeriod <= 0 || slowPeriod <= 0 || signalPeriod <= 0 || fastPeriod >= slowPeriod) {
            return null; // 或拋出異常
        }

        double[] fastEMA = calculateEMA(data, fastPeriod);
        double[] slowEMA = calculateEMA(data, slowPeriod);

        double[] macdLine = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            //快線跟慢線都要有值才能算, 因此前面幾個會是0
            if(i>=slowPeriod-1)
                macdLine[i] = fastEMA[i] - slowEMA[i];
            else
                macdLine[i] = 0;
        }

        double[] signalLine = calculateEMA(macdLine, signalPeriod);

        double[] histogram = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            //信號線也要有值
            if(i>=slowPeriod-1 && i>= signalPeriod -1)
                histogram[i] = macdLine[i] - signalLine[i];
            else
                histogram[i] = 0;
        }

        List<double[]> result = new ArrayList<>();
        result.add(macdLine);
        result.add(signalLine);
        result.add(histogram);
        return result;
    }

    /**
     * 根據 MACD 金叉/死叉、柱狀圖和簡化版背離策略生成交易信號.
     *
     * @param data       輸入的數據數組 (例如，收盤價).
     * @param fastPeriod   快線週期.
     * @param slowPeriod   慢線週期.
     * @param signalPeriod 信號線週期.
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] data, int fastPeriod, int slowPeriod, int signalPeriod) {
        List<double[]> macdData = calculateMACD(data, fastPeriod, slowPeriod, signalPeriod);
        if (macdData == null) {
            return null;
        }

        double[] macdLine = macdData.get(0);
        double[] signalLine = macdData.get(1);
        double[] histogram = macdData.get(2);

        List<String> signals = new ArrayList<>();
        signals.add("HOLD"); // 初始信號

        // 用於簡化版背離判斷的變數
        double prevPriceLow = data[0];
        double prevMACDLow = macdLine[0];
        double prevPriceHigh = data[0];
        double prevMACDHigh = macdLine[0];

        //從慢線跟信號線都有值開始
        int startIndex = Math.max(slowPeriod, signalPeriod) ;

        for (int i = startIndex; i < data.length; i++) {
            String signal = "HOLD"; // 默認信號

            // 金叉：MACD 線上穿信號線
            if (macdLine[i - 1] < signalLine[i - 1] && macdLine[i] > signalLine[i]) {
                signal = "BUY";
            }
            // 死叉：MACD 線下穿信號線
            else if (macdLine[i - 1] > signalLine[i - 1] && macdLine[i] < signalLine[i]) {
                signal = "SELL";
            }
            // 柱狀圖從負值區上穿 0 軸
            else if (histogram[i - 1] < 0 && histogram[i] > 0) {
                signal = "BUY"; // 也可以考慮只在金叉時買入
            }
            // 柱狀圖從正值區下穿 0 軸
            else if (histogram[i - 1] > 0 && histogram[i] < 0) {
                signal = "SELL"; // 也可以考慮只在死叉時賣出
            }

            // 簡化版底背離 (價格創新低，MACD 不創新低)
            // 注意：這只是一個非常簡化的版本，真正的背離判斷需要更複雜的邏輯
            if (data[i] < prevPriceLow && macdLine[i] > prevMACDLow) {
                //signal = "BUY"; // 這裡可以選擇是否覆蓋之前的信號
                // 背離通常需要和其他信號一起使用
            }

            // 簡化版頂背離 (價格創新高，MACD 不創新高)
            if (data[i] > prevPriceHigh && macdLine[i] < prevMACDHigh) {
                //signal = "SELL";
                //背離通常需要和其他信號一起使用
            }

            // 更新前低/前高 (簡化版)
            if (data[i] < prevPriceLow) {
                prevPriceLow = data[i];
                prevMACDLow = macdLine[i];
            }
            if (data[i] > prevPriceHigh) {
                prevPriceHigh = data[i];
                prevMACDHigh = macdLine[i];
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
        int fastPeriod = 12;
        int slowPeriod = 26;
        int signalPeriod = 9;

        // 計算 MACD
        List<double[]> macdData = calculateMACD(prices, fastPeriod, slowPeriod, signalPeriod);
        double[] macdLine = macdData.get(0);
        double[] signalLine = macdData.get(1);
        double[] histogram = macdData.get(2);

        // 生成交易信號
        List<String> signals = generateSignals(prices, fastPeriod, slowPeriod, signalPeriod);

        // 打印結果
        System.out.println("Price\tMACD\tSignal\tHistogram\tSignal");
        for (int i = 0; i < prices.length; i++) {
            //為了對齊
            String macdStr, signalStr, histogramStr;

            if(i<slowPeriod-1)
                macdStr = "N/A";
            else
                macdStr = String.format("%.2f", macdLine[i]);
            if(i<signalPeriod -1 || i<slowPeriod-1)
                signalStr = "N/A";
            else
                signalStr = String.format("%.2f", signalLine[i]);

            if(i<signalPeriod -1 || i<slowPeriod-1)
                histogramStr = "N/A";
            else
                histogramStr = String.format("%.2f", histogram[i]);


            System.out.println(String.format("%.2f", prices[i]) + "\t" +
                macdStr + "\t" +
                signalStr+ "\t" +
                histogramStr + "\t" +
                signals.get(Math.min(i,signals.size()-1))); // 避免 signals 越界
        }
    }
}