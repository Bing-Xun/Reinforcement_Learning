package ensembleLearning.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import ensembleLearning.strategy.vo.ActionVO;
import ensembleLearning.strategy.vo.StrategyVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrategyUtil {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    // 找到出現次數最高的元素
    public static String findMostFrequentElement(Map<String, Integer> map) {
        String mostFrequentElement = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentElement = entry.getKey();
            }
        }

        return mostFrequentElement;
    }

    public static ActionVO getAction(List<StrategyVO> strategyVOList) throws Exception {
        // 統計每個元素的出現次數
//        List<String> actionList = strategyVOList.stream().map(StrategyVO::getAction).toList();
        List<String> StrategyNameList = strategyVOList.stream().map(StrategyVO::getStrategyName).toList();

        Map<String, Integer> map = new HashMap<>();
        for (StrategyVO vo : strategyVOList) {
            map.put(vo.getAction(), map.getOrDefault(vo.getAction(), 0) + vo.getWeight());
        }

        String mostFrequentElement = StrategyUtil.findMostFrequentElement(map);
//        if(!mostFrequentElement.equals("HOLD")) {
        if(mostFrequentElement.equals("SELL")) {
//            System.out.println("統計結果: " + map);
//            System.out.println("出現次數最高的元素: " + mostFrequentElement);
//            System.out.println("策略名稱: " + objectMapper.writeValueAsString(strategyVOList));
        }

        return ActionVO.builder()
            .action(mostFrequentElement)
            .closeTime(strategyVOList.getLast().getCloseTime())
            .build();
    }

    /**
     * stdDev * Math.sqrt(252); // 年
     * stdDev * Math.sqrt(52); // 週
     * @param prices
     * @param period
     * @return
     */
    public static double calculateHistoricalVolatility(List<Double> prices, int period) {
        if (prices == null || prices.size() < 2) {
            return Double.NaN; // Not enough data
        }

        List<Double> logReturns = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            logReturns.add(Math.log(prices.get(i) / prices.get(i - 1)));
        }

        double stdDev = calculateStandardDeviation(logReturns);
        double annualizedVolatility = stdDev * Math.sqrt(period);

        return annualizedVolatility;
    }

    public static double calculateStandardDeviation(List<Double> data) {
        if (data == null || data.isEmpty()) {
            return Double.NaN;
        }

        double mean = 0;
        for (double value : data) {
            mean += value;
        }
        mean /= data.size();

        double sumOfSquaredDifferences = 0;
        for (double value : data) {
            sumOfSquaredDifferences += Math.pow(value - mean, 2);
        }

        return Math.sqrt(sumOfSquaredDifferences / (data.size() - 1));
    }

    public static void main(String[] args) {
//        System.out.println(calculateStandardDeviation(List.of(12.0, 23.0, 36.0)));
        System.out.println(calculateStandardDeviation(List.of(100.0, 200.0, 300.0, 400.0, 500.0)));
        System.out.println(calculateStandardDeviation(List.of(100.0, 100.0, 100.0, 500.0, 500.0)));
    }

}
