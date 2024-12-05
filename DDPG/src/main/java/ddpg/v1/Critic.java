package ddpg.v1;

import java.util.Arrays;

// Critic 模型
public class Critic {

    public double[] weights; // 多維權重
    private int stateSize;    // 狀態維度

    public Critic(int stateSize) {
        this.stateSize = stateSize;
        this.weights = new double[stateSize];
        // 初始化權重（可以根據需求調整初始值）
        for (int i = 0; i < stateSize; i++) {
            weights[i] = 0.1; // 初始權重
        }
    }

    // 根據狀態預測價值
    public double predictValue(double[] state) {
        double value = 0.0;
        for (int i = 0; i < state.length; i++) {
            value += state[i] * weights[i]; // 加權求和
        }
        return value;
    }

    // 計算 TD(0) 預測值
    public double evaluate(double reward, double gamma, double nextValue) {
        return reward + gamma * nextValue; // TD(0) 更新
    }

    // 更新 Critic 權重
    public void updateWeights(double[] state, double tdError, double learningRate) {
        for (int i = 0; i < weights.length; i++) {
            weights[i] += learningRate * tdError * state[i]; // 基於 TD 誤差更新權重
        }
    }

    // 打印當前權重（用於調試）
    public void printWeights() {
        System.out.println("Critic Weights: " + Arrays.toString(weights));
    }
}

