package feature;

import java.util.ArrayList;
import java.util.List;

public class WilliamsRTrading {

    /**
     * 計算威廉指標 (%R).
     *
     * @param high     最高價數組.
     * @param low      最低價數組.
     * @param close    收盤價數組.
     * @param period   週期 (通常為 14).
     * @return %R 數組.
     */
    public static double[] calculateWilliamsR(double[] high, double[] low, double[] close, int period) {
        if (high == null || low == null || close == null || high.length != low.length || high.length != close.length ||
            period <= 0 || period > high.length) {
            return null; // 或拋出異常
        }

        double[] williamsR = new double[close.length];
        for (int i = period - 1; i < close.length; i++) {
            double highestHigh = high[i];
            double lowestLow = low[i];
            for (int j = i - period + 1; j <= i; j++) {
                highestHigh = Math.max(highestHigh, high[j]);
                lowestLow = Math.min(lowestLow, low[j]);
            }
            williamsR[i] = ((highestHigh - close[i]) / (highestHigh - lowestLow)) * -100;
        }

        return williamsR;
    }

    /**
     * 根據威廉指標 (%R) 的超買/超賣策略生成交易信號.
     *
     * @param high     最高價數組.
     * @param low      最低價數組.
     * @param close    收盤價數組.
     * @param period   週期.
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] high, double[] low, double[] close, int period) {
        double[] williamsR = calculateWilliamsR(high, low, close, period);
        if (williamsR == null) {
            return null;
        }

        List<String> signals = new ArrayList<>();
        for(int i = 0; i < period - 1; ++i)
            signals.add("HOLD");
        for (int i = period - 1; i < close.length; i++) {
            // 超賣區域向上回升 (買入)
            if (williamsR[i - 1] < -80 && williamsR[i] >= -80) {
                signals.add("BUY");
            }
            // 超買區域向下回落 (賣出)
            else if (williamsR[i - 1] > -20 && williamsR[i] <= -20) {
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

        double[] highPrices = new double[closePrices.length];
        double[] lowPrices = new double[closePrices.length];
        // 這裡只是為了方便, 實務上要給值
        for(int i = 0; i< closePrices.length; ++i)
        {
            highPrices[i] = closePrices[i] * 1.05;
            lowPrices[i] = closePrices[i] * 0.95;
        }

        int period = 14;

        // 計算 %R
        double[] williamsR = calculateWilliamsR(highPrices, lowPrices, closePrices, period);

        // 生成交易信號
        List<String> signals = generateSignals(highPrices, lowPrices, closePrices, period);

        // 打印結果
        System.out.println("Price\t%R\tSignal");
        for (int i = 0; i < closePrices.length; i++) {
            String rStr;

            if(i < period -1)
                rStr = "N/A";
            else
                rStr = String.format("%.2f", williamsR[i]);
            System.out.println(String.format("%.2f", closePrices[i]) + "\t" +
                rStr + "\t" +
                signals.get(i));
        }
    }
}