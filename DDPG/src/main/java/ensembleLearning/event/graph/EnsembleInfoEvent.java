package ensembleLearning.event.graph;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class EnsembleInfoEvent {

    @Builder.Default
    private BigDecimal earnAmount = BigDecimal.ZERO;

    @Builder.Default
    private Double maxAmount = 0.0;

    @Builder.Default
    private Double minAmount = 99999.0;

    @Builder.Default
    private Integer holdCnt = 0;

    @Builder.Default
    private Integer sellCnt = 0;

    @Builder.Default
    private Integer buyCnt = 0;
}
