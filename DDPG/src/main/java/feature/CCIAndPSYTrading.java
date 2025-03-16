package feature;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class CCIAndPSYTrading {

    /**
     * 計算典型價格 (Typical Price).  (與之前程式碼中的 calculateTypicalPrice 函數相同)
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
     *  計算SMA  (與之前程式碼中的 calculateSMA 函數相同，此處省略)
     */
    public static double[] calculateSMA(double[] data, int period) {
        if (data == null || data.length < period || period <= 0) {
            return null; // 或拋出異常
        }
        double[] sma = new double[data.length - period + 1];

        for (int i = 0; i < data.length - period + 1; i++) {
            double sum = 0;
            for(int j = 0; j < period; j++){
                sum += data[i + j];
            }
            sma[i] = sum/period;
        }
        return sma;
    }
    /**
     * 計算平均絕對偏差 (Mean Absolute Deviation, MAD).
     *
     * @param data   數據數組.
     * @param sma    SMA 數組 (與 data 長度相同).
     * @param period 週期.
     * @return MAD 數組.
     */
    public static double[] calculateMAD(double[] data, double[] sma, int period) {
        if (data == null || sma == null || data.length != sma.length + period - 1 || period <= 0) {
            return null; // 或拋出異常
        }

        double[] mad = new double[data.length - period + 1];
        for (int i = 0; i < mad.length; i++) {
            double sum = 0;
            for (int j = 0; j < period; j++) {
                sum += Math.abs(data[i + j] - sma[i]);
            }
            mad[i] = sum / period;
        }
        return mad;
    }

    /**
     * 計算順勢指標 (Commodity Channel Index, CCI).
     *
     * @param high   最高價數組.
     * @param low    最低價數組.
     * @param close  收盤價數組.
     * @param period 週期 (通常為 20).
     * @return CCI 數組.
     */
    public static double[] calculateCCI(double[] high, double[] low, double[] close, int period) {
        if (high == null || low == null || close == null || high.length != low.length || high.length != close.length ||
            period <= 0 || period > high.length) {
            return null; // 或拋出異常
        }

        double[] typicalPrice = calculateTypicalPrice(high, low, close);
        double[] sma = calculateSMA(typicalPrice, period);
        double[] mad = calculateMAD(typicalPrice, sma, period);

        double[] cci = new double[close.length - period + 1];
        for (int i = 0; i < cci.length; i++) {
            if (mad[i] == 0) {
                cci[i] = 0; // 避免除以零
            } else {
                cci[i] = (typicalPrice[i + period - 1] - sma[i]) / (0.015 * mad[i]);
            }
        }

        return cci;
    }

    /**
     * 根據 CCI 策略生成交易信號.
     *
     * @param high   最高價數組.
     * @param low    最低價數組.
     * @param close  收盤價數組.
     * @param period 週期.
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateCCISignals(double[] high, double[] low, double[] close, int period) {
        double[] cci = calculateCCI(high, low, close, period);
        if (cci == null) {
            return null;
        }

        List<String> signals = new ArrayList<>();
        for (int i = 0; i < period - 1; i++)
            signals.add("HOLD");

        // 循环从 period - 1 开始
        for (int i = period - 1; i < close.length; i++) {
            // 注意这里的索引变化
            int cciIndex = i - (period - 1); // cci 数组的当前索引

            // CCI 從超賣區域向上突破 -100 (買入)
            // 检查 cciIndex - 1 是否越界
            if (cciIndex > 0 && cci[cciIndex - 1] < -100 && cci[cciIndex] >= -100) {
                signals.add("BUY");
            }
            // CCI 從超買區域向下跌破 +100 (賣出)
            // 检查 cciIndex - 1 是否越界
            else if (cciIndex > 0 && cci[cciIndex - 1] > 100 && cci[cciIndex] <= 100) {
                signals.add("SELL");
            }
            // 其他情況，保持
            else {
                signals.add("HOLD");
            }
        }

        return signals;
    }

    /**
     * 計算心理線 (Psychological Line, PSY).
     *
     * @param close  收盤價數組.
     * @param period 週期 (通常為 12).
     * @return PSY 數組.
     */
    public static double[] calculatePSY(double[] close, int period) {
        if (close == null || period <= 0 || close.length < period) {
            return null; // 或拋出異常
        }

        double[] psy = new double[close.length - period + 1];
        for (int i = 0; i < psy.length; i++) {
            int upDays = 0;
            // 内部循环少比较一次
            for (int j = 0; j < period - 1; j++) {
                if (close[i + j + 1] > close[i + j]) {
                    upDays++;
                }
            }
            // 单独处理 i + period - 1 和 i + period - 2 的比较，确保不会越界
            if(i + period < close.length  && close[i + period - 1] > close[i + period - 2])
                upDays++;

            psy[i] = ((double) upDays / period) * 100;
        }
        return psy;
    }

    /**
     * 根據 PSY 策略生成交易信號.
     *
     * @param close      收盤價數組.
     * @param period     週期.
     * @param buyThreshold   買入閾值 (例如 25).
     * @param sellThreshold  賣出閾值 (例如 75).
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */

    public static List<String> generatePSYSignals(double[] close, int period, double buyThreshold, double sellThreshold)
    {
        double[] psy = calculatePSY(close, period);
        if (psy == null) {
            return null;
        }

        List<String> signals = new ArrayList<>();
        for (int i = 0; i < period - 1; i++)
            signals.add("HOLD");

        for(int i = period - 1; i< close.length; ++i)
        {
            if(psy[i-(period - 1)] <= buyThreshold)
                signals.add("BUY");
            else if (psy[i - (period - 1)] >= sellThreshold)
                signals.add("SELL");
            else
                signals.add("HOLD");
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
        int cciPeriod = 20;
        int psyPeriod = 12;
        double psyBuyThreshold = 25;
        double psySellThreshold = 75;

        // 計算 CCI
        double[] cci = calculateCCI(highPrices, lowPrices, closePrices, cciPeriod);

        // 生成 CCI 交易信號
        List<String> cciSignals = generateCCISignals(highPrices, lowPrices, closePrices, cciPeriod);

        // 計算 PSY
        double[] psy = calculatePSY(closePrices, psyPeriod);

        //生成PSY信號
        List<String> psySignals = generatePSYSignals(closePrices, psyPeriod, psyBuyThreshold, psySellThreshold);

        // 打印結果
        System.out.println("Price\tCCI\tCCI Signal\tPSY\tPSY Signal");
        for (int i = 0; i < closePrices.length; i++) {

            String cciStr, psyStr;

            if(i < cciPeriod - 1)
                cciStr = "N/A";
            else
                cciStr = String.format("%.2f", cci[i-(cciPeriod - 1)]);

            if(i < psyPeriod - 1)
                psyStr = "N/A";
            else
                psyStr = String.format("%.2f", psy[i - (psyPeriod - 1)]);

            System.out.println(String.format("%.2f", closePrices[i]) + "\t" +
                cciStr + "\t" +
                cciSignals.get(Math.min(i,cciSignals.size()-1)) + " \t\t" +
                psyStr + "\t" +
                psySignals.get(Math.min(i, psySignals.size() -1)));
        }
    }
}