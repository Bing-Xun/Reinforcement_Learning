package feature;

import java.util.ArrayList;
import java.util.List;

public class OBVTrading {

    /**
     * 計算能量潮指標 (OBV).
     *
     * @param close   收盤價數組.
     * @param volume  成交量數組 (現在是 double[]).
     * @return OBV 數組 (現在是 double[]).
     */
    public static double[] calculateOBV(double[] close, double[] volume) {
        if (close == null || volume == null || close.length != volume.length || close.length == 0) {
            return null; // 或拋出異常
        }

        double[] obv = new double[close.length];
        obv[0] = volume[0]; // 第一個 OBV 值通常等於第一個成交量

        for (int i = 1; i < close.length; i++) {
            if (close[i] > close[i - 1]) {
                obv[i] = obv[i - 1] + volume[i];
            } else if (close[i] < close[i - 1]) {
                obv[i] = obv[i - 1] - volume[i];
            } else {
                obv[i] = obv[i - 1];
            }
        }

        return obv;
    }

    /**
     * 根據 OBV 策略生成交易信號.
     *
     * @param close   收盤價數組.
     * @param volume  成交量數組 (現在是 double[]).
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] close, double[] volume) {
        double[] obv = calculateOBV(close, volume);
        if (obv == null) {
            return null;
        }

        List<String> signals = new ArrayList<>();
        signals.add("HOLD");

        // 用於簡化版頂背離判斷的變數
        double prevPriceHigh = close[0];
        double prevOBVHigh = obv[0];


        for (int i = 1; i < obv.length; i++) {
            String signal = "HOLD";

            // OBV 上升趨勢 (簡化版：連續兩個上升)
            if (obv[i] > obv[i - 1] ) {
                signal = "BUY"; // 這裡可以更嚴格，例如要求突破前期高點
            }
            // OBV 下降趨勢 (簡化版：連續兩個下降)
            else if (obv[i] < obv[i - 1] ) {
                signal = "SELL"; // 這裡可以更嚴格，例如要求跌破前期低點
            }

            // 簡化版頂背離 (價格創新高，OBV 未創新高)
            if (close[i] > prevPriceHigh && obv[i] < prevOBVHigh) {
                // signal = "SELL";
            }

            // 更新前高 (簡化版)
            if (close[i] > prevPriceHigh) {
                prevPriceHigh = close[i];
                prevOBVHigh = obv[i];
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
        double[] volumes = { // 現在是 double[]
            1000, 1200, 1500, 900, 1800, 2000, 1600, 1400, 2200, 2500,
            1800, 1500, 1200, 1000, 800, 1100, 1300, 1700, 1900, 2100,
            2300, 2600, 2800, 2500, 2000, 1800, 1500, 1300, 1100, 900
        };

        // 計算 OBV
        double[] obv = calculateOBV(closePrices, volumes);

        // 生成交易信號
        List<String> signals = generateSignals(closePrices, volumes);

        // 打印結果
        System.out.println("Price\tVolume\tOBV\t\tSignal");
        for (int i = 0; i < closePrices.length; i++) {
            System.out.println(String.format("%.2f", closePrices[i]) + "\t" +
                String.format("%.2f",volumes[i]) + "\t" +  // volume 也格式化
                String.format("%.2f",obv[i]) + "\t\t" +
                signals.get(i));
        }
    }
}