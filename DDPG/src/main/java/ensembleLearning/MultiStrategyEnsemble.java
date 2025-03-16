package ensembleLearning;

import binace.vo.QuoteVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ddpg.action.position.Position;
import ensembleLearning.strategy.Strategy;
import org.reflections.Reflections;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class MultiStrategyEnsemble {


    // 使用 Reflections 掃描類路徑
    public static List<Strategy> loadStrategies() {
        List<Strategy> strategies = new ArrayList<>();

        // 掃描指定包下的所有類
        Reflections reflections = new Reflections("ensembleLearning.strategy"); // 替換為你的包名
        Set<Class<? extends Strategy>> strategyClasses = reflections.getSubTypesOf(Strategy.class);

        // 實例化每個類
        for (Class<? extends Strategy> clazz : strategyClasses) {
            try {
                Strategy strategy = clazz.getDeclaredConstructor().newInstance();
                strategies.add(strategy);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return strategies;
    }

    private String getAction(List<String> list) {
        // 統計每個元素的出現次數
        Map<String, Integer> map = new HashMap<>();
        for (String element : list) {
            map.put(element, map.getOrDefault(element, 0) + 1);
        }

        // 打印 Map
        System.out.println("統計結果: " + map);

        // 找到出現次數最高的元素
        String mostFrequentElement = findMostFrequentElement(map);
        System.out.println("出現次數最高的元素: " + mostFrequentElement);

        return mostFrequentElement;
    }

    // 找到出現次數最高的元素
    private static String findMostFrequentElement(Map<String, Integer> map) {
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

    public static void main(String[] args) throws Exception {
        MultiStrategyEnsemble ensemble = new MultiStrategyEnsemble();
        // 動態加載所有策略
        List<Strategy> strategies = loadStrategies();

        Position position = new Position();
        position.setAmount(new BigDecimal(1000));

        List<QuoteVO> quoteVOList = getDataList();
        for(int i=30; i<quoteVOList.size(); i++) {
            List<QuoteVO> subList = quoteVOList.subList(i-30, i);
            BigDecimal price = subList.getLast().getClose();

            List<String> actionList = new ArrayList<>();
            // 執行每個策略的 predict 方法
            for (Strategy strategy : strategies) {
                String prediction = strategy.predict(subList);
//            System.out.println(prediction);
                actionList.add(prediction);
            }

            String s = ensemble.getAction(actionList);
            if("BUY".equals(s)) {
                position.modifyPosition(price, 0.0008, 0);
            }
            if("SELL".equals(s)) {
                position.modifyPosition(price, 0.0008, 1);
            }

            System.out.println("###");
//            System.out.println(position.getAmount());
//            System.out.println(position.getPositionCnt());

            double b = position.getPositionCnt() * price.doubleValue();
            System.out.println(position.getAmount().add(new BigDecimal(b)));
        }
    }

    private static List<QuoteVO> getDataList() throws Exception {
        String filePath = "DDPG/data1m.json";
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<>(){});
    }

}
