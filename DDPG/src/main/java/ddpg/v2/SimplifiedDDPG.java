package ddpg.v2;

import ddpg.v1.SimpleActorCritic;
import ddpg.v2.actor.DirectionActor;
import ddpg.v2.actor.VolumeActor;
import ddpg.v2.critic.Critic;
import ddpg.v2.position.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class SimplifiedDDPG {

    private static final int STATE_SIZE = 1; // 假設狀態包含3個特徵
    private static final int ACTION_SIZE = 3; // 行動：買入、賣出、持倉

    private static double gamma = 0.1; // 折扣因子
    private static double learningRate = 0.1;  // actor 的學習率
    private static double criticLearningRate = 0.1;  // Critic 的學習率
    private static Position position = new Position();

    public static void main(String[] args) {
        double totalReward = 0.0;
        position.setAmount(BigDecimal.valueOf(1000.0));

        // 初始化 Actor 模型
        DirectionActor directionActor = new DirectionActor(STATE_SIZE, ACTION_SIZE);
        VolumeActor volumeActor = new VolumeActor(STATE_SIZE + ACTION_SIZE);
        Critic critic = new Critic(STATE_SIZE);

//        double[] state = {100.0, 50.0, 0.3}; // 假設一個初始狀態
        double[][] states = SimpleActorCritic.getStates(50);

        for(int i=0; i<states.length-1; i++) {
            double[] state = new double[]{states[i][0]};
            double[] nextState = new double[]{states[i+1][0]};
            double[] actionProbs = directionActor.predict(state); // 預測行動方向的概率
            double[] nextActionProbs = directionActor.predict(nextState); // 預測行動方向的概率
            double volume = volumeActor.predict(state, nextActionProbs, (position.getAmount().divide(BigDecimal.valueOf(state[0]), 10, RoundingMode.HALF_UP).doubleValue())); // 基於方向概率計算交易量
            double reward = getReward(state, actionProbs, volume);

            // Critic 的 Q 值
            double currentQValue = critic.predictValue(state); // 當前 Q 值
            double nextQValue = critic.predictValue(nextState); // 下一狀態 Q 值
            double tdError = reward + gamma * nextQValue - currentQValue; // TD 誤差計算

            // 更新權重
            directionActor.updateWeights(state, actionProbs, tdError, learningRate);
            volumeActor.updateWeights(state, actionProbs, tdError, learningRate);
            critic.updateWeights(state, tdError, criticLearningRate);

            System.out.println("");
            System.out.println("##########");
            System.out.println("狀態: " + java.util.Arrays.toString(state));
            System.out.println("方向概率 (買, 賣, 持倉): " + java.util.Arrays.toString(nextActionProbs));
            System.out.println("方向(買, 賣, 持倉): " + DirectionActor.getMaxIndex(actionProbs));
            System.out.printf("交易量: %.2f\n", volume);
            System.out.printf("現金餘額: %.2f\n", position.getAmount());
            System.out.printf("持倉量: %.2f\n", position.getPositionCnt());
            System.out.printf("持倉均價: %.2f\n", position.getPrice());

            totalReward += reward;
            System.out.printf("總收益: %.2f\n", totalReward);
        }
    }

    private static double getReward(double[] state, double[] actionProbs, double volume) {
//        System.out.println("getReward actionProbs:"+ java.util.Arrays.toString(actionProbs));
        double reward = 0.0;

        // buy
        if(DirectionActor.getMaxIndex(actionProbs) == 0) {
//            System.out.println("getReward 1");
            reward = position.modifyPosition(BigDecimal.valueOf(state[0]), BigDecimal.valueOf(volume), 0);
        }

        // sell
        if(DirectionActor.getMaxIndex(actionProbs) == 1) {
            reward = position.modifyPosition(BigDecimal.valueOf(state[0]), BigDecimal.valueOf(volume), 1);
        }

        return reward;
    }
}
