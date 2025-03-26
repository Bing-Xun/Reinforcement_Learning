package ensembleLearning.strategy.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ActionVO {

    private String action;
    private Long closeTime;
}
