package ddpg.v3.util;

import java.util.Arrays;
import java.util.List;

public class Utils {

    public static int mapNonLinearRangeV = 10;

    public static int getMaxIndex(double[] array) {
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

    public static double mapNonLinear(double value, double minValue, double maxValue) {
        minValue = value < minValue ? value : minValue;
        maxValue = value > maxValue ? value : maxValue;
        return mapNonLinear(value, minValue, maxValue, -mapNonLinearRangeV, mapNonLinearRangeV);
    }

    private static double mapNonLinear(double value, double minValue, double maxValue, double newMin, double newMax) {
        // 步骤 1: 线性映射到 [0, 1]
        double normalizedValue = (value - minValue) / (maxValue - minValue);

        // 步骤 2: 使用 tanh 进行非线性变换
        double tanhValue = Math.tanh(normalizedValue); // 使用tanh进行非线性变换

        // 步骤 3: 将 tanh 的结果映射到目标区间 [-10, 10]
        return newMin + tanhValue * (newMax - newMin); // 调整到 [-10, 10] 区间
    }

    public static List<Double> toDoubleList(double[] arr) {
        // 将 double[] 转为 Double[]
        Double[] boxedArr = Arrays.stream(arr).boxed().toArray(Double[]::new);

        // 使用 Arrays.asList
        return Arrays.asList(boxedArr);
    }


    /**
     * 非線性壓縮函數 (tanh)
     *
     * @param x 輸入值
     * @param k 曲線陡峭程度
     * @return 壓縮後的值 [-1, 1]
     *
     * k=5.0），壓縮曲線會更加陡峭，靠近 0 的值會被壓縮得更快。
     * k=0.5），壓縮曲線會更加平緩，壓縮效果不明顯。
     */
    public static double tanh(double x, double k) {
        return (Math.exp(k * x) - Math.exp(-k * x)) / (Math.exp(k * x) + Math.exp(-k * x));
    }

}
