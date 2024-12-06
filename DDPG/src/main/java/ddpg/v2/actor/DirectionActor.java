package ddpg.v2.actor;

import ddpg.v2.util.Utils;

import java.util.Arrays;
import java.util.Random;

public class DirectionActor {
    private double[][] weights; // 狀態到行動的權重
    private int stateSize, actionSize;
    private double epsilon = 0.3;  // ε-greedy 探索率
    static Random random = new Random();

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

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
            actions[i] = Math.tanh(actions[i]);  // 使用 tanh 激活函数
        }

        // 找出最大值，避免溢出
        double maxAction = Arrays.stream(actions).max().orElse(0.0);

        // 平移並計算指數
        for (int i = 0; i < actionSize; i++) {
            // 先加小常数避免对零取对数
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

                double gradient = (actionProbs[j] - (j == Utils.getMaxIndex(actionProbs) ? 1.0 : 0.0)) * state[i];
                weights[i][j] += learningRate * tdError * gradient;
            }
        }
    }

    public double[][] getWeights() {
        System.out.println(Arrays.deepToString(weights));
        return weights;
    }
}
