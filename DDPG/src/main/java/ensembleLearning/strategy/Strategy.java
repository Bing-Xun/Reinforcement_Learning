package ensembleLearning.strategy;

import binace.vo.QuoteVO;

import java.util.List;

public interface Strategy {

    String predict(List<QuoteVO> quoteVOList);
}
