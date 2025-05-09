package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.vo.StrategyVO;
import feature.ParabolicSARTrading;

import java.util.List;

public class ParabolicSARStrategy implements Strategy { // 2

    private String strategyName = "ParabolicSARStrategy";

    private double afStart = 0.02;
    private double afIncrement = 0.02;
    private double afMax = 0.2;

    public String predict(double[] highPrices, double[]  lowPrices) {
        List<String> signals = ParabolicSARTrading.generateSignals(highPrices, lowPrices, afStart, afIncrement, afMax);
        return signals.get(Math.min(highPrices.length-1, signals.size()-1));
    }

    @Override
    public StrategyVO predict(List<QuoteVO> quoteVOList) {
        double[] highPrices = new double[quoteVOList.size()];
        double[] lowPrices = new double[quoteVOList.size()];
        double[] closePrices = new double[quoteVOList.size()];
        double[] volumes = new double[quoteVOList.size()];

        for (int i = 0; i < quoteVOList.size(); i++) {
            QuoteVO quote = quoteVOList.get(i);
            highPrices[i] = quote.getHigh().doubleValue();
            lowPrices[i] = quote.getLow().doubleValue();
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
        double[] highPrices = {12,13,15,13,15,17,19,21,20,22,21,20,19,18,17,19,20,22,24,23};
        double[] lowPrices =  {10,11,12,11,13,15,16,18,18,19,19,18,16,15,15,17,18,19,21,20};

        System.out.println(new ParabolicSARStrategy().predict(highPrices, lowPrices));
    }
}