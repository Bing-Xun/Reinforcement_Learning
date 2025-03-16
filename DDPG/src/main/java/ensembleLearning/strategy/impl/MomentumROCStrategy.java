package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import feature.MACDTrading;
import feature.MomentumROCTrading;

import java.util.ArrayList;
import java.util.List;

public class MomentumROCStrategy implements Strategy {

    private int period = 10;

    public String predict(double[] prices) {
        double[] mtm = MomentumROCTrading.calculateMTM(prices, period); // 計算 ROC
        double[] roc = MomentumROCTrading.calculateROC(prices, period); // 計算 ROC
        List<String> mtmSignals = MomentumROCTrading.generateSignals(mtm, period); // 生成 MTM 交易信號
        List<String> rocSignals = MomentumROCTrading.generateSignals(roc, period); // 生成 ROC 交易信號
        String mtmAction = mtmSignals.get(Math.min(prices.length-1, mtmSignals.size()-1));
        String rocAction = rocSignals.get(Math.min(prices.length-1, rocSignals.size()-1));

        return mtmAction.equals(rocAction) ? mtmAction : "N/A"; // 若兩個動作相等則回傳, 不等回傳 N/A
    }

    public String predict(List<QuoteVO> quoteVOList) {
        double[] prices = quoteVOList.stream()
            .mapToDouble(o -> o.getClose().doubleValue())
            .toArray();
        return predict(prices);
    }

    public static void main(String[] args) {
        // 示例數據 (收盤價)
        double[] closePrices = {
            25, 26, 27, 26, 28, 29, 30, 29, 31, 32,
            31, 30, 29, 28, 27, 28, 29, 30, 31, 32,
            33, 34, 35, 34, 33, 32, 31, 30, 29, 28
        };

        System.out.println(new MomentumROCStrategy().predict(closePrices));
    }
}
