package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import feature.BollingerBandsTrading;
import feature.CCIAndPSYTrading;

import java.util.ArrayList;
import java.util.List;

public class CCIAndPSYStrategy implements Strategy {

    private int psyPeriod = 12;
    private double psyBuyThreshold = 25;
    private double psySellThreshold = 75;

    public String predict(double[] prices) {
        List<String> psySignals = CCIAndPSYTrading.generatePSYSignals(prices, psyPeriod, psyBuyThreshold, psySellThreshold);
        return psySignals.get(Math.min(prices.length-1, psySignals.size() -1));
    }

    public String predict(List<QuoteVO> quoteVOList) {
        double[] prices = quoteVOList.stream()
            .mapToDouble(o -> o.getClose().doubleValue())
            .toArray();
        return predict(prices);
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