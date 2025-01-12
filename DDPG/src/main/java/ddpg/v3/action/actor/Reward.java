package ddpg.v3.action.actor;

import ddpg.v3.util.Utils;
import ddpg.v3.action.enums.ActionEnum;
import ddpg.v3.action.history.ActionHistory;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Reward {

    private double reward;
    private double[] state;
    private double[] nextState;
    private static double holdDiffPrice = 100;

    /**
     * 最後一筆應為sell再跑
     * @param actionHistory
     */
    public static List<Reward> getRewards(ActionHistory actionHistory, BigDecimal price) {
        List<ActionHistory.History> historyList = actionHistory.getHistoryList();

        List<Reward> rewardList = new ArrayList<>();
        for(int i=0; i<historyList.size()-1; i++) {
            ActionHistory.History history = historyList.get(i);
            ActionHistory.History nextHistory = historyList.get(i+1); // 因為這邊要拿到next, 所以 size-1
            rewardList.add(getRewards(history, nextHistory, price));
        }

        return rewardList;
    }


    public static Reward getRewards(ActionHistory.History history, ActionHistory.History nextHistory, BigDecimal price) {
        Reward reward = new Reward();
        reward.state = history.getState();
        reward.nextState = nextHistory.getState();

        if(history.getAction().getActionEnum() == ActionEnum.HOLD) {
            double d = Utils.mapNonLinear(
                    history.getPosition().getPrice().doubleValue()
                    , price.subtract(new BigDecimal(holdDiffPrice)).doubleValue()
                    , price.add(new BigDecimal(holdDiffPrice)).doubleValue()
            );
            reward.reward = Math.abs(Utils.mapNonLinearRangeV - Math.abs(d)); // 越接近中心越大
        }

        if(history.getAction().getActionEnum() == ActionEnum.SELL) {
            reward.reward = Utils.mapNonLinear(
                    price.doubleValue()
                    , history.getPosition().getPrice().subtract(new BigDecimal(holdDiffPrice * 3)).doubleValue()
                    , history.getPosition().getPrice().add(new BigDecimal(holdDiffPrice * 3)).doubleValue()
            );
        }

        if(history.getAction().getActionEnum() == ActionEnum.BUY) {
            reward.reward =  -Utils.mapNonLinear(
                    price.doubleValue()
                    , history.getPosition().getPrice().subtract(new BigDecimal(holdDiffPrice * 3)).doubleValue()
                    , history.getPosition().getPrice().add(new BigDecimal(holdDiffPrice * 3)).doubleValue()
            );
        }

        return reward;
    }
}
