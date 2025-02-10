package ddpg.action.actor;

import ddpg.v3.position.Position;
import lombok.Data;

import java.text.DecimalFormat;


@Data
public class ActorReward {

    private Double preAction = 0.0;
    private Double sufAction = 0.0;

    private Double holdDiff = 0.1;

    public ActorRewardVO getRewards(Double[] state, Double[] nextState, int action, Double vwap, Double price) {
        ActorRewardVO vo = new ActorRewardVO();
        vo.setState(state);
        vo.setNextState(nextState);
        vo.setReward(0.0);

        // buy
        if(action == 0) {
            vo.setReward(((vwap / price) - 1));
//            System.out.println("###");
            DecimalFormat df = new DecimalFormat("0.################");
//            System.out.println("action 0:" + df.format(vo.getReward()));
        }

        // sell
        if(action == 1) {
            vo.setReward(((price / vwap) - 1));
//            System.out.println("###");
            DecimalFormat df = new DecimalFormat("0.################");
//            System.out.println("action 1:" + df.format((price / vwap) - 1));
        }

        // hold
        if(action == 2) {
            if(vwap * (1+holdDiff) > price ||  vwap * (1-holdDiff) < price) {
                if(vwap > price) {
                    vo.setReward((price / vwap) * 0.5);
                }
                if(vwap < price) {
                    vo.setReward((vwap / price) * 0.5);
                }
            } else {
                if(vwap > price) {
                    vo.setReward(1 - (vwap / price));
                }
                if(vwap < price) {
                    vo.setReward(1 - (price / vwap));
                }
            }
//            System.out.println(price);
//            System.out.println(vwap);
//            System.out.println(vo.getReward());
//            System.out.println("action 2:" + vo.getReward());
        }

        return vo;
    }

    public void updateActionWeight(Double preAction, Double sufAction) {
        this.preAction = preAction;
        this.sufAction = sufAction;
    }

    public int getAction(Double action) {
//        System.out.println("action:"+action);
//        System.out.println("preAction:"+preAction);
//        System.out.println("sufAction:"+sufAction);

        if(action > preAction) {
            return 0;
        }
        if(action < sufAction) {
            return 1;
        }

        return 2;
    }
}
