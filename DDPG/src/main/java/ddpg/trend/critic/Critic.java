package ddpg.trend.critic;

import java.util.Arrays;

// Critic 模型
public class Critic {

    private Double gamma = 0.8; // 折扣因子
    private Double learningRate= 0.9; // 学习率

    /**
     * 计算 Temporal Difference (TD) 误差
     *
     * @param reward 即时奖励 r
     * @param currentQ 当前状态动作的 Q 值 Q(s, a)
     * @param nextQ 下一状态动作的 Q 值 Q(s', a')
     * @return TD 误差
     */
    public Double[] calculateTDError(Double reward, Double[] currentQ, Double[] nextQ) {
        // 校验输入
        if (currentQ.length != nextQ.length) {
            throw new IllegalArgumentException("currentQ 和 nextQ 数组必须具有相同的长度");
        }

        // 创建一个数组来存储每个状态-动作对的 TD 误差
        Double[] tdErrors = new Double[currentQ.length];

        for (int i = 0; i < currentQ.length; i++) {
            // 计算每个状态-动作对的 TD 误差
            double nextQMax = Arrays.stream(nextQ).max(Double::compareTo).orElse(0.0);  // 获取 nextQ 中的最大值
            tdErrors[i] = reward + gamma * nextQMax - currentQ[i];
        }

        return tdErrors;
    }

    public Double calculateTDError(Double reward, Double currentQ, Double nextQ) {
        // 获取 nextQ 中的最大值（假设 nextQ 是一个单一的值）
        double nextQMax = nextQ;  // 这里只取 nextQ 作为最大值，因为 nextQ 只有一个元素

        // 计算 TD 误差
        double tdError = reward + gamma * nextQMax - currentQ;

        return tdError;  // 返回单一的 TD 误差
    }

    /**
     * 更新 Critic 的 Q 值
     *
     * @param currentQ 当前状态动作的 Q 值 Q(s, a)
     * @param tdError Temporal Difference (TD) 误差
     * @return 更新后的 Q 值
     */
    public Double[] updateQValues(Double[] currentQ, Double[] tdError) {
        // 校验输入
        if (currentQ.length != tdError.length) {
            System.out.println("currentQ.length:"+currentQ.length);
            System.out.println("tdError.length:"+tdError.length);
            throw new IllegalArgumentException("currentQ 和 tdError 数组必须具有相同的长度");
        }

        // 创建一个新的数组来存储更新后的 Q 值
        Double[] updatedQ = new Double[currentQ.length];

        // 遍历每个 Q 值，进行更新
        for (int i = 0; i < currentQ.length; i++) {
            updatedQ[i] = currentQ[i] + learningRate * tdError[i]; // 根据 TD 误差更新 Q 值
        }

        return updatedQ; // 返回更新后的 Q 值数组
    }


//    public static void main(String[] args) {
//        // 示例参数
//        double gamma = 0.8; // 折扣因子
//        double learningRate = 0.01; // Critic 学习率
//        Critic critic = new Critic();
//
//        double reward = 10.0; // 即时奖励
//        double currentQ = 5.0; // 当前 Q 值
//        double nextQ = 7.0; // 下一步的 Q 值
//
//        // 计算 TD 误差
//        double tdError = critic.calculateTDError(reward, currentQ, nextQ);
//        System.out.println("TD Error: " + tdError);
//
//        // 更新 Q 值
//        double updatedQ = critic.updateQValue(currentQ, tdError);
//        System.out.println("Updated Q Value: " + updatedQ);
//    }
}

