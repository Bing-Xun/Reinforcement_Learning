package feature;

import java.util.ArrayList;
import java.util.List;

public class DonchianChannelTrading {

    /**
     * 計算唐奇安通道.
     *
     * @param high   最高價數組.
     * @param low    最低價數組.
     * @param period 週期 (通常為 20).
     * @return 包含上通道、中通道和下通道的三個數組的列表.
     */
    public static List<double[]> calculateDonchianChannel(double[] high, double[] low, int period) {
        if (high == null || low == null || high.length != low.length || period <= 0) {
            return null; // 或拋出異常
        }

        int n = high.length;
        double[] upperChannel = new double[n - period + 1];
        double[] middleChannel = new double[n - period + 1];
        double[] lowerChannel = new double[n - period + 1];

        for (int i = 0; i < n - period + 1; i++) {
            double highestHigh = high[i];
            double lowestLow = low[i];
            for (int j = 1; j < period; j++) {
                highestHigh = Math.max(highestHigh, high[i + j]);
                lowestLow = Math.min(lowestLow, low[i + j]);
            }
            upperChannel[i] = highestHigh;
            middleChannel[i] = (highestHigh + lowestLow) / 2.0;
            lowerChannel[i] = lowestLow;
        }

        List<double[]> result = new ArrayList<>();
        result.add(upperChannel);
        result.add(middleChannel);
        result.add(lowerChannel);
        return result;
    }

    /**
     * 根據唐奇安通道突破策略生成交易信號.
     *
     * @param high   最高價數組.
     * @param low    最低價數組.
     * @param close  收盤價數組
     * @param period 週期.
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] high, double[] low, double[] close, int period) {
        List<double[]> channel = calculateDonchianChannel(high, low, period);
        if (channel == null) {
            return null;
        }

        double[] upperChannel = channel.get(0);
        double[] lowerChannel = channel.get(2);

        List<String> signals = new ArrayList<>();
        // 前 period - 1 個數據點沒有通道值，所以沒有信號
        for (int i = 0; i < period - 1; i++) {
            signals.add("HOLD");
        }

        for (int i = period - 1; i < close.length; i++) {
            // 突破上通道 (買入)
            if (close[i] > upperChannel[i - (period - 1)]) {
                signals.add("BUY");
            }
            // 跌破下通道 (賣出)
            else if (close[i] < lowerChannel[i - (period - 1)]) {
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
        // 示例數據
        double[] highPrices = {12, 13, 15, 13, 15, 17, 19, 21, 20, 22, 21, 20, 19, 18, 17, 19, 20, 22, 24, 23, 25, 24, 22, 23, 21, 20, 18, 19, 17, 16};
        double[] lowPrices = {10, 11, 12, 11, 13, 15, 16, 18, 18, 19, 19, 18, 16, 15, 15, 17, 18, 19, 21, 20, 22, 21, 20, 21, 19, 18, 16, 17, 15, 14};
        double[] closePrices = {11, 12.5, 14, 13.5, 14.5, 16.5, 18, 20, 19.5, 21, 20.5, 19.5, 18.5, 17.5, 18, 18.5, 19, 21, 23, 22, 24, 23, 21, 22, 20, 19, 17, 18, 16, 15.5};

        int period = 20;

        // 計算唐奇安通道
        List<double[]> channel = calculateDonchianChannel(highPrices, lowPrices, period);
        double[] upperChannel = channel.get(0);
        double[] middleChannel = channel.get(1);
        double[] lowerChannel = channel.get(2);

        // 生成交易信號
        List<String> signals = generateSignals(highPrices, lowPrices, closePrices, period);

        // 打印結果
        System.out.println("High\tLow\tClose\tUpper\tMiddle\tLower\tSignal");
        for (int i = 0; i < closePrices.length; i++) {
            String upper, middle, lower;

            if(i < period - 1)
            {
                upper = "N/A";
                middle = "N/A";
                lower = "N/A";
            }
            else
            {
                upper = String.format("%.2f", upperChannel[i-(period-1)]);
                middle = String.format("%.2f", middleChannel[i-(period-1)]);
                lower = String.format("%.2f", lowerChannel[i-(period-1)]);
            }

            System.out.println(String.format("%.2f", highPrices[i]) + "\t" +
                String.format("%.2f", lowPrices[i]) + "\t" +
                String.format("%.2f", closePrices[i]) + "\t" +
                upper + "\t" +
                middle + "\t" +
                lower + "\t" +
                signals.get(i));
        }
    }
}