package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import feature.EMATrading;
import feature.MACDTrading;

import java.util.List;

public class EMAStrategy implements Strategy {

    private int shortPeriod = 5;
    private int longPeriod = 12;

    public String predict(double[] prices) {
        // 生成交易信號
        List<String> signals = EMATrading.generateSignals(prices, shortPeriod, longPeriod);
        return signals.get(prices.length-1);
    }

    public String predict(List<QuoteVO> quoteVOList) {
        double[] prices = quoteVOList.stream()
            .mapToDouble(o -> o.getClose().doubleValue())
            .toArray();
        return predict(prices);
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
