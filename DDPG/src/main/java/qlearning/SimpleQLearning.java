package qlearning;

import java.util.Arrays;
import java.util.Random;

public class SimpleQLearning {

    public static void main(String[] args) {
        // 環境設定
        int environmentSize = 10; // 一維環境的大小
        int startState = 0;
        int goalState = environmentSize - 1;

        // Q-learning 參數
        double alpha = 0.1;   // 學習率
        double gamma = 0.9;   // 折扣因子
        double epsilon = 0.3; // 初始探索率
        double epsilonDecay = 0.995; // 探索率衰減
        int numEpisodes = 1000;
        int maxStepsPerEpisode = 100;

        // 初始化 Q 表
        double[][] qTable = new double[environmentSize][2]; // 2 actions: 0 (left), 1 (right)
        for (double[] row : qTable) {
            Arrays.fill(row, 0.0);
        }

        Random random = new Random();

        // 訓練迴圈 (Episodes)
        for (int episode = 0; episode < numEpisodes; episode++) {
            int currentState = startState;
            //單一episode內的迴圈(Steps)
            for (int step = 0; step < maxStepsPerEpisode; step++) {
                // 選擇動作 (ε-greedy)
                int action;
                if (random.nextDouble() < epsilon) {
                    action = random.nextInt(2); // 隨機動作 (0 or 1)
                } else {
                    action = (qTable[currentState][0] > qTable[currentState][1]) ? 0 : 1; // 選擇Q值較大的
                }

                // 執行動作，獲取新狀態和獎勵
                int newState = currentState;
                int reward = 0;

                if (action == 0) { // 向左
                    newState = Math.max(0, currentState - 1); // 避免超出邊界
                    if(newState == currentState)
                        reward = -1; //撞牆
                } else { // 向右
                    newState = Math.min(environmentSize - 1, currentState + 1);
                    if(newState == currentState)
                        reward = -1;//撞牆
                }

                if (newState == goalState) {
                    reward = 1; // 到達終點
                }


                // 更新 Q 值
                double maxNextQ = Math.max(qTable[newState][0], qTable[newState][1]);
                qTable[currentState][action] += alpha * (reward + gamma * maxNextQ - qTable[currentState][action]);

                currentState = newState;

                if (currentState == goalState) {
                    break; // 到達終點，結束當前 episode
                }
            }

            epsilon *= epsilonDecay; // 衰減探索率
            if((episode+1)%100==0)
                System.out.println("Episode "+ (episode+1) + " finished.");
        }

        // 輸出訓練後的 Q 表
        System.out.println("Trained Q-table:");
        for (int i = 0; i < environmentSize; i++) {
            System.out.println("State " + i + ": Left = " + qTable[i][0] + ", Right = " + qTable[i][1]);
        }

        // 測試策略
        System.out.println("\nTesting the policy:");
        int testState = startState;
        while (testState != goalState) {
            int testAction = (qTable[testState][0] > qTable[testState][1]) ? 0 : 1;
            System.out.println("Current state: " + testState + ", Action: " + (testAction == 0 ? "Left" : "Right"));
            testState = (testAction == 0) ? Math.max(0, testState - 1) : Math.min(environmentSize - 1, testState + 1);
        }
        System.out.println("Reached goal state: " + testState);
    }
}