package ensembleLearning.vo;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.Strategy;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StrategyEnsembleVO {
    private java.util.List<QuoteVO> quoteVOList;
    private List<Strategy> strategies;
    private BigDecimal initAmount;
    private Integer strategyDataTickCnt;
}
