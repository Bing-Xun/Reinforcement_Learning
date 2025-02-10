package ddpg.trend.actor;

import lombok.Data;


@Data
public class ActorReward {

    private Double reward;
    private Double[] state;
    private Double[] nextState;

    /**
     * 這邊先用趨勢預測線性對應昨日跟今日加權價差
     * @param state
     * @param nextState
     * @param predictTrend
     * @param vwapDiff
     * @return
     */
    public static ActorReward getRewards(Double[] state, Double[] nextState, Double predictTrend, Double vwapDiff) {
        ActorReward actorReward = new ActorReward();
        actorReward.state = state;
        actorReward.nextState = nextState;
        actorReward.reward = 1 - Math.abs(predictTrend - vwapDiff);
//        System.out.println("###");
//        System.out.println("predictTrend:"+predictTrend);
//        System.out.println("vwapDiff:"+vwapDiff);
//        System.out.println("reward:"+actorReward.reward);
        return actorReward;
    }
}
