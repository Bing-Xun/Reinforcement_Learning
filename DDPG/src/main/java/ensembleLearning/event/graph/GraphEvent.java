package ensembleLearning.event.graph;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class GraphEvent {

    private List<GraphEventVO> graphEventVOList;

    @Builder
    @Data
    public static class GraphEventVO {
        private String action;
        private Long closeTime;
        private BigDecimal price;
        private BigDecimal amount;
        private double positionCnt;
        private BigDecimal positionPrice;
    }
}
