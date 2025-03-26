package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.vo.HighLowTradingVO;
import ensembleLearning.strategy.vo.StrategyVO;
import feature.HighLowTrading;

import java.util.List;

public class HighLowStrategy implements Strategy {

    private String strategyName = "HighLowStrategy";

    private HighLowTradingVO predict(double[] prices) {
        // 生成交易信號
        HighLowTradingVO signals = HighLowTrading.generateSignals(prices);
        return signals;
    }

    @Override
    public StrategyVO predict(List<QuoteVO> quoteVOList) {
        double[] prices = quoteVOList.stream()
                .mapToDouble(o -> o.getClose().doubleValue())
                .toArray();

        HighLowTradingVO vo = predict(prices);

        return StrategyVO.builder()
                .strategyName(strategyName)
                .action(vo.getAction())
                .weight(vo.getWeight())
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

        System.out.println(new BiasRatioStrategy().predict(closePrices));
    }
}
