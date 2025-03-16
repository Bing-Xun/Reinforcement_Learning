package feature;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class ChaikinOscillatorTrading {

    /**
     * 計算累計/派發線 (Accumulation/Distribution Line, ADL).
     *
     * @param high   最高價數組.
     * @param low    最低價數組.
     * @param close  收盤價數組.
     * @param volume 成交量數組 (現在是 double[]).
     * @return ADL 數組.
     */
    public static double[] calculateADL(double[] high, double[] low, double[] close, double[] volume) {
        if (high == null || low == null || close == null || volume == null ||
            high.length != low.length || high.length != close.length || high.length != volume.length) {
            return null; // 或拋出異常
        }

        double[] adl = new double[close.length];
        adl[0] = 0; // 初始值, 雖然沒用到

        for (int i = 0; i < close.length; i++) {
            double moneyFlowMultiplier = ((close[i] - low[i]) - (high[i] - close[i])) / (high[i] - low[i]);
            //若最高最低價相同
            if(Double.isNaN(moneyFlowMultiplier))
                moneyFlowMultiplier = 0;
            double moneyFlowVolume = moneyFlowMultiplier * volume[i];
            if(i>0)
                adl[i] = adl[i - 1] + moneyFlowVolume;
            else
                adl[i] = moneyFlowVolume;
        }

        return adl;
    }
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
     * 計算佳慶指標 (Chaikin Oscillator).
     *
     * @param high       最高價數組.
     * @param low        最低價數組.
     * @param close      收盤價數組.
     * @param volume     成交量數組 (現在是 double[]).
     * @param fastPeriod 快線週期 (通常為 3).
     * @param slowPeriod 慢線週期 (通常為 10).
     * @return Chaikin Oscillator 數組.
     */
    public static double[] calculateChaikinOscillator(double[] high, double[] low, double[] close, double[] volume, int fastPeriod, int slowPeriod) {
        double[] adl = calculateADL(high, low, close, volume);
        if (adl == null) {
            return null;
        }

        double[] fastEMA = calculateEMA(adl, fastPeriod);
        double[] slowEMA = calculateEMA(adl, slowPeriod);

        if(fastEMA == null || slowEMA == null) return null;

        double[] chaikinOscillator = new double[adl.length];
        for (int i = 0; i < adl.length; i++) {
            //都要有值
            if(i>= slowPeriod -1)
                chaikinOscillator[i] = fastEMA[i] - slowEMA[i];
            else
                chaikinOscillator[i] = 0;
        }

        return chaikinOscillator;
    }

    /**
     * 根據 Chaikin Oscillator 策略生成交易信號.
     *
     * @param high       最高價數組.
     * @param low        最低價數組.
     * @param close      收盤價數組.
     * @param volume     成交量數組 (現在是 double[]).
     * @param fastPeriod 快線週期.
     * @param slowPeriod 慢線週期.
     * @param maPeriod   移動平均線週期 (用於判斷突破).
     * @return 交易信號列表 ("BUY", "SELL", 或 "HOLD").
     */
    public static List<String> generateSignals(double[] high, double[] low, double[] close, double[] volume, int fastPeriod, int slowPeriod, int maPeriod) {
        double[] chaikinOscillator = calculateChaikinOscillator(high, low, close, volume, fastPeriod, slowPeriod);
        if (chaikinOscillator == null) {
            return null;
        }

        // 計算 Chaikin Oscillator 的移動平均線
        double[] oscillatorMA = calculateEMA(chaikinOscillator, maPeriod);


        List<String> signals = new ArrayList<>();
        //先hold
        for(int i = 0; i< Math.max(slowPeriod, maPeriod) - 1; ++i)
            signals.add("HOLD");

        for (int i = Math.max(slowPeriod, maPeriod) - 1; i < chaikinOscillator.length; i++) {

            int maIndex = i - (maPeriod - 1);
            // 指標由負轉正 (買入)
            if (chaikinOscillator[i - 1] < 0 && chaikinOscillator[i] > 0) {
                signals.add("BUY");
            }
            // 指標由正轉負 (賣出)
            else if (chaikinOscillator[i - 1] > 0 && chaikinOscillator[i] < 0) {
                signals.add("SELL");
            }
            // 指標向上突破其移動平均線 (買入)
            else if (maIndex > 0 && chaikinOscillator[i - 1] < oscillatorMA[maIndex - 1] && chaikinOscillator[i] > oscillatorMA[maIndex]) {
                signals.add("BUY");
            }
            // 指標向下跌破其移動平均線 (賣出)
            else if (maIndex > 0 && chaikinOscillator[i - 1] > oscillatorMA[maIndex - 1] && chaikinOscillator[i] < oscillatorMA[maIndex]) {
                signals.add("SELL");
            }
            // 其他情況，保持
            else {
                signals.add("HOLD");
            }
        }

        return signals;
    }
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
        int fastPeriod = 3;
        int slowPeriod = 10;
        int maPeriod = 9; // 用於判斷突破的移動平均線週期

        // 計算 Chaikin Oscillator
        double[] chaikinOscillator = calculateChaikinOscillator(highPrices, lowPrices, closePrices, volumes, fastPeriod, slowPeriod);

        // 生成交易信號
        List<String> signals = generateSignals(highPrices, lowPrices, closePrices, volumes, fastPeriod, slowPeriod, maPeriod);

        // 計算 Chaikin Oscillator 的移動平均線
        double[] oscillatorMA = calculateSMA(chaikinOscillator, maPeriod);

        // 打印結果
        System.out.println("Price\tChaikin Osc.\tMA\tSignal");
        for (int i = 0; i < closePrices.length; i++) {

            String oscStr, maStr;

            if(i< slowPeriod - 1)
                oscStr = "N/A";
            else
                oscStr = String.format("%.2f", chaikinOscillator[i]);

            if(i< maPeriod -1 || i< slowPeriod - 1)
                maStr = "N/A";
            else
                maStr = String.format("%.2f", oscillatorMA[i-(maPeriod - 1)]);


            System.out.println(String.format("%.2f", closePrices[i]) + "\t" +
                oscStr + "\t\t" +
                maStr + "\t" +
                signals.get(Math.min(i,signals.size()-1)));
        }
    }
}