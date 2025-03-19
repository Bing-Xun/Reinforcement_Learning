package ensembleLearning.strategy;

import binace.vo.QuoteVO;
import ensembleLearning.strategy.vo.StrategyVO;

import java.util.List;

public interface Strategy {

    StrategyVO predict(List<QuoteVO> quoteVOList);
}
