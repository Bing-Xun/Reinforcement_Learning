package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.vo.StrategyVO;
import feature.ChaikinOscillatorTrading;

import java.util.List;

public class ChaikinOscillatorStrategy implements Strategy {  // 2
//public class ChaikinOscillatorStrategy {

    private String strategyName = "ChaikinOscillatorStrategy";

    private int fastPeriod = 3;
    private int slowPeriod = 10;
    private int maPeriod = 9; // 用於判斷突破的移動平均線週期

    public String predict(double[] highPrices, double[] lowPrices, double[] closePrices, double[] volumes) {
        List<String> signals = ChaikinOscillatorTrading.generateSignals(highPrices, lowPrices, closePrices, volumes, fastPeriod, slowPeriod, maPeriod);
        return signals.get(Math.min(closePrices.length-1,signals.size()-1));
    }

    public StrategyVO predict(List<QuoteVO> quoteVOList) {
        double[] highPrices = new double[quoteVOList.size()];
        double[] lowPrices = new double[quoteVOList.size()];
        double[] closePrices = new double[quoteVOList.size()];
        double[] volumes = new double[quoteVOList.size()];

        for (int i = 0; i < quoteVOList.size(); i++) {
            QuoteVO quote = quoteVOList.get(i);
            highPrices[i] = quote.getHigh().doubleValue();
            lowPrices[i] = quote.getLow().doubleValue();
            closePrices[i] = quote.getClose().doubleValue();
            volumes[i] = quote.getVolume().doubleValue();
        }

        return StrategyVO.builder()
            .strategyName(strategyName)
            .action(predict(highPrices, lowPrices, closePrices, volumes))
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
        double[] highPrices = new double[closePrices.length];
        double[] lowPrices = new double[closePrices.length];
        // 這裡只是為了方便, 實務上要給值
        for(int i = 0; i< closePrices.length; ++i)
        {
            highPrices[i] = closePrices[i] * 1.05;
            lowPrices[i] = closePrices[i] * 0.95;
        }
        double[] volumes = {
            1000, 1200, 1500, 900, 1800, 2000, 1600, 1400, 2200, 2500,
            1800, 1500, 1200, 1000, 800, 1100, 1300, 1700, 1900, 2100,
            2300, 2600, 2800, 2500, 2000, 1800, 1500, 1300, 1100, 900
        };

        System.out.println(new ChaikinOscillatorStrategy().predict(highPrices, lowPrices, closePrices, volumes));
    }
}