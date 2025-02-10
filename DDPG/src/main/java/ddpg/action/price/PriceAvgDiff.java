package ddpg.action.price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PriceAvgDiff {

    // 計算前半和後半的平均差
    public static BigDecimal calculateAverageDifference(List<BigDecimal> values) {
        int n = values.size();

        // 確保列表有足夠的數據
        if (n < 2) {
            throw new IllegalArgumentException("列表必須至少包含兩個元素");
        }

        int mid = n / 2;

        // 計算前半部分的平均值
        BigDecimal firstHalfSum = BigDecimal.ZERO;
        for (int i = 0; i < mid; i++) {
            firstHalfSum = firstHalfSum.add(values.get(i));
        }
        BigDecimal firstHalfAverage = firstHalfSum.divide(BigDecimal.valueOf(mid), RoundingMode.HALF_UP);

        // 計算後半部分的平均值
        BigDecimal secondHalfSum = BigDecimal.ZERO;
        for (int i = mid; i < n; i++) {
            secondHalfSum = secondHalfSum.add(values.get(i));
        }
        BigDecimal secondHalfAverage = secondHalfSum.divide(BigDecimal.valueOf(n - mid), RoundingMode.HALF_UP);

        // 計算總平均值
        BigDecimal totalSum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            totalSum = totalSum.add(value);
        }
        BigDecimal totalAverage = totalSum.divide(BigDecimal.valueOf(n), RoundingMode.HALF_UP);

        // 計算前後平均差並返回結果
        BigDecimal averageDifference = (secondHalfAverage.subtract(firstHalfAverage)).divide(totalAverage, RoundingMode.HALF_UP);

        return averageDifference;
    }

    public static void main(String[] args) {
        // 測試數據，使用 BigDecimal
        List<BigDecimal> values = List.of(
            new BigDecimal("120.5"),
            new BigDecimal("150.3"),
            new BigDecimal("130.2"),
            new BigDecimal("140.0"),
            new BigDecimal("110.1")
        );

        // 計算並輸出結果
        BigDecimal result = calculateAverageDifference(values);
        System.out.println("前後平均差 / 總平均 = " + result);
    }
}
