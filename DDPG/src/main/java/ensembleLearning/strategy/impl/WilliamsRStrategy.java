package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.vo.StrategyVO;
import feature.WilliamsRTrading;

import java.util.List;

public class WilliamsRStrategy implements Strategy {

    private String strategyName = "WilliamsRStrategy";
    private int period = 14;

    public String predict(double[] highPrices, double[] lowPrices, double[] closePrices) {
        List<String> signals = WilliamsRTrading.generateSignals(highPrices, lowPrices, closePrices, period);
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
        // 示例數據 (收盤價)
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

        System.out.println(new WilliamsRStrategy().predict(highPrices, lowPrices, closePrices));
    }
}