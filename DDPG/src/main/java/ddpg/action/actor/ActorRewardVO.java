package ddpg.action.actor;

import lombok.Data;


@Data
public class ActorRewardVO {

    private double reward;
    private double[][] state;
}
