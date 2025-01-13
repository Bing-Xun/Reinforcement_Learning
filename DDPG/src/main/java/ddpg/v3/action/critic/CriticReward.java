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

    public static CriticReward getRewards(ActionHistory.History history, double[] nextState, double maxVolumeRewardSource) {
        CriticReward criticReward = new CriticReward();
        criticReward.state = history.getState();
        criticReward.nextState = nextState;

//        if(history.getAction().getActionEnum() == ActionEnum.HOLD) {
//            double d = ddpg.v3.util.Utils.mapNonLinear(
//                    history.getPosition().getPrice().doubleValue()
//                    , price.subtract(new BigDecimal(holdDiffPrice)).doubleValue()
//                    , price.add(new BigDecimal(holdDiffPrice)).doubleValue()
//            );
//            actorReward.reward = Math.abs(ddpg.v3.util.Utils.mapNonLinearRangeV - Math.abs(d)); // 越接近中心越大
//        }

        if(history.getAction().getActionEnum() == ActionEnum.SELL) {
            double diffPrice = (history.getAction().getPrice().subtract(history.getPosition().getPrice())).doubleValue();
            criticReward.reward = ddpg.v3.util.Utils.mapNonLinear(
                    diffPrice * history.getAction().getVolume() * maxVolumeRewardSource
                    , history.getPosition().getPrice().subtract(new BigDecimal(holdDiffPrice * 3)).doubleValue()
                    , history.getPosition().getPrice().add(new BigDecimal(holdDiffPrice * 3)).doubleValue()
            );
        }

        if(history.getAction().getActionEnum() == ActionEnum.BUY) {
            criticReward.reward =  -Utils.mapNonLinear(
                    price.doubleValue()
                    , history.getPosition().getPrice().subtract(new BigDecimal(holdDiffPrice * 3)).doubleValue()
                    , history.getPosition().getPrice().add(new BigDecimal(holdDiffPrice * 3)).doubleValue()
            );
        }

        return actorReward;
    }
}
