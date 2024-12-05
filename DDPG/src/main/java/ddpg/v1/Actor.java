package ddpg.v1;

// Actor 模型
public class Actor {
    // 簡單模擬一個權重，用於計算行動值
    private double[][] weights;
    private int actionSize;

    public Actor(int stateSize, int actionSize) {
        this.weights = new double[actionSize][stateSize];
        // 初始化每個行動的權重為隨機值
        for (int i = 0; i < actionSize; i++) {
            for (int j = 0; j < stateSize; j++) {
                weights[i][j] = Math.random() * 0.1; // 小範圍初始化
            }
        }

        this.actionSize = actionSize;
    }

    // Getter 用於測試和外部訪問
    public double[][] getWeights() {
        return weights;
    }

    // 設置權重（可用於重置或測試）
    public void setWeights(double[][] newWeights) {
        this.weights = newWeights;
    }

    public double selectAction(double[] state) {
        double[] actionProbabilities = new double[3]; // 假設有三個可能的行動: SELL, HOLD, BUY

        // 計算每個行為的加權和
        double[] actionValues = new double[3];
        for (int i = 0; i < state.length; i++) {
            actionValues[0] += state[i] * weights[0][i]; // SELL
            actionValues[1] += state[i] * weights[1][i]; // HOLD
            actionValues[2] += state[i] * weights[2][i]; // BUY
        }

        // Softmax 計算概率分佈
        double sumExp = 0.0;
        for (int i = 0; i < actionValues.length; i++) {
            actionValues[i] = Math.exp(actionValues[i]); // 計算 e^(actionValue)
            sumExp += actionValues[i];
        }

        for (int i = 0; i < actionProbabilities.length; i++) {
            actionProbabilities[i] = actionValues[i] / sumExp; // 計算概率
        }

        // 根據概率選擇行動
        double randomValue = Math.random();
        if (randomValue < actionProbabilities[0]) {
            return 0.0; // SELL
        } else if (randomValue < actionProbabilities[0] + actionProbabilities[1]) {
            return 1.0; // HOLD
        } else {
            return 2.0; // BUY
        }
    }

    public void updateWeights(double[] state, double tdError, double learningRate) {
        // 計算每個行為的加權和
        double[] actionProbabilities = new double[actionSize]; // 存放每個行為的選擇概率
        double sumExp = 0.0;
        double[] actions = new double[actionSize]; // 存放每個行為的得分

        // 計算每個行為的得分，這裡使用線性模型
        for (int i = 0; i < actionSize; i++) {
            actions[i] = 0.0; // 初始化每個行為的得分
            for (int j = 0; j < state.length; j++) {
                actions[i] += state[j] * weights[i][j]; // 計算每個行為的加權和
            }
            sumExp += Math.exp(actions[i]); // 計算 softmax 的總和
        }

        // 計算每個行為的選擇機率（使用 softmax）
        for (int i = 0; i < actionSize; i++) {
            actionProbabilities[i] = Math.exp(actions[i]) / sumExp; // 使用 softmax 計算概率
        }

        // 假設隨機選擇某個行為
        double randomValue = Math.random();
        int chosenAction = 0;
        double cumulativeProb = 0.0;
        for (int i = 0; i < actionSize; i++) {
            cumulativeProb += actionProbabilities[i];
            if (randomValue < cumulativeProb) {
                chosenAction = i;
                break;
            }
        }

        // 基於選擇的行為來更新權重
        for (int i = 0; i < state.length; i++) {
            // 策略梯度更新公式：更新選擇的行為的權重
            double gradient = actionProbabilities[chosenAction] * (1 - actionProbabilities[chosenAction]);
            weights[chosenAction][i] += learningRate * tdError * state[i] * gradient;
        }
    }

}