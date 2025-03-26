package feature;

import ensembleLearning.util.HighLowStrategyUtil;
import ensembleLearning.strategy.vo.HighLowTradingVO;

import java.util.Arrays;
import java.util.List;

public class HighLowTrading {

    public static HighLowTradingVO generateSignals(double[] closePrices) {
        List<Double> closePricesList = Arrays.stream(closePrices)
                .boxed() // 將 double 轉換為 Double
                .collect(java.util.stream.Collectors.toList());

        Double perD = closePrices[closePrices.length-1];
        List<Double> decileList = HighLowStrategyUtil.calculatePercentiles(closePricesList);
        int decile = HighLowStrategyUtil.getPercentile(perD, decileList);

        String action = "HOLD";
        Integer weight = 1;

        if(decile > 85) {
            action = "SELL";
            weight = 2;
        }
        if(decile <= 10) {
            action = "BUY";
            weight = 2;
        }

        return HighLowTradingVO.builder()
            .action(action)
            .weight(weight)
            .build();
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

        System.out.println(generateSignals(closePrices));
    }
}