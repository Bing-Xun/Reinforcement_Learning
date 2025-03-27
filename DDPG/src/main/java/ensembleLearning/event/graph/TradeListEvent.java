package ensembleLearning.event.graph;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class TradeListEvent {

    private List<TradeVO> graphEventVOList;

    @Builder
    @Data
    public static class TradeVO {
        private String action;
        private Long closeTime;
        private BigDecimal price;
        private BigDecimal amount;
        private double positionCnt;
        private BigDecimal positionPrice;
    }
}
