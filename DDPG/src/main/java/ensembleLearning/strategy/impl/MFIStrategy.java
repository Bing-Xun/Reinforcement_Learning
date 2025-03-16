package ensembleLearning.strategy.impl;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import feature.MFITrading;
import feature.RSITrading;

import java.util.ArrayList;
import java.util.List;

public class MFIStrategy implements Strategy {

    private int period = 14;

    public String predict(double[] highPrices, double[] lowPrices, double[] closePrices, double[] volumes) {
        List<String> signals = MFITrading.generateSignals(highPrices, lowPrices, closePrices, volumes, period);
        return signals.get(closePrices.length-1);
    }

    public String predict(List<QuoteVO> quoteVOList) {
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

        return predict(highPrices, lowPrices, closePrices, volumes);
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
        for (int i = 0; i < closePrices.length; ++i) {
            highPrices[i] = closePrices[i] * 1.05;
            lowPrices[i] = closePrices[i] * 0.95;
        }
        double[] volumes = {
            1000, 1200, 1500, 900, 1800, 2000, 1600, 1400, 2200, 2500,
            1800, 1500, 1200, 1000, 800, 1100, 1300, 1700, 1900, 2100,
            2300, 2600, 2800, 2500, 2000, 1800, 1500, 1300, 1100, 900
        };

        System.out.println(new MFIStrategy().predict(highPrices, lowPrices, closePrices, volumes));
    }
}