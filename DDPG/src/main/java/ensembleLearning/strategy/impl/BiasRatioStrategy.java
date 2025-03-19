package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.vo.StrategyVO;
import feature.BiasRatioTrading;

import java.util.List;

//public class BiasRatioStrategy implements Strategy {
public class BiasRatioStrategy {


    private int period = 12;
    private double buyThreshold = -4; // 根據市場情況和回測調整
    private double sellThreshold = 4;  // 根據市場情況和回測調整

    public String predict(double[] prices) {
        // 生成交易信號
        List<String> signals = BiasRatioTrading.generateSignals(prices, period, buyThreshold, sellThreshold);
        return signals.get(Math.min(prices.length-1,signals.size()-1));
    }

    public StrategyVO predict(List<QuoteVO> quoteVOList) {
        double[] prices = quoteVOList.stream()
            .mapToDouble(o -> o.getClose().doubleValue())
            .toArray();

        return StrategyVO.builder()
            .strategyName("BiasRatioStrategy")
            .action(predict(prices))
            .closeTime(quoteVOList.getLast().getCloseTime())
            .build();
    }

    public static void main(String[] args) {
        // 示例數據 (收盤價)
        double[] closePrices = {
            25, 26, 27, 26, 28, 29, 30, 29, 31, 32,
            31, 30, 29, 28, 27, 28, 29, 30, 31, 32,
            33, 34, 35, 34, 33, 32, 31, 30, 29, 28
        };

        System.out.println(new BiasRatioStrategy().predict(closePrices));
    }
}