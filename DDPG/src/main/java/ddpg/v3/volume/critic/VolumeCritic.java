package ddpg.v3.volume.critic;

import java.util.Arrays;

// Critic 模型
public class VolumeCritic {

    public double[] weights; // 多維權重
    private int stateSize;    // 狀態維度

    public VolumeCritic(int stateSize) {
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

    public double getTdError(double reward, double gamma, double[] nextState, double[] state) {
        return getTdError(reward, gamma, predictValue(nextState), predictValue(state));
    }


    public static double getTdError(double reward, double gamma, double nextQValue, double currentQValue) {
        double tdError = reward + gamma * nextQValue - currentQValue; // TD 誤差計算
        if (Math.abs(tdError) > 1e10) {
//                System.err.println("tdError 超出範圍：" + tdError);
            tdError = Math.signum(tdError) * 1e10; // 限制在最大值
        }

        return tdError;
    }

    // 打印當前權重（用於調試）
    public void printWeights() {
        System.out.println("Critic Weights: " + Arrays.toString(weights));
    }
}

