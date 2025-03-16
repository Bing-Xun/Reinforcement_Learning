package ddpg.action.critic;

import java.util.Arrays;

// Critic 模型
public class Critic {

    private Double gamma = 0.8; // 折扣因子
    private Double learningRate = 0.9; // 学习率
    private double[][] weights; // 神经网络的权重，假设是二维权重

    public Critic(int inputSize, int outputSize) {
        // 初始化权重
        weights = new double[inputSize][outputSize];
        // 在这里可以初始化权重的具体值
    }

    /**
     * 计算 Temporal Difference (TD) 误差
     *
     * @param reward 即时奖励 r
     * @param currentQ 当前状态动作的 Q 值 Q(s, a)
     * @param nextQ 下一状态动作的 Q 值 Q(s', a')
     * @return TD 误差
     */
    public double calculateTDError(double currentQ, double nextQ) {
        double reward = 0;
        // TD误差 = reward + gamma * nextQ - currentQ
        double tdError = reward + gamma * nextQ - currentQ;
        return tdError;
    }

    /**
     * 通过神经网络的前向传播来预测 Q 值
     *
     * @param state 状态
     * @return Q 值
     */
    public double forward(double[] state) {
        // 假设是一个简单的前向传递，计算 Q 值
        double qValue = 0.0;
        for (int i = 0; i < state.length; i++) {
            qValue += state[i] * weights[i][0]; // 简单的加权求和
        }
        return qValue;
    }

    /**
     * 通过 TD 误差来更新 Critic 的权重
     *
     * @param state 状态
     * @param tdError TD 误差
     */
    public void updateWeights(double[] state, double tdError) {
        // 基于 TD 误差更新权重
        for (int i = 0; i < state.length; i++) {
            weights[i][0] += learningRate * tdError * state[i];
        }
    }
}


