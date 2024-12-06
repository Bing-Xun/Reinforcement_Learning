package ddpg.v2.indicators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalculateKD {

    private static final BigDecimal alpha = BigDecimal.valueOf(1.0 / 3.0); // 平滑系数

    /**
     *
     * @param prices 價格
     * @param period 計算週期 看要kd幾
     * @return
     */
    public static List<double[]> calculateKDJ(List<BigDecimal> prices, int period) {
        // 用于保存 KDJ 值
        List<double[]> kdjValues = new ArrayList<>();
        BigDecimal prevK = BigDecimal.valueOf(50); // 初始化K，通常为50
        BigDecimal prevD = BigDecimal.valueOf(50); // 初始化D，通常为50

        // 计算最高价和最低价列表
        List<BigDecimal> highestPrices = new ArrayList<>();
        List<BigDecimal> lowestPrices = new ArrayList<>();

        for (int i = period - 1; i < prices.size(); i++) {
            List<BigDecimal> window = prices.subList(i - period + 1, i + 1);
            highestPrices.add(Collections.max(window));
            lowestPrices.add(Collections.min(window));
        }

        // 计算KDJ
        for (int i = period - 1; i < prices.size(); i++) {
            BigDecimal close = prices.get(i);
            BigDecimal highest = highestPrices.get(i - period + 1);
            BigDecimal lowest = lowestPrices.get(i - period + 1);

            // 计算RSV
            BigDecimal rsv = (close.subtract(lowest)).divide(highest.subtract(lowest), 10, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));

            // 使用平滑系数更新K和D值
            BigDecimal k = prevK.multiply(BigDecimal.valueOf(1).subtract(alpha)).add(rsv.multiply(alpha));
            BigDecimal d = prevD.multiply(BigDecimal.valueOf(1).subtract(alpha)).add(k.multiply(alpha));

            // 计算J值
            BigDecimal j = k.multiply(BigDecimal.valueOf(3)).subtract(d.multiply(BigDecimal.valueOf(2)));

            // 存储K、D、J值
            kdjValues.add(new double[]{k.doubleValue(), d.doubleValue(), j.doubleValue()});

            // 更新前一个K和D值
            prevK = k;
            prevD = d;
        }
        return kdjValues;
    }
}
