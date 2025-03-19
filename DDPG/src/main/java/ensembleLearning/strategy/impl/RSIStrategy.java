package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.vo.StrategyVO;
import feature.RSITrading;

import java.util.List;

public class RSIStrategy implements Strategy {

    private int period = 14;

    public String predict(double[] prices) {
        // 生成交易信號
        List<String> signals = RSITrading.generateSignals(prices, period);
        return signals.get(prices.length-1);
    }

    public StrategyVO predict(List<QuoteVO> quoteVOList) {
        double[] prices = quoteVOList.stream()
            .mapToDouble(o -> o.getClose().doubleValue())
            .toArray();

        return StrategyVO.builder()
            .strategyName("RSIStrategy")
            .action(predict(prices))
            .closeTime(quoteVOList.getLast().getCloseTime())
            .build();
    }

    public static void main(String[] args) {
        // 示例數據 (收盤價)
        double[] prices = {
            45.1, 46.2, 44.8, 45.5, 47.0, 46.5, 48.2, 49.0, 48.5, 49.8,
            50.2, 49.5, 48.8, 47.2, 46.0, 47.5, 48.8, 49.2, 50.5, 51.2,
            50.8, 52.0, 53.5, 53.0, 52.5, 51.8, 52.2, 53.0, 52.8, 53.5
        };

        System.out.println(new RSIStrategy().predict(prices));
    }
}