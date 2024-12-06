package ddpg.v2.util;

public class Utils {

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
        return mapNonLinear(value, minValue, maxValue, -10, 10);
    }

    private static double mapNonLinear(double value, double minValue, double maxValue, double newMin, double newMax) {
        // 步骤 1: 线性映射到 [0, 1]
        double normalizedValue = (value - minValue) / (maxValue - minValue);

        // 步骤 2: 使用 tanh 进行非线性变换
        double tanhValue = Math.tanh(normalizedValue); // 使用tanh进行非线性变换

        // 步骤 3: 将 tanh 的结果映射到目标区间 [-10, 10]
        return newMin + (tanhValue + 1) * (newMax - newMin) / 2; // 调整到 [-10, 10] 区间
    }
}
