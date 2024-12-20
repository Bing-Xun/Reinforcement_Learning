package ddpg.v3;

import ddpg.v1.SimpleActorCritic;
import ddpg.v3.action.actor.ActionActor;
import ddpg.v3.action.critic.ActionCritic;
import ddpg.v3.action.enums.ActionEnum;
import ddpg.v3.action.history.ActionHistory;
import ddpg.v3.reward.Reward;
import ddpg.v3.position.Position;
import ddpg.v3.util.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class SimplifiedDDPG {

    private static final int STATE_SIZE = 1; // 假設狀態包含3個特徵
    private static final int ACTION_SIZE = 3; // 行動：買入、賣出、持倉, 成交量

    private static double gamma = 0.8; // 折扣因子
    private static double learningRate = 0.01;  // actor 的學習率
    private static double criticLearningRate = 0.01;  // Critic 的學習率

    public static void main(String[] args) {
        Position position = new Position();
        position.setAmount(BigDecimal.valueOf(1000.0)); // 初始化資金

        // 倉位跟展示相關參數
        double totalReward = 0.0;
        double maxProfit = 0.0;
        double minProfit = 0.0;

        // 初始化 行情模型
        ActionActor actionActor = new ActionActor(STATE_SIZE, ACTION_SIZE);
        actionActor.setEpsilon(0.9);
        ActionCritic actionCritic = new ActionCritic(STATE_SIZE);

        // 模擬行情
        double[][] states = SimpleActorCritic.getStates(600);
//        plotKdGraph(states); // 畫kd圖
        ActionHistory actionHistory = new ActionHistory();

        for(int i=0; i<states.length-1; i++) {
            double[] state = new double[]{states[i][0]};
            double price = state[0];

            // 行情
            double[] actionProbs = actionActor.predict(state); // 預測行動方向的概率

            ActionHistory.History history = new ActionHistory.History();
            history.setState(state);
            history.setAction(actionProbs);
            history.setPrice(BigDecimal.valueOf(price));
            history.setPosition(position.getPositionCnt());
            actionHistory.getHistoryList().add(history);

            if(Utils.getMaxIndex(actionProbs) == ActionEnum.SELL.getValue()) {
                List<Reward> rewardList = Reward.getRewards(actionHistory);

                for(Reward reward : rewardList) {
                    double actionTdError = actionCritic.getTdError(reward.getReward(), gamma, reward.getState(), reward.getNextState()); // TD 誤差計算
                    // 更新 action 權重
                    actionActor.updateWeights(state, actionProbs, actionTdError, learningRate);
                    actionCritic.updateWeights(state, actionTdError, criticLearningRate);
                }
            }

            int action = Utils.getMaxIndex(actionProbs);
            double volume = actionProbs[ACTION_SIZE];
            double profit = position.modifyPosition(BigDecimal.valueOf(state[0]), volume, action);
            if (profit > maxProfit) maxProfit = profit;
            if (profit < minProfit) minProfit = profit;
            totalReward += profit;

            System.out.println("");
            System.out.println("##########");
            System.out.println("狀態: " + java.util.Arrays.toString(state));
            System.out.println("方向概率 (買, 賣, 持倉): " + java.util.Arrays.toString(actionProbs));
//            System.out.println("方向(買, 賣, 持倉): " + action);
//            System.out.println("獎勵: " + reward);
//            if(reward > 1) System.out.println("獎勵aa: " + reward);
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

    private static double getVolume(Position position, Integer action, BigDecimal price) {
        if(action == 0) {
            Double maxPosition = position.getAmount().divide(price, 4, RoundingMode.DOWN).doubleValue();
            return Math.random() * ((maxPosition / 10.0) + 1);
        }
        if(action == 1) {
            return Math.random() * (position.getPositionCnt() + 1);
        }

        return 0;
    }
}
