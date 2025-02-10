package util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Utils {

    public static Double mapNonLinearRangeV = 10.0;

    public static Integer getMaxIndex(Double[] array) {
        int maxIndex = 0; // 假設第一個元素是最大值
        double maxValue = array[0];

        // 從第二個元素開始比較
        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i; // 更新最大值的索引
            }
        }
        return maxIndex; // 返回最大值的索引
    }

    public static Double mapNonLinear(Double value, Double minValue, Double maxValue) {
        minValue = value < minValue ? value : minValue;
        maxValue = value > maxValue ? value : maxValue;
        return mapNonLinear(value, minValue, maxValue, -mapNonLinearRangeV, mapNonLinearRangeV);
    }

    private static double mapNonLinear(Double value, Double minValue, Double maxValue, Double newMin, Double newMax) {
        // 步骤 1: 线性映射到 [0, 1]
        Double normalizedValue = (value - minValue) / (maxValue - minValue);

        // 步骤 2: 使用 tanh 进行非线性变换
        Double tanhValue = Math.tanh(normalizedValue); // 使用tanh进行非线性变换

        // 步骤 3: 将 tanh 的结果映射到目标区间 [-10, 10]
        return newMin + tanhValue * (newMax - newMin); // 调整到 [-10, 10] 区间
    }

    /**
     * 计算指定百分比范围的平均值
     *
     * @param values  数据列表
     * @param percent 百分比（范围 0 - 100）
     * @param isBottom 如果为 true，计算前百分比；否则计算后百分比
     * @return 指定范围的平均值
     */
    public static double calculatePercentileAverage(List<Double> values, double percent, boolean isBottom) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("输入列表不能为空");
        }
        if (percent <= 0 || percent > 100) {
            throw new IllegalArgumentException("百分比必须在 (0, 100] 范围内");
        }

        // 对列表排序
        Collections.sort(values);

        int size = values.size();
        int limit = (int) Math.ceil(size * percent / 100.0); // 确定元素数量，向上取整以确保至少一个元素
        double sum = 0.0;

        if (isBottom) {
            // 计算前 N% 的平均值
            for (int i = 0; i < limit; i++) {
                sum += values.get(i);
            }
        } else {
            // 计算后 N% 的平均值
            for (int i = size - limit; i < size; i++) {
                sum += values.get(i);
            }
        }

        return sum / limit;
    }
}
