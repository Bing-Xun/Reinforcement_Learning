package ensembleLearning.strategy.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ActionVO {

    private String action;
    private Long closeTime;
}
