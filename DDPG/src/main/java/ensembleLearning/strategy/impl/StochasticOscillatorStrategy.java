package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.vo.StrategyVO;
import feature.StochasticOscillatorTrading;

import java.util.List;

public class StochasticOscillatorStrategy implements Strategy {

    private String strategyName = "StochasticOscillatorStrategy";
    private int kPeriod = 9;
    private int dPeriod = 3;
    private int slowing = 3; // 1 for fast stochastic, > 1 for slow stochastic

    public String predict(double[] highPrices, double[] lowPrices, double[] closePrices) {
        List<String> signals = StochasticOscillatorTrading.generateSignals(highPrices, lowPrices, closePrices, kPeriod, dPeriod, slowing);
        return signals.get(closePrices.length-1);
    }

    @Override
    public StrategyVO predict(List<QuoteVO> quoteVOList) {
        double[] highPrices = new double[quoteVOList.size()];
        double[] lowPrices = new double[quoteVOList.size()];
        double[] closePrices = new double[quoteVOList.size()];

        for (int i = 0; i < quoteVOList.size(); i++) {
            QuoteVO quote = quoteVOList.get(i);
            highPrices[i] = quote.getHigh().doubleValue();
            lowPrices[i] = quote.getLow().doubleValue();
            closePrices[i] = quote.getClose().doubleValue();
        }

        return StrategyVO.builder()
            .strategyName(strategyName)
            .action(predict(highPrices, lowPrices, closePrices))
            .closeTime(quoteVOList.getLast().getCloseTime())
            .build();
    }

    @Override
    public String getStrategyName() {
        return strategyName;
    }

    public static void main(String[] args) {
        // 示例數據
        double[] highPrices = {25, 26, 27, 26, 28, 29, 30, 29, 31, 32, 31, 30, 29, 28, 27, 28, 29, 30, 31, 32};
        double[] lowPrices = {23, 24, 25, 24, 26, 27, 28, 27, 29, 30, 29, 28, 27, 26, 25, 26, 27, 28, 29, 30};
        double[] closePrices = {24, 25.5, 26.5, 25.5, 27, 28.5, 29.5, 28.5, 30, 31.5, 30.5, 29.5, 28.5, 27.5, 26.5, 27.5, 28.5, 29.5, 30.5, 31.5};

        System.out.println(new StochasticOscillatorStrategy().predict(highPrices, lowPrices, closePrices));
    }
}
