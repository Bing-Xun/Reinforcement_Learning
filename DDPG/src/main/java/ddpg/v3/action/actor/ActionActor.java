package ddpg.v3.action.actor;

import ddpg.v3.util.Utils;

import java.util.Arrays;
import java.util.Random;

public class ActionActor {
    private double[][] weights; // 狀態到行動的權重
    private int stateSize;
    private int actionSize;
    private double epsilon = 0.3;  // ε-greedy 探索率
    static Random random = new Random();

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public ActionActor(int stateSize, int actionSize) {
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

    private double[] doEpsilon() {
        double[] actions = new double[actionSize + 1];  // 增加1个维度用于表示成交量比例

        Arrays.setAll(actions, i -> random.nextDouble());  // 初始化为随机值
        double sum = actions[0] + actions[1] + actions[2]; // 前三个值归一化
        for (int i = 0; i < 3; i++) {
            actions[i] /= sum;
        }
        actions[actionSize] = random.nextDouble();  // 随机生成成交量比例
        return actions;
    }

    public double[] predict(double[] state) {

        // 随机选择动作
        if (random.nextDouble() < epsilon) return doEpsilon();

        double[] actions = new double[actionSize + 1];  // 增加1个维度用于表示成交量比例

        // 计算线性得分
        double[] actionScores = new double[actionSize];
        for (int i = 0; i < actionSize; i++) {
            for (int j = 0; j < stateSize; j++) {
                actionScores[i] += state[j] * weights[j][i];
            }
            actionScores[i] = Math.tanh(actionScores[i]);  // 使用 tanh 激活函数
        }

        // 找出最大值，避免溢出
        double maxActionScore = Arrays.stream(actionScores).max().orElse(0.0);

        // 平移并计算指数
        double sumExp = 0.0;
        for (int i = 0; i < actionSize; i++) {
            actionScores[i] = Math.exp(actionScores[i] - maxActionScore);
            sumExp += actionScores[i];
        }

        // 归一化概率
        if (sumExp == 0.0) {
            Arrays.fill(actionScores, 1.0 / actionSize); // 默认为均匀分布
        } else {
            for (int i = 0; i < actionSize; i++) {
                actionScores[i] /= sumExp;
            }
        }

        // 将前三个分数作为动作的概率
        System.arraycopy(actionScores, 0, actions, 0, actionSize);

        // 添加成交量比例（独立输出）
        double volume = 0.0;
        for (int j = 0; j < stateSize; j++) {
            volume += state[j] * weights[j][actionSize-1];
        }
        actions[actionSize] = Math.tanh(volume);  // 归一化到 [-1, 1]
        actions[actionSize] = Math.abs(actions[actionSize]);  // 取绝对值归一化到 [0, 1]

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
