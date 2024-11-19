package ddpg;

// Critic 模型
public class Critic {
    private double weight = 0.1; // Critic 權重，影響預測的強度

    public double predictValue(double[] state) {
        // 根據狀態預測價值，這裡簡單示範：價格 * 權重
        return state[0] * weight;
    }

    public double evaluate(double reward, double gamma, double nextValue) {
        // 計算 TD(0) 預測值：V(s) = R + γ * V(s')
        return reward + gamma * nextValue;
    }

    public void update(double tdError, double learningRate) {
        // 更新 Critic 權重
        weight += tdError * learningRate;
    }
}

