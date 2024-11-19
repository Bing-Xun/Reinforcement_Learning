package ddpg;

import java.util.Arrays;
import java.util.Random;

// 主程序
public class SimpleActorCritic {
    public static void main(String[] args) {
        // 初始化 Actor 和 Critic
        Actor actor = new Actor(2); // 假設狀態有兩個特徵
        Critic critic = new Critic();

        // 初始化隨機數生成器
        Random random = new Random();
        int numSamples = 50; // 模擬 50 筆價格與成交量數據

        double[][] states = new double[numSamples][2];  // 價格與成交量
        double[] rewards = new double[numSamples];      // 獎勳（根據買賣計算）

        double capital = 10000.0; // 初始資金
        double position = 0.0;    // 初始持倉
        double entryPrice = 0.0;  // 買入價格
        double totalProfit = 0.0; // 累積收益
        boolean holding = false;  // 是否持有

        // 生成模擬數據
        for (int i = 0; i < numSamples; i++) {
            double basePrice = 95 + (i % 3) * 5;  // 基準價格：每 3 次價格變化
            double baseVolume = 1000 + (i % 3) * 200;  // 基準成交量

            // 隨機波動價格與成交量
            states[i][0] = basePrice + random.nextDouble() * 5 - 2.5;  // 價格波動範圍
            states[i][1] = baseVolume + random.nextInt(100) - 50;  // 成交量波動範圍

            // 根據交易行為計算獎勳
            if (holding) {
                // 若持有，判斷賣出時是否有盈利
                double priceChange = states[i][0] - entryPrice;
                rewards[i] = priceChange * position;
                totalProfit += rewards[i];
                capital += position * states[i][0]; // 更新資金
                position = 0.0; // 卖出後清空持倉
                holding = false; // 更新持倉狀態
            } else {
                // 如果沒有持倉，則可以選擇買入
                if (i > 0 && states[i][0] > states[i - 1][0]) { // 如果價格上漲
                    position = capital / states[i][0];
                    entryPrice = states[i][0];
                    capital = 0.0; // 資金用於買入
                    holding = true; // 設置持倉狀態為持有
                }
            }
        }

        // 輸出生成的數據與獎勳
        System.out.println("生成的 50 筆價格與成交量數據，及對應的獎勳：");
        for (int i = 0; i < states.length; i++) {
            System.out.println("價格: " + String.format("%.2f", states[i][0]) + ", 成交量: " + (int) states[i][1] + ", 獎勳: " + rewards[i]);
        }

        double gamma = 0.9;
        double learningRate = 0.01;

        System.out.println("== 多次決策與收益模擬 ==");

        double totalReward = 0.0;

        for (int t = 0; t < states.length - 1; t++) {
            double[] currentState = states[t];
            double[] nextState = states[t + 1];

            // Actor 模型做出行動預測
            double action = actor.selectAction(currentState);

            // Critic 模型計算狀態價值與 TD 誤差
            double nextValue = actor.selectAction(nextState);
            double value = critic.evaluate(rewards[t], gamma, nextValue);
            double tdError = value - action;

            // 更新 Actor 的權重
            actor.updateWeights(currentState, tdError, learningRate);

            // 計算累積收益
            totalReward += rewards[t];

            System.out.printf("第 %d 次決策:\n", t + 1);
            System.out.println("狀態: " + Arrays.toString(currentState));
            System.out.println("行動預測值: " + action);
            System.out.println("收益: " + rewards[t]);
            System.out.println("累積收益: " + totalReward);
            System.out.println("更新後的權重: " + Arrays.toString(actor.weights));
            System.out.println();
        }

        test(actor, critic);
    }

    private static void test(Actor actor, Critic critic) {
        // 初始化隨機數生成器
        Random random = new Random();

        // 模擬 30 次的價格與成交量波動
        double[][] testStates = new double[30][2];  // 每個狀態 [價格, 成交量]
        for (int i = 0; i < testStates.length; i++) {
            testStates[i][0] = 95 + random.nextDouble() * 10; // 價格在 [95, 105] 範圍內波動
            testStates[i][1] = 1400 + random.nextInt(400);   // 成交量在 [1400, 1800] 範圍內波動
        }

        // 初始化資金與頭寸
        double capital = 10000.0; // 初始資金
        double position = 0.0;    // 初始持倉
        double entryPrice = 0.0;  // 持倉價格
        double totalProfit = 0.0; // 累積收益

        double priceChangeThreshold = 1.0; // 價格波動閾值

        // 儲存 Critic 和 Actor 的更新
        double[] criticValues = new double[testStates.length]; // 每個狀態的價值預測
        double[] rewards = new double[testStates.length]; // 每個步驟的獎勳
        double[] tdErrors = new double[testStates.length];  // 時間差分誤差

        System.out.println("== 測試應用（30 次，收益計算） ==");

        for (int i = 0; i < testStates.length; i++) {
            double[] state = testStates[i];
            double predictedAction = actor.selectAction(state); // 由 Actor 選擇行為
            double price = state[0]; // 當前價格
            double profit = 0.0;     // 單次收益

            // Critic 預測當前狀態的價值
            criticValues[i] = critic.predictValue(state);

            System.out.printf("PredictedAction: %.2f, ", predictedAction);

            // 行動決策模擬（假設行動值範圍：[0, 1] => 0: SELL, 1: BUY）
            if (predictedAction > 0.5 && position == 0) { // BUY
                position = capital / price;  // 購入數量
                entryPrice = price;          // 記錄買入價格
                capital = 0.0;               // 消耗資金
                System.out.printf("狀態 %d: BUY at price %.2f, 持倉 %.2f\n", i + 1, price, position);
            } else if (predictedAction <= 0.5 && position > 0 && Math.abs(price - entryPrice) >= priceChangeThreshold) { // SELL with threshold
                profit = (price - entryPrice) * position; // 單次收益
                capital += price * position;  // 更新資金
                totalProfit += profit;       // 累積收益
                position = 0.0;              // 清空持倉
                System.out.printf("狀態 %d: SELL at price %.2f, 單次收益 %.2f, 累積收益 %.2f\n", i + 1, price, profit, totalProfit);
            } else {
                System.out.printf("狀態 %d: HOLD at price %.2f\n", i + 1, price);
            }

            // 計算獎勳（這裡假設根據價格變化來計算獎勳）
            if (i > 0) {
                double priceChange = testStates[i][0] - testStates[i - 1][0];
                rewards[i] = priceChange * 10; // 簡單獎勳計算
            } else {
                rewards[i] = 0;
            }

            // 計算 TD error
            if (i > 0) {
                tdErrors[i] = rewards[i] + criticValues[i] - criticValues[i - 1];  // TD error
            }

            // 使用 TD error 更新 Critic 權重
            critic.update(tdErrors[i], 0.01); // 使用 TD error 更新 Critic 權重
        }

        // 最終資金與收益
        System.out.printf("最終資金: %.2f, 累積收益: %.2f\n", capital + (position * entryPrice), totalProfit);
    }
}