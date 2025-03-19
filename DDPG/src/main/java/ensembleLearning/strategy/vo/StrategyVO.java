package ensembleLearning.strategy.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StrategyVO {

    private String strategyName;
    private String action;
    private Long closeTime;
}
