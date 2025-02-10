package ddpg.action.actor;

import lombok.Data;


@Data
public class ActorRewardVO {

    private Double reward;
    private Double[] state;
    private Double[] nextState;
}
