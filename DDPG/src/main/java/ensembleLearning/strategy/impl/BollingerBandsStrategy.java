package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import feature.BiasRatioTrading;
import feature.BollingerBandsTrading;

import java.util.ArrayList;
import java.util.List;

public class BollingerBandsStrategy implements Strategy {

    private int period = 20;
    private double k = 2.0; // 標準差倍數

    public String predict(double[] prices) {
        List<String> signals = BollingerBandsTrading.generateSignals(prices, period, k);
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

        System.out.println(new BollingerBandsStrategy().predict(prices));
    }
}