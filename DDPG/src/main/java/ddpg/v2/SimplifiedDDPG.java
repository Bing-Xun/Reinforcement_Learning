package ddpg.v2;

import ddpg.v1.SimpleActorCritic;
import ddpg.v2.actor.DirectionActor;
import ddpg.v2.actor.VolumeActor;
import ddpg.v2.critic.Critic;
import ddpg.v2.position.Position;
import ddpg.v2.util.Utils;

import java.math.BigDecimal;
import java.util.List;

public class SimplifiedDDPG {

    private static final int STATE_SIZE = 1; // 假設狀態包含3個特徵
    private static final int ACTION_SIZE = 3; // 行動：買入、賣出、持倉

    private static double gamma = 0.7; // 折扣因子
    private static double learningRate = 0.01;  // actor 的學習率
    private static double criticLearningRate = 0.01;  // Critic 的學習率
    private static Position position = new Position();
    private static double maxReward = 0.0;
    private static double minReward = 0.0;
    private static double maxProfit = 0.0;
    private static double minProfit = 0.0;

    public static void main(String[] args) {
        double totalReward = 0.0;
        position.setAmount(BigDecimal.valueOf(1000.0));

        // 初始化 Actor 模型
        DirectionActor directionActor = new DirectionActor(STATE_SIZE, ACTION_SIZE);
        directionActor.setEpsilon(0.9);
        VolumeActor volumeActor = new VolumeActor(STATE_SIZE + ACTION_SIZE);
        Critic critic = new Critic(STATE_SIZE);


//        double[] state = {100.0, 50.0, 0.3}; // 假設一個初始狀態
        double[][] states = SimpleActorCritic.getStates(3000);

        for(int i=0; i<states.length-1; i++) {
            double[] state = new double[]{states[i][0]};
            double[] nextState = new double[]{states[i+1][0]};
            double[] actionProbs = directionActor.predict(state); // 預測行動方向的概率
            double[] nextActionProbs = directionActor.predict(nextState); // 預測行動方向的概率
            double volume = volumeActor.predict(state, nextActionProbs, VolumeActor.getMaxPosition(position.getAmount(), BigDecimal.valueOf(state[0]), position.getPositionCnt())); // 基於方向概率計算交易量
            int action = Utils.getMaxIndex(actionProbs);
            double reward = getReward(state, action, volume, minReward, maxReward);
            minReward = minReward > reward ? reward : minReward;
            maxReward = maxReward < reward ? reward : maxReward;

            // Critic 的 Q 值
            double currentQValue = critic.predictValue(state); // 當前 Q 值
            double nextQValue = critic.predictValue(nextState); // 下一狀態 Q 值
            double tdError = reward + gamma * nextQValue - currentQValue; // TD 誤差計算
            if (Math.abs(tdError) > 1e10) {
//                System.err.println("tdError 超出範圍：" + tdError);
                tdError = Math.signum(tdError) * 1e10; // 限制在最大值
            }

            // 更新權重
            directionActor.updateWeights(state, actionProbs, tdError, learningRate);
            volumeActor.updateWeights(state, actionProbs, tdError, learningRate);
            critic.updateWeights(state, tdError, criticLearningRate);

            if(i == 100) directionActor.setEpsilon(0.5); // 調整探索率
            if(i == 200) directionActor.setEpsilon(0.1); // 調整探索率
            if(i == 500) gamma = 0.1; // 調整探索率

            // 更新持倉跟交易相關
            double profit = position.modifyPosition(BigDecimal.valueOf(state[0]), volume, action);
            if (profit > maxProfit) maxProfit = profit;
            if (profit < minProfit) minProfit = profit;
            totalReward += profit;

            System.out.println("");
            System.out.println("##########");
            System.out.println("狀態: " + java.util.Arrays.toString(state));
            System.out.println("方向概率 (買, 賣, 持倉): " + java.util.Arrays.toString(nextActionProbs));
            System.out.println("方向(買, 賣, 持倉): " + action);
            System.out.println("獎勵: " + reward);
            if(reward > 1) System.out.println("獎勵aa: " + reward);
            System.out.printf("交易量: %.2f\n", volume);
            System.out.printf("現金餘額: %.2f\n", position.getAmount());
            System.out.printf("持倉量: %.2f\n", position.getPositionCnt());
            System.out.printf("持倉均價: %.2f\n", position.getPrice());
            System.out.printf("單次收益: %.2f\n", profit);
            System.out.printf("最高收益: %.2f\n", maxProfit);
            System.out.printf("最低收益: %.2f\n", minProfit);
            System.out.printf("總收益: %.2f\n", totalReward);
        }
    }


    private static double getReward(double[] state, int action, double volume, double minProfit, double maxProfit) {
        double reward = 0.0;

        if(List.of(0, 1).contains(action) && volume == 0) return -1;

        // buy
        if(action == 0) {
            reward = position.getPredictReward(BigDecimal.valueOf(state[0]), volume, 0, minProfit, maxProfit);
            if(reward == 0 && position.getPositionCnt() == 0) reward = 0.1; // 獎勵買進
        }

        // sell
        if(action == 1 ) {
            reward = position.getPredictReward(BigDecimal.valueOf(state[0]), volume, 1, minProfit, maxProfit);
            if(reward == 0 && position.getPositionCnt() > 0) reward = 0.1; // 獎勵賣出
        }

        return reward;
    }
}
