package ddpg.v3.action.critic;

import ddpg.v3.action.actor.ActorReward;
import ddpg.v3.action.enums.ActionEnum;
import ddpg.v3.action.history.ActionHistory;
import ddpg.v3.util.Utils;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CriticReward {

    private double reward;
    private double[] state;
    private double[] nextState;

    public static CriticReward getRewards(ActionHistory.History history, double[] nextState, double holdDiffPrice, double rewardBoundary) {
        CriticReward criticReward = new CriticReward();
        criticReward.state = history.getState();
        criticReward.nextState = nextState;

        double priceReward = Utils.mapNonLinear(
            history.getAction().getPrice().doubleValue()
            , history.getPosition().getPrice().subtract(new BigDecimal(holdDiffPrice * 3)).doubleValue()
            , history.getPosition().getPrice().add(new BigDecimal(holdDiffPrice * 3)).doubleValue()
        );

        double volumeReward = Math.min(history.getAction().getVolume() / rewardBoundary, 1);

        if(history.getAction().getActionEnum() == ActionEnum.HOLD) {
            criticReward.reward = 1;

            if(history.getAction().getVolume() != 0) {
                criticReward.reward = -Utils.mapNonLinearRangeV;
            }
        }

        if(history.getAction().getActionEnum() == ActionEnum.SELL) {
            criticReward.reward = priceReward * volumeReward;

            if(history.getAction().getVolume() > history.getPosition().getCnt()) {
                criticReward.reward = -Utils.mapNonLinearRangeV;
            }
            if(history.getAction().getVolume() == 0) {
                criticReward.reward = -Utils.mapNonLinearRangeV;
            }
        }

        if(history.getAction().getActionEnum() == ActionEnum.BUY) {
            criticReward.reward = -priceReward * volumeReward;

            if(history.getAction().getVolume() < history.getPosition().getCnt()) {
                criticReward.reward = -Utils.mapNonLinearRangeV;
            }
            if(history.getAction().getVolume() == 0) {
                criticReward.reward = -Utils.mapNonLinearRangeV;
            }
        }

        return criticReward;
    }
}
