package feature;

import java.util.ArrayList;
import java.util.List;

public class MomentumROCTrading {

    /**
     * 計算動量指標 (Momentum, MTM).
     *
     * @param close  收盤價數組.
     * @param period 週期 (例如，10 表示 10 日動量).
     * @return MTM 數組.
     */
    public static double[] calculateMTM(double[] close, int period) {
        if (close == null || period <= 0 || close.length <= period) {
            return null; // 或拋出異常
        }

        double[] mtm = new double[close.length - period];
        for (int i = 0; i < mtm.length; i++) {
            mtm[i] = close[i + period] - close[i];
        }
        return mtm;
    }

    /**
     * 計算變化率指標 (Rate of Change, ROC).
     *
     * @param close  收盤價數組.
     * @param period 週期 (例如，12 表示 12 日 ROC).
     * @return ROC 數組.
     */
    public static double[] calculateROC(double[] close, int period) {
        if (close == null || period <= 0 || close.length <= period) {
            return null; // 或拋出異常
        }

        double[] roc = new double[close.length - period];
        for (int i = 0; i < roc.length; i++) {
            roc[i] = ((close[i + period] - close[i]) / close[i]) * 100;
        }
        return roc;
    }

    /**
     * 根據 MTM/ROC 策略生成交易信號.
     *
     * @param data   輸入的數據數組 (MTM 或 ROC).  注意不是價格
     * @param period      週期.
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] data, int period) {
        if (data == null) {
            return null;
        }

        List<String> signals = new ArrayList<>();
        //前面先hold
        for(int i = 0; i < period; ++i)
            signals.add("HOLD");

        for (int i = 1; i < data.length; i++) {
            // MTM/ROC 由負轉正 (買入)
            if (data[i - 1] < 0 && data[i] > 0) {
                signals.add("BUY");
            }
            // MTM/ROC 由正轉負 (賣出)
            else if (data[i - 1] > 0 && data[i] < 0) {
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
        int period = 10;

        // 計算 MTM
        double[] mtm = calculateMTM(closePrices, period);

        // 計算 ROC
        double[] roc = calculateROC(closePrices, period);

        // 生成 MTM 交易信號
        List<String> mtmSignals = generateSignals(mtm, period);

        // 生成 ROC 交易信號
        List<String> rocSignals = generateSignals(roc, period);

        // 打印結果
        System.out.println("Price\tMTM\tSignal (MTM)    ROC\tSignal (ROC)");
        for (int i = 0; i < closePrices.length; i++) {

            String mtmStr, rocStr;

            if(i < period)
            {
                mtmStr = "N/A";
                rocStr = "N/A";
            }
            else
            {
                mtmStr = String.format("%.2f", mtm[i - period]);
                rocStr = String.format("%.2f", roc[i - period]);
            }

            System.out.println(String.format("%.2f", closePrices[i]) + "\t" +
                mtmStr + "\t" +
                mtmSignals.get(Math.min(i, mtmSignals.size() -1)) + "     \t" +
                rocStr + "\t" +
                rocSignals.get(Math.min(i, rocSignals.size()-1)));
        }
    }
}
