package ddpg;

// Actor 模型
public class Actor {
    // 簡單模擬一個權重，用於計算行動值
    public double[] weights;

    public Actor(int stateSize) {
        this.weights = new double[stateSize];
        // 初始化權重為隨機值
        for (int i = 0; i < stateSize; i++) {
            weights[i] = Math.random() * 0.1; // 小範圍初始化
        }
    }

    public double selectAction(double[] state) {
        double action = 0.0;
        double[] actionProbabilities = new double[3]; // 假設有三個可能的行動

        // 計算每個行為的加權和
        for (int i = 0; i < state.length; i++) {
            action += state[i] * weights[i];
        }

        // Softmax 計算概率分佈
        double expAction = Math.exp(action);
        double sumExp = Math.exp(action) + Math.exp(-action); // 這是簡單的例子，假設有 SELL 和 BUY
        actionProbabilities[0] = expAction / sumExp; // SELL
        actionProbabilities[1] = 1 - actionProbabilities[0]; // BUY

        // 根據概率選擇行動
        double randomValue = Math.random();
        if (randomValue < actionProbabilities[0]) {
            return 0.0; // SELL
        } else {
            return 1.0; // BUY
        }
    }


    // 更新 Actor 的權重
    public void updateWeights(double[] state, double error, double learningRate) {
        for (int i = 0; i < weights.length; i++) {
            weights[i] += learningRate * error * state[i];
        }
    }
}