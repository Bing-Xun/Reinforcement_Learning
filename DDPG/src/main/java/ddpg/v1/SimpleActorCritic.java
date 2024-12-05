package ddpg.v1;

import ddpg.v1.position.Position;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

// 主程序
public class SimpleActorCritic {

    private static double gamma = 0.95;          // 折扣因子
    private static double actorLearningRate = 0.09;  // Actor 的學習率
    private static double criticLearningRate = 0.03;  // Critic 的學習率

    private static Position position = new Position();


    public static void main(String[] args) {
        // 初始化 Actor 和 Critic
        Actor actor = new Actor(2, 3); // 假設狀態有兩個特徵
        Critic critic = new Critic(2);

        // 初始化隨機數生成器
        int numSamples = 50; // 模擬 50 筆價格與成交量數據

        // 生成模擬數據
        double[][] states = getStates(numSamples); // 價格與成交量

        double totalReward = 0.0; // 用于跟踪累积收益
        for (int i = 0; i < numSamples - 1; i++) {
            double[] currentState = states[i];
            double[] nextState = states[i + 1];

            // Actor 模型预测行动
            double action = actor.selectAction(currentState);

            // 根据 Actor 的输出行动值执行交易逻辑
            double reward = 0.0; // 即时奖励
//            if (position.getPositionCnt() > 0) {
//                // 如果持有，允许卖出
//                if (action == 0) { // SELL
//                    BigDecimal priceChange = BigDecimal.valueOf(currentState[0]).subtract(position.getPrice());
//                    reward = priceChange * position; // 计算收益
//                    capital += position * currentState[0];
//                    position = 0.0; // 清空持仓
//                    holding = false;
//                }
//            } else {
//                // 如果没有持有，允许买入
//                if (action == 2) { // BUY
//                    position = capital / currentState[0];
//                    entryPrice = currentState[0];
//                    capital = 0.0; // 资金用于买入
//                    holding = true;
//                }
//            }

            // Critic 计算当前与下一状态的价值
            double value = critic.predictValue(currentState);
            double nextValue = critic.predictValue(nextState);

            // 计算 TD 误差 (Temporal Difference Error)
            double tdError = reward + gamma * nextValue - value;

            // 更新 Actor 的权重
            actor.updateWeights(currentState, tdError, actorLearningRate);

            // 更新 Critic 的权重
            critic.updateWeights(currentState, tdError, criticLearningRate);

            // 累加总收益
            totalReward += reward;

            // 输出状态与权重更新
            System.out.printf("第 %d 次决策:\n", i + 1);
            System.out.println("状态: " + Arrays.toString(currentState));
            System.out.printf("行动: %.2f, TD 误差: %.2f\n", action, tdError);
            System.out.println("持倉: " + position);
            System.out.println("即时奖励: " + reward);
            System.out.println("累积收益: " + totalReward);
            System.out.println("更新后的 Actor 权重: " + Arrays.deepToString(actor.getWeights()));
            System.out.println("更新后的 Critic 权重: " + Arrays.toString(critic.weights));
            System.out.println();

        System.out.println("== 多次決策與收益模擬 ==");

//        for(int i=0; i<30; i++) {
//            test(actor, critic);
//        }
            }
    }

    public static double[][] getStates(int numSamples) {
        double[][] states = new double[numSamples][2];  // 價格與成交量
        Random random = new Random();

        // 生成模擬數據
        for (int i = 0; i < numSamples; i++) {
            double basePrice = 95 + (i % 3) * 5;  // 基準價格：每 3 次價格變化
            double baseVolume = 1000 + (i % 3) * 200;  // 基準成交量

            // 隨機波動價格與成交量
            states[i][0] = basePrice + random.nextDouble() * 5 - 2.5;  // 價格波動範圍
            states[i][1] = baseVolume + random.nextInt(100) - 50;  // 成交量波動範圍
        }

        return states;
    }

//    private static void test(Actor actor, Critic critic) {
//        // 假設狀態有 2 個特徵（價格和成交量）
//        int stateSize = 2;
//
//        // 假設交易環境設定
//        double capital = 10000.0; // 初始資金
//        double position = 0.0; // 初始持倉
//        double entryPrice = 0.0; // 持倉價格
//        double totalReward = 0.0; // 累積獎勳
//        double gamma = 0.9; // 折扣因子
//
//        // 模擬 30 次的價格與成交量波動
//        double[][] states = Quote.generateStates(30); // 生成隨機狀態
//
//        boolean holding = false; // 是否持倉
//        double[] rewards = new double[30]; // 儲存每筆交易的獎勳
//
//        // 交易邏輯（僅測試，不更新 Actor 或 Critic 的權重）
//        for (int i = 0; i < states.length; i++) {
//            double[] currentState = states[i];
//            double action = actor.selectAction(currentState); // Actor 選擇行動
//            double reward = 0.0;
//
//            // 根據交易行為計算獎勳
//            if (holding) {
//                // 若持有，判斷賣出時是否有盈利
//                double priceChange = currentState[0] - entryPrice;
//                reward = priceChange * position;
//                capital += position * currentState[0]; // 更新資金
//                position = 0.0; // 賣出後清空持倉
//                holding = false; // 更新持倉狀態
//            } else {
//                // 如果沒有持倉，則可以選擇買入
//                if (i > 0 && currentState[0] > states[i - 1][0]) { // 如果價格上漲
//                    position = capital / currentState[0];
//                    entryPrice = currentState[0];
//                    capital = 0.0; // 資金用於買入
//                    holding = true; // 設置持倉狀態為持有
//                }
//            }
//
//            // 計算 Critic 評估（此處僅用作測試）
//            double nextValue = i < states.length - 1 ? actor.selectAction(states[i + 1]) : 0.0; // 下一個狀態的評估
//            double value = critic.evaluate(reward, gamma, nextValue); // 計算 TD(0) 評估值
//
//            // 累積獎勳
//            totalReward += reward;
//
//            // 可選：打印每步的狀態與決策
////            System.out.printf("第 %d 次交易:\n", i + 1);
////            System.out.println("狀態: " + currentState[0] + ", " + currentState[1]);
////            System.out.println("行動預測值: " + action);
////            System.out.println("收益: " + reward);
////            System.out.println("累積收益: " + totalReward);
////            System.out.println();
//        }
//
//        System.out.println("最終資金: " + capital + ", 累積獎勳: " + totalReward);
//    }
}