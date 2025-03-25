package ensembleLearning.strategy.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HighLowTradingVO {

    private String action;
    private Integer weight;
}
