package ensembleLearning;

import binace.vo.QuoteVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ddpg.action.position.Position;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.impl.*;
import ensembleLearning.strategy.vo.ActionVO;
import ensembleLearning.strategy.vo.StrategyVO;
import graph.DataPoint;
import graph.PriceChart;
import org.reflections.Reflections;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class MultiStrategyEnsemble {

    private static ObjectMapper objectMapper = new ObjectMapper();;

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

    private ActionVO getAction(List<StrategyVO> strategyVOList) throws Exception {
        // 統計每個元素的出現次數
        List<String> actionList = strategyVOList.stream().map(StrategyVO::getAction).toList();
        List<String> StrategyNameList = strategyVOList.stream().map(StrategyVO::getStrategyName).toList();

        Map<String, Integer> map = new HashMap<>();
        for (String element : actionList) {
            map.put(element, map.getOrDefault(element, 0) + 1);
        }

        String mostFrequentElement = findMostFrequentElement(map);
        if(!mostFrequentElement.equals("HOLD")) {
//            System.out.println("統計結果: " + map);
//            System.out.println("出現次數最高的元素: " + mostFrequentElement);
//            System.out.println("策略名稱: " + objectMapper.writeValueAsString(strategyVOList));
        }

        return ActionVO.builder()
            .action(mostFrequentElement)
            .closeTime(strategyVOList.getLast().getCloseTime())
            .build();
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
//        List<Strategy> strategies = loadStrategies();
        List<Strategy> strategies = List.of(new HighLowStrategy()); // holdCnt:5270 sellCnt:1589 buyCnt:1890
//        List<Strategy> strategies = List.of(new BollingerBandsStrategy()); // holdCnt:6573 sellCnt:1003 buyCnt:1173
//        List<Strategy> strategies = List.of(new CCIAndPSYStrategy()); // holdCnt:7608 sellCnt:203 buyCnt:938
//        List<Strategy> strategies = List.of(new ChaikinOscillatorStrategy()); // holdCnt:7151 sellCnt:803 buyCnt:795
//        List<Strategy> strategies = List.of(new EMAStrategy()); // holdCnt:7997 sellCnt:376 buyCnt:376
//        List<Strategy> strategies = List.of(new MACDStrategy()); // holdCnt:8035 sellCnt:364 buyCnt:350
//        List<Strategy> strategies = List.of(new MFIStrategy()); // holdCnt:8478 sellCnt:122 buyCnt:149
//        List<Strategy> strategies = List.of(new MomentumROCStrategy()); // holdCnt:736 sellCnt:693 buyCnt:693
//        List<Strategy> strategies = List.of(new OBVStrategy()); // holdCnt:10 sellCnt:4382 buyCnt:4357
//        List<Strategy> strategies = List.of(new ParabolicSARStrategy()); // holdCnt:0 sellCnt:0 buyCnt:8749
//        List<Strategy> strategies = List.of(new RSIStrategy()); // holdCnt:8465 sellCnt:122 buyCnt:162
//        List<Strategy> strategies = List.of(new StochasticOscillatorStrategy()); // holdCnt:8182 sellCnt:305 buyCnt:262
//        List<Strategy> strategies = List.of(new WilliamsRStrategy()); // holdCnt:7457 sellCnt:648 buyCnt:644
//        List<Strategy> strategies = List.of(
////                new HighLowStrategy()
//                new BollingerBandsStrategy()
//                , new CCIAndPSYStrategy()
//                , new OBVStrategy()
//                , new WilliamsRStrategy()
//        );

//        List<Strategy> strategies = List.of(
//                new HighLowStrategy()
//                , new EMAStrategy()
//                , new MACDStrategy()
//                , new RSIStrategy()
//        );


        Position position = new Position();
        position.setAmount(new BigDecimal(1000));

        List<DataPoint> dataPoints = new ArrayList<>();
        List<QuoteVO> quoteVOList = getDataList();
        int holdCnt = 0;
        int sellCnt = 0;
        int buyCnt = 0;
        int tickI = 300;
        for(int i=tickI; i<quoteVOList.size(); i++) {
            List<QuoteVO> subList = quoteVOList.subList(i-tickI, i);
            BigDecimal price = subList.getLast().getClose();

            List<StrategyVO> strategyVOList = new ArrayList<>();
            // 執行每個策略的 predict 方法
            for (Strategy strategy : strategies) {
                StrategyVO vo = strategy.predict(subList);
//            System.out.println(prediction);
                strategyVOList.add(vo);
            }

            ActionVO actionVO = ensemble.getAction(strategyVOList);
            String action = actionVO.getAction();

            if("HOLD".equals(action)) {
                holdCnt++;
            }

            if("BUY".equals(action)) {
//                position.modifyPosition(price, 0.0008, 0);
                if(Math.abs(price.doubleValue() - position.getPrice().doubleValue()) > price.doubleValue() * 0.05) {
                    position.modifyPosition(price, getBuyTradePosition(price, new BigDecimal(1000)), 0);
                    buyCnt++;
                } else {
                    action = "HOLD";
                }
            }

            if("SELL".equals(action)) {
//                position.modifyPosition(price, 0.0008, 1);
                if(Math.abs(price.doubleValue() - position.getPrice().doubleValue()) > price.doubleValue() * 0.05) {
                    position.modifyPosition(price, getSellTradePosition(price, new BigDecimal(1000)), 1);
                    sellCnt++;
                } else {
                    action = "HOLD";
                }
            }

            // 多兩個風控條件
            Double priceD = price.doubleValue();
            Double positionPrice = position.getPrice().doubleValue();
//            if((positionPrice - (positionPrice * 0.1)) > priceD) {
//                position.modifyPosition(price, position.getPositionCnt(), 1);
//            }
//            if((positionPrice + (positionPrice * 0.05)) > priceD) {
//                position.modifyPosition(price, position.getPositionCnt() / 10, 1);
//            }

//            System.out.println("###");
//            System.out.println(position.getAmount());
//            System.out.println(position.getPositionCnt());

            DataPoint dataPoint = new DataPoint(
                price
                , new BigDecimal(position.getPositionCnt())
                , subList.getLast().getCloseTime()
//                , action.equals("HOLD") ? "" : actionVO.getAction() + ":" + actionVO.getCloseTime());
                , "");

            dataPoints.add(dataPoint);

            if(Set.of("BUY", "SELL").contains(action)) {
                double b = position.getPositionCnt() * price.doubleValue();
                System.out.println(position.getAmount().add(new BigDecimal(b)));
            }
        }

        System.out.println("holdCnt:"+holdCnt);
        System.out.println("sellCnt:"+sellCnt);
        System.out.println("buyCnt:"+buyCnt);
        PriceChart.plot(dataPoints);
    }

    private static List<QuoteVO> getDataList() throws Exception {
        String filePath = "DDPG/data1m.json";
//        String filePath = "DDPG/data3m.json";
//        String filePath = "DDPG/data5m.json";
//        String filePath = "DDPG/data15m.json";
//        String filePath = "DDPG/data1h.json";

        ObjectMapper objectMapper = new ObjectMapper();
        List<QuoteVO> list = objectMapper.readValue(new File(filePath), new TypeReference<>(){});

        return list;
//        return list.subList(list.size() - 20000, list.size() - 10000);
    }

    private static Double getBuyTradePosition(BigDecimal price, BigDecimal initAmount) {
        return initAmount.doubleValue() * 0.25 / price.doubleValue();
    }

    private static Double getSellTradePosition(BigDecimal price, BigDecimal initAmount) {
        return initAmount.doubleValue() * 0.25 / price.doubleValue();
    }
}
