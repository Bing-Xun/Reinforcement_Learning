package ddpg.v3;

import ddpg.v1.SimpleActorCritic;
import ddpg.v3.action.actor.ActionActor;
import ddpg.v3.action.critic.ActionCritic;
import ddpg.v3.volume.actor.VolumeActor;
import ddpg.v3.graph.ChartDrawer;
import ddpg.v3.indicators.CalculateKD;
import ddpg.v3.position.Position;
import ddpg.v3.util.Utils;
import ddpg.v3.volume.critic.VolumeCritic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SimplifiedDDPG {

    private static final int STATE_SIZE = 1; // 假設狀態包含3個特徵
    private static final int ACTION_SIZE = 3; // 行動：買入、賣出、持倉

    private static double gamma = 0.7; // 折扣因子
    private static double learningRate = 0.01;  // actor 的學習率
    private static double criticLearningRate = 0.01;  // Critic 的學習率

    public static void main(String[] args) {
        Position position = new Position();
        position.setAmount(BigDecimal.valueOf(1000.0)); // 初始化資金

        // 倉位跟展示相關參數
        double totalReward = 0.0;
        double maxReward = 0.0;
        double minReward = 0.0;
        double maxProfit = 0.0;
        double minProfit = 0.0;

        // 初始化 行情模型
        ActionActor actionActor = new ActionActor(STATE_SIZE, ACTION_SIZE);
        actionActor.setEpsilon(0.9);
        ActionCritic actionCritic = new ActionCritic(STATE_SIZE);

        // 初始化 交易量
        VolumeActor volumeActor = new VolumeActor(STATE_SIZE + ACTION_SIZE);
        VolumeCritic volumeCritic = new VolumeCritic(STATE_SIZE);

        // 模擬行情
        double[][] states = SimpleActorCritic.getStates(100);
//        plotKdGraph(states); // 畫kd圖

        for(int i=0; i<states.length-1; i++) {
            double[] state = new double[]{states[i][0]};
            double[] nextState = new double[]{states[i+1][0]};

            // 行情
            double[] actionProbs = actionActor.predict(state); // 預測行動方向的概率
            double[] nextActionProbs = actionActor.predict(nextState); // 預測行動方向的概率
            double actionReward = 0.0; // TODO double reward = getReward(position, state, action, volume, minReward, maxReward);
            double actionTdError = actionCritic.getTdError(actionReward, gamma, nextState, state); // TD 誤差計算
            // 更新 action 權重
            actionActor.updateWeights(state, actionProbs, actionTdError, learningRate);
            actionCritic.updateWeights(state, actionTdError, criticLearningRate);

            // 成交量
            double volume = volumeActor.predict(state, actionProbs, VolumeActor.getMaxPosition(position.getAmount(), BigDecimal.valueOf(state[0]), position.getPositionCnt())); // 基於方向概率計算交易量
            double nextVolume = volumeActor.predict(nextState, nextActionProbs, VolumeActor.getMaxPosition(position.getAmount(), BigDecimal.valueOf(state[0]), position.getPositionCnt())); // 基於方向概率計算交易量
            double volumeReward = 0.0; // TODO
            double volumeTdError = volumeCritic.getTdError(volumeReward, gamma, nextState, state); // TD 誤差計算, TODO 這邊還要包含行情方向
            // 更新 volume 權重
            volumeActor.updateWeights(state, actionProbs, volumeTdError, learningRate);
            volumeCritic.updateWeights(state, volumeTdError, criticLearningRate);

            // TODO 最大最小獎勵(要做區間壓縮用)
            int action = Utils.getMaxIndex(actionProbs);
            double reward = getReward(position, state, action, volume, minReward, maxReward);
            minReward = minReward > reward ? reward : minReward;
            maxReward = maxReward < reward ? reward : maxReward;

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

//        directionActor.getWeights();
//        volumeActor.getWeights();
    }


    private static double getReward(Position position, double[] state, int action, double volume, double minProfit, double maxProfit) {
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