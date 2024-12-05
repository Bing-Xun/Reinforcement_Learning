package ddpg.v2.actor;

import java.util.Arrays;
import java.util.Random;

public class DirectionActor {
    private double[][] weights; // 狀態到行動的權重
    private int stateSize, actionSize;
    private double epsilon = 0.2;  // ε-greedy 探索率
    static Random random = new Random();

    public DirectionActor(int stateSize, int actionSize) {
        this.stateSize = stateSize;
        this.actionSize = actionSize;
        this.weights = new double[stateSize][actionSize];
        // 初始化權重
        for (int i = 0; i < stateSize; i++) {
            for (int j = 0; j < actionSize; j++) {
                weights[i][j] = random.nextDouble() * 0.1;
            }
        }
    }

    public double[] predict(double[] state) {
        if (random.nextDouble() < epsilon) {
            double[] randomNumbers = new double[actionSize];
            Random random = new Random();

            // 使用 Arrays.setAll 填充隨機數
            Arrays.setAll(randomNumbers, i -> random.nextDouble());
            return randomNumbers;
        }

        double[] actions = new double[actionSize];
        double sumExp = 0.0;

        // 計算線性得分
        for (int i = 0; i < actionSize; i++) {
            for (int j = 0; j < stateSize; j++) {
                actions[i] += state[j] * weights[j][i];
            }
        }

        // 找出最大值，避免溢出
        double maxAction = Arrays.stream(actions).max().orElse(0.0);

        // 平移並計算指數
        for (int i = 0; i < actionSize; i++) {
            actions[i] = Math.exp(actions[i] - maxAction);
            sumExp += actions[i];
        }

        // 檢查 sumExp 並進行歸一化
        if (sumExp == 0.0) {
            Arrays.fill(actions, 1.0 / actionSize); // 默認為均勻分布
        } else {
            for (int i = 0; i < actionSize; i++) {
                actions[i] /= sumExp;
            }
        }

        return actions;
    }

    public void updateWeights(double[] state, double[] actionProbs, double tdError, double learningRate) {
        for (int i = 0; i < stateSize; i++) {
            for (int j = 0; j < actionSize; j++) {
                // 策略梯度更新
                double gradient = (actionProbs[j] - (j == getMaxIndex(actionProbs) ? 1.0 : 0.0)) * state[i];
                weights[i][j] += learningRate * tdError * gradient;
            }
        }
    }

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
}
