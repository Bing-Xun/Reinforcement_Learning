package ddpg.v3.volume.reward;

import ddpg.v3.position.Position;

import java.math.BigDecimal;
import java.util.List;

public class VolumeReward {

    public static double getReward(Position position, double[] state, int action, double minProfit, double maxProfit) {
        double reward = 0.0;

//        if(List.of(0, 1).contains(action) && volume == 0) return -1;
//
//        // buy
//        if(action == 0) {
//            reward = position.getPredictReward(BigDecimal.valueOf(state[0]), volume, 0, minProfit, maxProfit);
//            if(reward == 0 && position.getPositionCnt() == 0) reward = 0.1; // 獎勵買進
//        }
//
//        // sell
//        if(action == 1 ) {
//            reward = position.getPredictReward(BigDecimal.valueOf(state[0]), volume, 1, minProfit, maxProfit);
//            if(reward == 0 && position.getPositionCnt() > 0) reward = 0.1; // 獎勵賣出
//        }

        return reward;
    }
}
