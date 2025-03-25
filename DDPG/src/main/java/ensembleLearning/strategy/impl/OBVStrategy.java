package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.vo.StrategyVO;
import feature.OBVTrading;

import java.util.List;

public class OBVStrategy implements Strategy { // 2

    private String strategyName = "OBVStrategy";

    public String predict(double[] closePrices, double[]  volumes) {
        List<String> signals = OBVTrading.generateSignals(closePrices, volumes);
        return signals.get(closePrices.length-1);
    }

    @Override
    public StrategyVO predict(List<QuoteVO> quoteVOList) {
        double[] closePrices = new double[quoteVOList.size()];
        double[] volumes = new double[quoteVOList.size()];

        for (int i = 0; i < quoteVOList.size(); i++) {
            QuoteVO quote = quoteVOList.get(i);
            closePrices[i] = quote.getClose().doubleValue();
            volumes[i] = quote.getVolume().doubleValue();
        }

        return StrategyVO.builder()
            .strategyName(strategyName)
            .action(predict(closePrices, volumes))
            .closeTime(quoteVOList.getLast().getCloseTime())
            .build();
    }

    @Override
    public String getStrategyName() {
        return strategyName;
    }

    public static void main(String[] args) {
        // 示例數據
        double[] closePrices = {
            25, 26, 27, 26, 28, 29, 30, 29, 31, 32,
            31, 30, 29, 28, 27, 28, 29, 30, 31, 32,
            33, 34, 35, 34, 33, 32, 31, 30, 29, 28
        };
        double[] volumes = {
            1000, 1200, 1500, 900, 1800, 2000, 1600, 1400, 2200, 2500,
            1800, 1500, 1200, 1000, 800, 1100, 1300, 1700, 1900, 2100,
            2300, 2600, 2800, 2500, 2000, 1800, 1500, 1300, 1100, 900
        };

        System.out.println(new OBVStrategy().predict(closePrices, volumes));
    }
}