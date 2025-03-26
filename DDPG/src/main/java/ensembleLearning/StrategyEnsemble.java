package ensembleLearning;

import binace.vo.QuoteVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import ddpg.action.position.Position;
import ensembleLearning.service.ActionService;
import ensembleLearning.event.graph.GraphEvent;
import ensembleLearning.event.graph.EnsembleInfoEvent;
import ensembleLearning.event.graph.listener.GraphEventListener;
import ensembleLearning.event.graph.listener.EnsembleInfoListener;
import ensembleLearning.service.EarnService;
import ensembleLearning.service.InfoService;
import ensembleLearning.service.TradeService;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.impl.*;
import ensembleLearning.util.StrategyUtil;
import ensembleLearning.strategy.vo.ActionVO;
import ensembleLearning.strategy.vo.StrategyVO;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class StrategyEnsemble {

    public void testStrategy(List<QuoteVO> quoteVOList, List<Strategy> strategies, BigDecimal initAmount) throws Exception {
        EventBus eventBus = buildEventBus();
        Position position = new Position();
        position.setAmount(initAmount);
        List<GraphEvent.GraphEventVO> graphEventList = new ArrayList<>();
        EnsembleInfoEvent infoEvent = EnsembleInfoEvent.builder().minAmount(initAmount.doubleValue()).build();
        TradeService tradeService = new TradeService(initAmount);
        ActionService ensembleAction = new ActionService();
        EarnService earnService = new EarnService(initAmount);

        int tickI = 7200;
        for(int i=tickI; i<quoteVOList.size(); i++) {
            List<QuoteVO> subQuoteVOList = quoteVOList.subList(i-tickI, i);
            BigDecimal price = subQuoteVOList.getLast().getClose();
            ActionVO actionVO = getStrategyAction(subQuoteVOList, strategies); // 策略動作
            String action = ensembleAction.getAction(actionVO.getAction(), price, position, tradeService.getBuyPrice(), tradeService.getSellPrice()); // 交易動作
//            doRisk(price, position); // 檢核風控
            tradeService.trade(action, price, position);
            BigDecimal earn = earnService.setEarn(position);
            InfoService.setTradeInfo(action, position, earn, infoEvent); // 記錄交易總計信息

            // graph
            graphEventList.add(GraphEvent.GraphEventVO.builder()
                    .action(action)
                    .closeTime(actionVO.getCloseTime())
                    .price(price)
                    .amount(position.getAmount())
                    .positionCnt(position.getPositionCnt())
                    .positionPrice(position.getPrice())
                .build());

            double b = position.getPositionCnt() * price.doubleValue();
            if(Set.of("BUY", "SELL").contains(action)) {
                System.out.println(position.getAmount().add(new BigDecimal(b)) + ":" + position.getAmount());
            }
        }

        eventBus.post(GraphEvent.builder().graphEventVOList(graphEventList).build());
        eventBus.post(infoEvent);
    }

    private static void doRisk(BigDecimal price, Position position) {
        Double priceD = price.doubleValue();
        Double positionPrice = position.getPrice().doubleValue();
        if((positionPrice - (positionPrice * 0.05)) > priceD) {
//                System.out.println("### :"+position.getPositionCnt() + ":" + positionPrice + ":" + priceD);
//                position.modifyPosition(price, position.getPositionCnt(), 1);
//                maxAmount = Math.max(position.getAmount().doubleValue(), maxAmount);
//                minAmount = Math.min(position.getAmount().doubleValue(), minAmount);
//                sellPrice = price;
//                buyPrice = price;
//
//                action = "SELL";
        }
    }

    private ActionVO getStrategyAction(List<QuoteVO> quoteVOList, List<Strategy> strategies) throws Exception {
        List<StrategyVO> strategyVOList = new ArrayList<>();
        // 執行每個策略的 predict 方法
        for (Strategy strategy : strategies) {
            StrategyVO vo = strategy.predict(quoteVOList);
            strategyVOList.add(vo);
        }

        return StrategyUtil.getAction(strategyVOList);
    }

    private EventBus buildEventBus() {
        EventBus eventBus = new EventBus();
        eventBus.register(new GraphEventListener());
        eventBus.register(new EnsembleInfoListener());

        return eventBus;
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
    }

    public static void main(String[] args) throws Exception {
        List<Strategy> strategies = List.of(
            new HighLowStrategy()
            , new TrendStrategy()
        );
        BigDecimal initAmount = new BigDecimal(1000);
        List<QuoteVO> quoteVOList = getDataList();

        StrategyEnsemble ensemble = new StrategyEnsemble();
        ensemble.testStrategy(quoteVOList, strategies, initAmount);
    }
}
