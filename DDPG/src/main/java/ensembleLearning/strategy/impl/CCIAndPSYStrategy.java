package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.vo.StrategyVO;
import feature.CCIAndPSYTrading;

import java.util.List;

public class CCIAndPSYStrategy implements Strategy {

    private String strategyName = "CCIAndPSYStrategy";
    private int psyPeriod = 12;
    private double psyBuyThreshold = 25;
    private double psySellThreshold = 75;

    public String predict(double[] prices) {
        List<String> psySignals = CCIAndPSYTrading.generatePSYSignals(prices, psyPeriod, psyBuyThreshold, psySellThreshold);
        return psySignals.get(Math.min(prices.length-1, psySignals.size() -1));
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
        // 示例數據
        double[] closePrices = {
            25, 26, 27, 26, 28, 29, 30, 29, 31, 32,
            31, 30, 29, 28, 27, 28, 29, 30, 31, 32,
            33, 34, 35, 34, 33, 32, 31, 30, 29, 28
        };

        System.out.println(new CCIAndPSYStrategy().predict(closePrices));
    }
}