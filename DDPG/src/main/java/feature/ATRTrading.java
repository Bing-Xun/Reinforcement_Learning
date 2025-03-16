package feature;

import java.util.ArrayList;
import java.util.List;

public class ATRTrading {

    /**
     * 計算真實波幅 (True Range, TR).
     *
     * @param high   最高價數組.
     * @param low    最低價數組.
     * @param close  收盤價數組.
     * @return TR 數組.
     */
    public static double[] calculateTR(double[] high, double[] low, double[] close) {
        if (high == null || low == null || close == null || high.length != low.length || high.length != close.length || high.length == 0) {
            return null; // 或拋出異常
        }

        double[] tr = new double[close.length];
        tr[0] = high[0] - low[0]; // 第一個 TR 值

        for (int i = 1; i < close.length; i++) {
            double h_minus_l = high[i] - low[i];
            double h_minus_pc = Math.abs(high[i] - close[i - 1]);
            double l_minus_pc = Math.abs(low[i] - close[i - 1]);
            tr[i] = Math.max(h_minus_l, Math.max(h_minus_pc, l_minus_pc));
        }

        return tr;
    }

    /**
     * 計算平均真實波幅 (Average True Range, ATR).
     *
     * @param high   最高價數組.
     * @param low    最低價數組.
     * @param close  收盤價數組.
     * @param period 週期 (通常為 14).
     * @return ATR 數組.
     */
    public static double[] calculateATR(double[] high, double[] low, double[] close, int period) {
        if (high == null || low == null || close == null || high.length != low.length || high.length != close.length ||
            period <= 0 || period > high.length) {
            return null; // 或拋出異常
        }

        double[] tr = calculateTR(high, low, close);
        double[] atr = new double[close.length];

        // 第一個 ATR 使用 TR 的 SMA
        double sum = 0;
        for(int i = 0; i < period; ++i)
            sum += tr[i];
        atr[period - 1] = sum / period;

        // 後續的 ATR 使用 Wilder's smoothing method
        for (int i = period; i < close.length; i++) {
            atr[i] = (atr[i - 1] * (period - 1) + tr[i]) / period;
        }

        return atr;
    }

    /**
     * 根據 ATR 設置止損和止盈.
     *
     * @param entryPrice   入場價格.
     * @param isLong       是否是多頭 (true: 多頭, false: 空頭).
     * @param atr          ATR 值.
     * @param stopLossMultiplier 止損倍數 (ATR 的倍數).
     * @param takeProfitMultiplier 止盈倍數 (ATR 的倍數).
     * @return 包含止損價和止盈價的數組 [stopLoss, takeProfit].
     */
    public static double[] calculateStopLossAndTakeProfit(double entryPrice, boolean isLong, double atr, double stopLossMultiplier, double takeProfitMultiplier) {
        double stopLoss;
        double takeProfit;

        if (isLong) {
            stopLoss = entryPrice - (atr * stopLossMultiplier);
            takeProfit = entryPrice + (atr * takeProfitMultiplier);
        } else {
            stopLoss = entryPrice + (atr * stopLossMultiplier);
            takeProfit = entryPrice - (atr * takeProfitMultiplier);
        }

        return new double[] {stopLoss, takeProfit};
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
        int period = 14;
        double entryPrice = 30.0; // 假設入場價格
        boolean isLong = true;   // 假設是多頭
        double stopLossMultiplier = 2.0; // 止損倍數
        double takeProfitMultiplier = 3.0; // 止盈倍數

        // 計算 ATR
        double[] atr = calculateATR(highPrices, lowPrices, closePrices, period);

        // 設置止損和止盈
        // 從有atr開始
        double[] stopLossAndTakeProfit = calculateStopLossAndTakeProfit(entryPrice, isLong, atr[period - 1], stopLossMultiplier, takeProfitMultiplier);


        // 打印結果
        System.out.println("Price\tATR\tStop Loss\tTake Profit");

        for (int i = 0; i < closePrices.length; i++) {
            String atrStr;
            if(i < period -1)
                atrStr = "N/A";
            else
                atrStr = String.format("%.2f", atr[i]);

            //假設每次都重新入場
            if(i >= period - 1)
            {
                stopLossAndTakeProfit = calculateStopLossAndTakeProfit(closePrices[i], isLong, atr[i], stopLossMultiplier, takeProfitMultiplier);
            }

            System.out.println(String.format("%.2f", closePrices[i]) + "\t" +
                atrStr + "\t" +
                (i < period - 1 ? "N/A" : String.format("%.2f", stopLossAndTakeProfit[0])) + "\t\t" +
                (i < period - 1 ? "N/A" : String.format("%.2f", stopLossAndTakeProfit[1])));

        }

    }
}