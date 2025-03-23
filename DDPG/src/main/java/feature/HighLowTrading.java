package feature;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HighLowTrading {

    public static String generateSignals(double[] closePrices) {
        List<Double> closePricesList = Arrays.stream(closePrices)
                .boxed() // 將 double 轉換為 Double
                .collect(java.util.stream.Collectors.toList());


        Double indexD = closePrices[closePrices.length-1];
        // 對 List<Double> 進行排序
        Collections.sort(closePricesList); // 預設為升序排序
        int index = closePricesList.indexOf(indexD);

        String signals = "HOLD";
        if(index >= 0.6 * closePricesList.size()) {
            signals = "SELL";
        }
        if(index <= 0.2 * closePricesList.size() && index >= 0.1 * closePricesList.size()) {
            signals = "BUY";
        }

        return signals;
    }

//    public static List<String> generateSignals(double[] close) {
//
//    }

    public static void main(String[] args) {
        // 示例數據
        double[] closePrices = {
            25, 26, 27, 26, 28, 29, 30, 29, 31, 32,
            31, 30, 29, 28, 27, 28, 29, 30, 31, 32,
            33, 34, 35, 34, 33, 32, 31, 30, 29, 28
        };
        double[] highPrices = new double[closePrices.length];
        double[] lowPrices = new double[closePrices.length];

        System.out.println(generateSignals(closePrices));
    }
}