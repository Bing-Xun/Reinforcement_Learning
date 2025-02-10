package ddpg.action.actor;


import lstm.SimpleLSTM;

import java.util.Arrays;

public class Actor {
    private static Double learningRate = 0.01;  // 学习率
    private static Integer stateSize;  // 状态空间大小
    private static Integer actionSize;  // 动作空间大小
    private Double[][] actorWeights; // Actor 的权重和偏差
    private SimpleLSTM lstm = new SimpleLSTM();

    public Actor(Integer stateSize, Integer actionSize) {
        this.stateSize = stateSize;
        this.actionSize = actionSize;
        this.actorWeights = new Double[stateSize][actionSize];

        // 初始化 Actor 的权重
        for (int i = 0; i < stateSize; i++) {
            for (int j = 0; j < actionSize; j++) {
                actorWeights[i][j] = Math.random() * 0.1 - 0.05; // 随机值在 [-0.05, 0.05]
            }
        }
    }

    public void setLearningRate(Double learningRate) {
        this.learningRate = learningRate;
    }

    // Actor 預測趨勢概率的範圍 [-1, 1]
    public Double predict(Double[] state) {
        double[] h = lstm.predict(Arrays.stream(state).mapToDouble(Double::doubleValue).toArray());
        return lstm.forwardH(h);
    }

    public Double train(Double[] state) {
        double[] h = lstm.forward(Arrays.stream(state).mapToDouble(Double::doubleValue).toArray());
        return lstm.forwardH(h);
    }

    // 更新 Actor 权重
    public void updateWeights(Double[] state, Double tdError) {
        double[] dL_dh = lstm.getDL_dh(tdError);
        lstm.backward(Arrays.stream(state).mapToDouble(Double::doubleValue).toArray(), dL_dh);
    }

    // 更新 Actor 权重
    public void updateWeights(Double[] state, Double[] tdError) {
        lstm.backward(Arrays.stream(state).mapToDouble(Double::doubleValue).toArray()
            , Arrays.stream(tdError).mapToDouble(Double::doubleValue).toArray());
    }
}
