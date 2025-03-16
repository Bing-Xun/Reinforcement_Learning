package feature;

import java.util.ArrayList;
import java.util.List;

public class MFITrading {

    /**
     * 計算典型價格 (Typical Price).
     *
     * @param high 最高價數組.
     * @param low  最低價數組.
     * @param close 收盤價數組.
     * @return 典型價格數組.
     */
    public static double[] calculateTypicalPrice(double[] high, double[] low, double[] close) {
        if (high == null || low == null || close == null || high.length != low.length || high.length != close.length) {
            return null; // 或拋出異常
        }

        double[] typicalPrice = new double[close.length];
        for (int i = 0; i < close.length; i++) {
            typicalPrice[i] = (high[i] + low[i] + close[i]) / 3.0;
        }
        return typicalPrice;
    }

    /**
     * 計算資金流量指標 (Money Flow Index, MFI).
     *
     * @param high     最高價數組.
     * @param low      最低價數組.
     * @param close    收盤價數組.
     * @param volume   成交量數組 (現在是 double[]).
     * @param period   週期 (通常為 14).
     * @return MFI 數組.
     */
    public static double[] calculateMFI(double[] high, double[] low, double[] close, double[] volume, int period) {
        if (high == null || low == null || close == null || volume == null ||
            high.length != low.length || high.length != close.length || high.length != volume.length ||
            period <= 0 || period >= high.length) {
            return null; // 或拋出異常
        }

        double[] typicalPrice = calculateTypicalPrice(high, low, close);
        double[] moneyFlow = new double[close.length];
        double[] positiveMoneyFlow = new double[close.length];
        double[] negativeMoneyFlow = new double[close.length];
        double[] mfi = new double[close.length];


        // 計算 Money Flow
        for (int i = 0; i < close.length; i++) {
            moneyFlow[i] = typicalPrice[i] * volume[i];
        }

        // 計算 Positive and Negative Money Flow
        for (int i = 1; i < close.length; i++) {
            if (typicalPrice[i] > typicalPrice[i - 1]) {
                positiveMoneyFlow[i] = moneyFlow[i];
                negativeMoneyFlow[i] = 0;
            } else if (typicalPrice[i] < typicalPrice[i - 1]) {
                negativeMoneyFlow[i] = moneyFlow[i];
                positiveMoneyFlow[i] = 0;
            } else {
                positiveMoneyFlow[i] = 0;
                negativeMoneyFlow[i] = 0;
            }
        }

        // 計算 MFI
        for (int i = period; i < close.length; i++) {
            double sumPositive = 0;
            double sumNegative = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sumPositive += positiveMoneyFlow[j];
                sumNegative += negativeMoneyFlow[j];
            }
            double moneyRatio = (sumNegative == 0) ? Double.POSITIVE_INFINITY : sumPositive / sumNegative; // 避免除以零
            mfi[i] = 100.0 - (100.0 / (1.0 + moneyRatio));
        }

        return mfi;
    }

    /**
     * 根據 MFI 超買/超賣和簡化版背離策略生成交易信號.
     *
     * @param high     最高價數組.
     * @param low      最低價數組.
     * @param close    收盤價數組.
     * @param volume   成交量數組 (現在是 double[]).
     * @param period   MFI 的週期.
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] high, double[] low, double[] close, double[] volume, int period) {
        double[] mfi = calculateMFI(high, low, close, volume, period);
        if (mfi == null) {
            return null;
        }

        List<String> signals = new ArrayList<>();
        for(int i = 0; i < period; ++i)
            signals.add("HOLD");

        // 用於簡化版背離判斷的變數
        double prevPriceLow = close[0];
        double prevMFILow = mfi[0];
        double prevPriceHigh = close[0];
        double prevMFIHigh = mfi[0];

        for (int i = period; i < close.length; i++) {
            String signal = "HOLD";

            // 超賣區域向上回升 (買入)
            if (mfi[i - 1] < 20 && mfi[i] >= 20) {
                signal = "BUY";
            }
            // 超買區域向下回落 (賣出)
            else if (mfi[i - 1] > 80 && mfi[i] <= 80) {
                signal = "SELL";
            }

            // 簡化版底背離 (價格創新低，MFI 不創新低)
            if (close[i] < prevPriceLow && mfi[i] > prevMFILow) {
                //signal = "BUY";  // 可選
            }

            // 簡化版頂背離 (價格創新高，MFI 不創新高)
            if (close[i] > prevPriceHigh && mfi[i] < prevMFIHigh) {
                //signal = "SELL"; // 可選
            }

            // 更新前低/前高 (簡化版)
            if (close[i] < prevPriceLow) {
                prevPriceLow = close[i];
                prevMFILow = mfi[i];
            }
            if (close[i] > prevPriceHigh) {
                prevPriceHigh = close[i];
                prevMFIHigh = mfi[i];
            }

            signals.add(signal);
        }

        return signals;
    }

    public static void main(String[] args) {
        // 示例數據
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
        double[] volumes = { // 現在是 double[]
            1000, 1200, 1500, 900, 1800, 2000, 1600, 1400, 2200, 2500,
            1800, 1500, 1200, 1000, 800, 1100, 1300, 1700, 1900, 2100,
            2300, 2600, 2800, 2500, 2000, 1800, 1500, 1300, 1100, 900
        };
        int period = 14;

        // 計算 MFI
        double[] mfi = calculateMFI(highPrices, lowPrices, closePrices, volumes, period);

        // 生成交易信號
        List<String> signals = generateSignals(highPrices, lowPrices, closePrices, volumes, period);

        // 打印結果
        System.out.println("Price\tMFI\tSignal");
        for (int i = 0; i < closePrices.length; i++) {
            String mfiStr;
            if(i < period)
                mfiStr = "N/A";
            else
                mfiStr = String.format("%.2f", mfi[i]);
            System.out.println(String.format("%.2f", closePrices[i]) + "\t" +
                mfiStr + "\t" +
                signals.get(i));
        }
    }
}