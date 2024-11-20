package ddpg;

import java.util.Arrays;
import java.util.Random;

// 主程序
public class SimpleActorCritic {

    private static double gamma = 0.95;          // 折扣因子
    private static double actorLearningRate = 0.01;  // Actor 的學習率
    private static double criticLearningRate = 0.08;  // Critic 的學習率


    public static void main(String[] args) {
        // 初始化 Actor 和 Critic
        Actor actor = new Actor(2, 3); // 假設狀態有兩個特徵
        Critic critic = new Critic(2);

        // 初始化隨機數生成器
        Random random = new Random();
        int numSamples = 50; // 模擬 50 筆價格與成交量數據

        double[][] states = new double[numSamples][2];  // 價格與成交量
        double[] rewards = new double[numSamples];      // 獎勳（根據買賣計算）

        double capital = 10000.0; // 初始資金
        double position = 0.0;    // 初始持倉
        double entryPrice = 0.0;  // 買入價格
        boolean holding = false;  // 是否持有

        // 生成模擬數據
        for (int i = 0; i < numSamples; i++) {
            double basePrice = 95 + (i % 3) * 5;  // 基準價格：每 3 次價格變化
            double baseVolume = 1000 + (i % 3) * 200;  // 基準成交量

            // 隨機波動價格與成交量
            states[i][0] = basePrice + random.nextDouble() * 5 - 2.5;  // 價格波動範圍
            states[i][1] = baseVolume + random.nextInt(100) - 50;  // 成交量波動範圍
        }

        for (int i = 0; i < numSamples - 1; i++) {
            double[] currentState = states[i];
            double[] nextState = states[i + 1];

            // Actor 模型預測行動
            double action = actor.selectAction(currentState);

            // 根據 Actor 的預測行動執行交易邏輯
            double reward = 0.0; // 即時獎勵
            if (holding) {
                if (action <= 0.5) { // Actor 預測賣出
                    double priceChange = currentState[0] - entryPrice;
                    reward = priceChange * position; // 計算收益
                    capital += position * currentState[0];
                    position = 0.0; // 清空持倉
                    holding = false;
                }
            } else {
                if (action > 0.5) { // Actor 預測買入
                    position = capital / currentState[0];
                    entryPrice = currentState[0];
                    capital = 0.0; // 資金用於買入
                    holding = true;
                }
            }

            // Critic 計算當前與下一狀態的價值
            double value = critic.predictValue(currentState);
            double nextValue = critic.predictValue(nextState);

            // 計算 TD 誤差 (Temporal Difference Error)
            double tdError = reward + gamma * nextValue - value;

            // 更新 Actor 的權重
            actor.updateWeights(currentState, tdError, actorLearningRate);

            // 更新 Critic 的權重
            critic.updateWeights(currentState, tdError, criticLearningRate);

            // 輸出狀態與權重更新
            System.out.printf("第 %d 次決策:\n", i + 1);
            System.out.println("狀態: " + Arrays.toString(currentState));
            System.out.printf("行動: %.2f, TD 誤差: %.2f\n", action, tdError);
            System.out.println("更新後的 Actor 權重: " + Arrays.toString(actor.getWeights()));
            System.out.println("更新後的 Critic 權重: " + Arrays.toString(critic.weights));
            System.out.println();

        System.out.println("== 多次決策與收益模擬 ==");

//        for(int i=0; i<30; i++) {
//            test(actor, critic);
//        }
            }
    }

    private static void test(Actor actor, Critic critic) {
        // 假設狀態有 2 個特徵（價格和成交量）
        int stateSize = 2;

        // 假設交易環境設定
        double capital = 10000.0; // 初始資金
        double position = 0.0; // 初始持倉
        double entryPrice = 0.0; // 持倉價格
        double totalReward = 0.0; // 累積獎勳
        double gamma = 0.9; // 折扣因子

        // 模擬 30 次的價格與成交量波動
        double[][] states = generateStates(30); // 生成隨機狀態

        boolean holding = false; // 是否持倉
        double[] rewards = new double[30]; // 儲存每筆交易的獎勳

        // 交易邏輯（僅測試，不更新 Actor 或 Critic 的權重）
        for (int i = 0; i < states.length; i++) {
            double[] currentState = states[i];
            double action = actor.selectAction(currentState); // Actor 選擇行動
            double reward = 0.0;

            // 根據交易行為計算獎勳
            if (holding) {
                // 若持有，判斷賣出時是否有盈利
                double priceChange = currentState[0] - entryPrice;
                reward = priceChange * position;
                capital += position * currentState[0]; // 更新資金
                position = 0.0; // 賣出後清空持倉
                holding = false; // 更新持倉狀態
            } else {
                // 如果沒有持倉，則可以選擇買入
                if (i > 0 && currentState[0] > states[i - 1][0]) { // 如果價格上漲
                    position = capital / currentState[0];
                    entryPrice = currentState[0];
                    capital = 0.0; // 資金用於買入
                    holding = true; // 設置持倉狀態為持有
                }
            }

            // 計算 Critic 評估（此處僅用作測試）
            double nextValue = i < states.length - 1 ? actor.selectAction(states[i + 1]) : 0.0; // 下一個狀態的評估
            double value = critic.evaluate(reward, gamma, nextValue); // 計算 TD(0) 評估值

            // 累積獎勳
            totalReward += reward;

            // 可選：打印每步的狀態與決策
//            System.out.printf("第 %d 次交易:\n", i + 1);
//            System.out.println("狀態: " + currentState[0] + ", " + currentState[1]);
//            System.out.println("行動預測值: " + action);
//            System.out.println("收益: " + reward);
//            System.out.println("累積收益: " + totalReward);
//            System.out.println();
        }

        System.out.println("最終資金: " + capital + ", 累積獎勳: " + totalReward);
    }

    // 生成 n 個隨機狀態
    public static double[][] generateStates(int n) {
        Random random = new Random();
        double[][] states = new double[n][2]; // 假設每個狀態有 2 個特徵

        for (int i = 0; i < n; i++) {
            // 隨機生成價格和成交量
            states[i][0] = 95 + random.nextDouble() * 10; // 價格在 [95, 105] 範圍內波動
            states[i][1] = 1400 + random.nextInt(400);   // 成交量在 [1400, 1800] 範圍內波動
        }

        return states;
    }
}