package feature;

import java.util.ArrayList;
import java.util.List;

public class ParabolicSARTrading {

    /**
     * 計算拋物線轉向指標 (Parabolic SAR).
     *
     * @param high       最高價數組.
     * @param low        最低價數組.
     * @param afStart    初始加速因子 (通常為 0.02).
     * @param afIncrement 加速因子增量 (通常為 0.02).
     * @param afMax      最大加速因子 (通常為 0.2).
     * @return SAR 數組.
     */
    public static double[] calculateSAR(double[] high, double[] low, double afStart, double afIncrement, double afMax) {
        if (high == null || low == null || high.length != low.length || high.length < 2) {
            return null; // 或拋出異常
        }

        int n = high.length;
        double[] sar = new double[n];
        boolean isUpTrend = true; // 初始趨勢假設為上升
        double af = afStart;     // 加速因子
        double ep;              // 極值點 (Extreme Point)

        // 初始化第一個 SAR 值和 EP
        sar[0] = low[0]; // 隨便設, 只要初始值正確就好
        if (high[1] > high[0]) { // 第二天價格更高，上升趨勢
            isUpTrend = true;
            ep = high[1];
            sar[1] = low[0];
        } else {
            isUpTrend = false;
            ep = low[1];
            sar[1] = high[0];
        }

        // 計算後續的 SAR 值
        for (int i = 2; i < n; i++) {
            double prevSAR = sar[i - 1];
            double nextSAR;

            if (isUpTrend) {
                // 上升趨勢
                nextSAR = prevSAR + af * (ep - prevSAR);
                // 檢查趨勢是否反轉
                if (nextSAR > low[i]) {
                    isUpTrend = false;
                    af = afStart;
                    nextSAR = ep; // SAR 變為之前的 EP
                    ep = low[i];   // 新的 EP
                } else {
                    // 更新 EP
                    if (high[i] > ep) {
                        ep = high[i];
                        af = Math.min(af + afIncrement, afMax); // 加速因子增加，但不超過最大值
                    }
                    // 確保 SAR 不會進入前一日或當日的價格區間
                    nextSAR = Math.min(nextSAR, Math.min(low[i - 1], low[i - 2]));

                }
            } else {
                // 下降趨勢
                nextSAR = prevSAR - af * (prevSAR - ep);

                // 檢查趨勢是否反轉
                if (nextSAR < high[i]) {
                    isUpTrend = true;
                    af = afStart;
                    nextSAR = ep; // SAR 變為之前的 EP
                    ep = high[i];  // 新的 EP
                } else {
                    // 更新 EP
                    if (low[i] < ep) {
                        ep = low[i];
                        af = Math.min(af + afIncrement, afMax); // 加速因子增加，但不超過最大值
                    }
                    // 確保 SAR 不會進入前一日或當日的價格區間
                    nextSAR = Math.max(nextSAR, Math.max(high[i - 1], high[i - 2]));
                }
            }

            sar[i] = nextSAR;
        }

        return sar;
    }

    /**
     * 根據 Parabolic SAR 策略生成交易信號.
     *
     * @param high       最高價數組.
     * @param low        最低價數組.
     * @param afStart    初始加速因子.
     * @param afIncrement 加速因子增量.
     * @param afMax      最大加速因子.
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] high, double[] low, double afStart, double afIncrement, double afMax) {
        double[] sar = calculateSAR(high, low, afStart, afIncrement, afMax);
        if (sar == null) {
            return null;
        }

        List<String> signals = new ArrayList<>();
        signals.add("HOLD");//第一個沒信號

        for (int i = 1; i < sar.length; i++) {
            // SAR 從價格上方移動到價格下方 (買入)
            // 比較難直接比較, 因為sar會跳到最高最低價, 因此用趨勢判斷
            if(sar[i-1] >= high[i-1] && sar[i] <= low[i]) //這裡用前一天比, 比較合理
            {
                signals.add("BUY");
            }
            else if(sar[i-1] <= low[i-1] && sar[i] >= high[i])//這裡用前一天比, 比較合理
            {
                signals.add("SELL");
            }
            else
                signals.add("HOLD");
        }

        return signals;
    }

    public static void main(String[] args) {
        // 示例數據
        double[] highPrices = {12,13,15,13,15,17,19,21,20,22,21,20,19,18,17,19,20,22,24,23};
        double[] lowPrices =  {10,11,12,11,13,15,16,18,18,19,19,18,16,15,15,17,18,19,21,20};

        // SAR 參數
        double afStart = 0.02;
        double afIncrement = 0.02;
        double afMax = 0.2;

        // 計算 SAR
        double[] sar = calculateSAR(highPrices, lowPrices, afStart, afIncrement, afMax);

        // 生成交易信號
        List<String> signals = generateSignals(highPrices, lowPrices, afStart, afIncrement, afMax);

        // 打印結果
        System.out.println("High\tLow\tSAR\t\tSignal");
        for (int i = 0; i < highPrices.length; i++) {
            System.out.println(String.format("%.2f", highPrices[i]) + "\t" +
                String.format("%.2f", lowPrices[i]) + "\t" +
                String.format("%.2f", sar[i]) + "\t" +
                signals.get(Math.min(i,signals.size()-1)));
        }
    }
}