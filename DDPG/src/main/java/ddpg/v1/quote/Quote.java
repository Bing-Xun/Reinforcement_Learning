package ddpg.v1.quote;

import java.util.Random;

public class Quote {

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
