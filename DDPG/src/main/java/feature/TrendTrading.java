package feature;

import ensembleLearning.util.HighLowStrategyUtil;
import ensembleLearning.util.StrategyUtil;
import ensembleLearning.strategy.vo.HighLowTradingVO;

import java.util.Arrays;
import java.util.List;

public class TrendTrading {

    /**
     * 空頭走勢（Bear Market）：
     *
     * 波動性較高：
     * 空頭市場通常伴隨著較高的恐慌情緒，投資者急於拋售股票，導致股價大幅波動。
     * 下跌趨勢往往比上漲趨勢更為劇烈，短時間內可能出現大幅下跌。
     * 特性：
     * 投資者普遍對市場前景感到悲觀。
     * 交易量可能增加，尤其是在股價下跌時。
     * 市場情緒容易受到負面消息影響，如經濟衰退、政治不穩定等。
     * 大漲大跌：
     * 在空頭市場中，雖然整體趨勢是下跌，但也會出現反彈。然而，這些反彈通常短暫且不穩定，隨後可能出現更劇烈的下跌。
     * 多頭走勢（Bull Market）：
     *
     * 波動性相對較低：
     * 多頭市場中，投資者信心較強，股價呈現穩定上升趨勢。
     * 波動性通常較空頭市場低，但仍可能受到短期因素影響。
     * 特性：
     * 投資者對市場前景感到樂觀。
     * 交易量可能穩定增加，或隨股價上漲而增加。
     * 市場情緒容易受到正面消息影響，如經濟成長、公司獲利等。
     * 漸進式上漲：
     * 多頭市場中，股價通常以漸進式上漲為主，偶爾出現回檔整理，但整體趨勢向上。
     * 總結：
     *
     * 空頭市場的波動性通常高於多頭市場，且容易出現大漲大跌。
     * 多頭市場的股價呈現穩定上升趨勢，波動性相對較低。
     */
    public static HighLowTradingVO generateSignals(double[] closePrices) {
        // 先判定空頭
        List<Double> closePricesList = Arrays.stream(closePrices)
                .boxed() // 將 double 轉換為 Double
                .collect(java.util.stream.Collectors.toList());

        Double indexD = closePrices[closePrices.length-1];
        List<Double> decileList = HighLowStrategyUtil.calculatePercentiles(closePricesList);
        int decile = HighLowStrategyUtil.getPercentile(indexD, decileList);

        String action = "HOLD";
        Integer weight = 1;

        if(decile < 15) {
            List<Double> list = Arrays.stream(closePrices)
                .boxed() // 將 double 轉換為 Double
                .collect(java.util.stream.Collectors.toList());

            int middleIndex = closePricesList.size() / 2; // 計算中間索引
            List<Double> preClosePricesList = closePricesList.subList(0, middleIndex); // 取得前半部分的子列表
            List<Double> sufClosePricesList = closePricesList.subList(middleIndex, closePricesList.size()); // 取得後半部分的子列表
            double preSD = StrategyUtil.calculateStandardDeviation(preClosePricesList);
            double sufSD = StrategyUtil.calculateStandardDeviation(sufClosePricesList);

            if(preSD < sufSD) {
                action = "HOLD";
                weight = 5;
            }
        }

        return HighLowTradingVO.builder()
            .action(action)
            .weight(weight)
            .build();
    }

    public static void main(String[] args) {
        // 示例數據
        double[] closePrices = {
            25, 26, 27, 26, 28, 29, 30, 29, 31, 32,
            31, 30, 29, 28, 27, 28, 29, 30, 31, 32,
            33, 34, 35, 34, 33, 32, 31, 30, 29, 28
        };
        double[] highPrices = new double[closePrices.length];
        double[] lowPrices = new double[closePrices.length];

        System.out.println(generateSignals(closePrices));
    }
}