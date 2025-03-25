package feature;

import ensembleLearning.strategy.vo.HighLowTradingVO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HighLowShortTrading {

    public static HighLowTradingVO generateSignals(double[] closePrices) {
        List<Double> closePricesList = Arrays.stream(closePrices)
                .boxed() // 將 double 轉換為 Double
                .collect(java.util.stream.Collectors.toList());


        Double indexD = closePrices[closePrices.length-1];
        // 對 List<Double> 進行排序
        Collections.sort(closePricesList); // 預設為升序排序

        int index = closePricesList.indexOf(indexD);
        String action = "HOLD";
        Integer weight = 1;

        if(index >= 0.8 * closePricesList.size()) {
//            System.out.println("HighLowShortTrading:"+index);
//            System.out.println("HighLowShortTrading:"+closePricesList.size());
            action = "SELL";
        }
        if(index <= 0.2 * closePricesList.size()) {
            action = "BUY";
        }

        return HighLowTradingVO.builder()
            .action(action)
            .weight(weight)
            .build();
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