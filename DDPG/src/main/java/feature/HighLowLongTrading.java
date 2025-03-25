package feature;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ensembleLearning.strategy.util.HighLowStrategyUtil;
import ensembleLearning.strategy.vo.HighLowTradingVO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HighLowLongTrading {

    public static HighLowTradingVO generateSignals(double[] closePrices) {
        List<Double> closePricesList = Arrays.stream(closePrices)
                .boxed() // 將 double 轉換為 Double
                .collect(java.util.stream.Collectors.toList());

        Double indexD = closePrices[closePrices.length-1];
        List<Double> decileList = HighLowStrategyUtil.calculatePercentiles(closePricesList);
        int decile = HighLowStrategyUtil.getPercentile(indexD, decileList);

        String action = "HOLD";
        Integer weight = 1;

        if(decile > 70) {
            action = "SELL";
            weight = 2;
        }
        if(decile <= 10) {
            action = "BUY";
            weight = 2;
        }

//        if(!"HOLD".equals(action)) {
//            try {
//                System.out.println(action);
//                System.out.println();
//                System.out.println(new ObjectMapper().writeValueAsString(decileList));
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        }

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