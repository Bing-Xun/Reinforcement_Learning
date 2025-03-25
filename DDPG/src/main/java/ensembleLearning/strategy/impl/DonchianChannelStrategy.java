package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.vo.StrategyVO;
import feature.DonchianChannelTrading;

import java.util.List;

//public class DonchianChannelStrategy implements Strategy {
public class DonchianChannelStrategy {

    private String strategyName = "EMAStrategy";
    private int period = 20;

    public String predict(double[] highPrices, double[] lowPrices, double[] closePrices) {
        List<String> signals = DonchianChannelTrading.generateSignals(highPrices, lowPrices, closePrices, period);
        return signals.get(closePrices.length-1);
    }


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
            .strategyName("DonchianChannelStrategy")
            .action(predict(highPrices, lowPrices, closePrices))
            .closeTime(quoteVOList.getLast().getCloseTime())
            .build();
    }

    public String getStrategyName() {
        return strategyName;
    }

    public static void main(String[] args) {
        // 示例數據
        double[] highPrices = {12, 13, 15, 13, 15, 17, 19, 21, 20, 22, 21, 20, 19, 18, 17, 19, 20, 22, 24, 23, 25, 24, 22, 23, 21, 20, 18, 19, 17, 16};
        double[] lowPrices = {10, 11, 12, 11, 13, 15, 16, 18, 18, 19, 19, 18, 16, 15, 15, 17, 18, 19, 21, 20, 22, 21, 20, 21, 19, 18, 16, 17, 15, 14};
        double[] closePrices = {11, 12.5, 14, 13.5, 14.5, 16.5, 18, 20, 19.5, 21, 20.5, 19.5, 18.5, 17.5, 18, 18.5, 19, 21, 23, 22, 24, 23, 21, 22, 20, 19, 17, 18, 16, 15.5};

        System.out.println(new DonchianChannelStrategy().predict(highPrices, lowPrices, closePrices));
    }
}