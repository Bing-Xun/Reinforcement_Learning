package ddpg.v3.action.actor;

import ddpg.v3.util.Utils;
import ddpg.v3.action.enums.ActionEnum;
import ddpg.v3.action.history.ActionHistory;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActorReward {

    private double reward;
    private double[] state;
    private double[] nextState;

    public static ActorReward getRewards(ActionHistory.History history, double[] nextState, double holdDiffPrice) {
        ActorReward actorReward = new ActorReward();
        actorReward.state = history.getState();
        actorReward.nextState = nextState;
        BigDecimal price = history.getAction().getPrice();

        if(history.getAction().getActionEnum() == ActionEnum.HOLD) {
            double d = Utils.mapNonLinear(
                    history.getPosition().getPrice().doubleValue()
                    , price.subtract(new BigDecimal(holdDiffPrice)).doubleValue()
                    , price.add(new BigDecimal(holdDiffPrice)).doubleValue()
            );
            actorReward.reward = Math.abs(Utils.mapNonLinearRangeV - Math.abs(d)); // 越接近中心越大
        }

        if(history.getAction().getActionEnum() == ActionEnum.SELL) {
            actorReward.reward = Utils.mapNonLinear(
                    price.doubleValue()
                    , history.getPosition().getPrice().subtract(new BigDecimal(holdDiffPrice * 3)).doubleValue()
                    , history.getPosition().getPrice().add(new BigDecimal(holdDiffPrice * 3)).doubleValue()
            );
        }

        if(history.getAction().getActionEnum() == ActionEnum.BUY) {
            actorReward.reward =  -Utils.mapNonLinear(
                    price.doubleValue()
                    , history.getPosition().getPrice().subtract(new BigDecimal(holdDiffPrice * 3)).doubleValue()
                    , history.getPosition().getPrice().add(new BigDecimal(holdDiffPrice * 3)).doubleValue()
            );
        }

        return actorReward;
    }
}
