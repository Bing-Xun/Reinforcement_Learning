package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.vo.StrategyVO;
import feature.EMATrading;

import java.util.List;

public class EMAStrategy implements Strategy {

    private String strategyName = "EMAStrategy";
    private int shortPeriod = 5;
    private int longPeriod = 12;

    public String predict(double[] prices) {
        // 生成交易信號
        List<String> signals = EMATrading.generateSignals(prices, shortPeriod, longPeriod);
        return signals.get(prices.length-1);
    }

    @Override
    public StrategyVO predict(List<QuoteVO> quoteVOList) {
        double[] prices = quoteVOList.stream()
            .mapToDouble(o -> o.getClose().doubleValue())
            .toArray();

        return StrategyVO.builder()
            .strategyName(strategyName)
            .action(predict(prices))
            .closeTime(quoteVOList.getLast().getCloseTime())
            .build();
    }

    @Override
    public String getStrategyName() {
        return strategyName;
    }

    public static void main(String[] args) {
        // 示例數據 (收盤價)
        double[] prices = {
            10, 12, 15, 14, 16, 18, 20, 19, 22, 25,
            24, 26, 28, 27, 29, 31, 30, 28, 26, 24,
            25, 23, 21, 22, 20, 19, 17, 18, 16, 15
        };

        System.out.println(new MACDStrategy().predict(prices));
    }
}
