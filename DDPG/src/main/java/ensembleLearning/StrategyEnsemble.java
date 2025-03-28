package ensembleLearning;

import binace.vo.QuoteVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import ddpg.action.position.Position;
import ensembleLearning.event.graph.TradeListEvent;
import ensembleLearning.event.graph.EnsembleInfoEvent;
import ensembleLearning.event.graph.listener.TradeListListener;
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

    public void testStrategy(List<QuoteVO> quoteVOList, List<Strategy> strategies, BigDecimal initAmount, Integer strategyDataTickCnt) throws Exception {
        EventBus eventBus = buildEventBus();
        Position position = new Position();
        position.setAmount(initAmount);
        List<TradeListEvent.TradeVO> tradeVOList = new ArrayList<>();
        EnsembleInfoEvent infoEvent = EnsembleInfoEvent.builder().minAmount(initAmount.doubleValue()).build();
        TradeService tradeService = new TradeService(initAmount);
        EarnService earnService = new EarnService(initAmount);

        for(int i = strategyDataTickCnt; i<quoteVOList.size(); i++) {
            List<QuoteVO> subQuoteVOList = quoteVOList.subList(i- strategyDataTickCnt, i);
            BigDecimal price = subQuoteVOList.getLast().getClose();
            ActionVO actionVO = getStrategyAction(subQuoteVOList, strategies); // 策略動作
            String action = tradeService.getAction(actionVO.getAction(), price, position); // 交易動作
//            doRisk(price, position); // 檢核風控
            tradeService.trade(action, price, position);
            BigDecimal earn = earnService.setEarn(position);
            InfoService.setTradeInfo(action, position, earn, infoEvent); // 記錄交易總計信息

            // graph
            tradeVOList.add(TradeListEvent.TradeVO.builder()
                    .action(action)
                    .closeTime(actionVO.getCloseTime())
                    .price(price)
                    .amount(position.getAmount())
                    .positionCnt(position.getPositionCnt())
                    .positionPrice(position.getPrice())
                .build());
        }

        eventBus.post(TradeListEvent.builder().graphEventVOList(tradeVOList).build());
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
        eventBus.register(new TradeListListener());
        eventBus.register(new EnsembleInfoListener());

        return eventBus;
    }

    private static List<QuoteVO> getDataList() throws Exception {
//        String filePath = "DDPG/data_btc_1m.json";
//        String filePath = "DDPG/data_paxg_1m.json";
        String filePath = "DDPG/data_xrp_1m.json";
//        String filePath = "DDPG/data_link_1m.json";
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
        ensemble.testStrategy(quoteVOList, strategies, initAmount, 7200);
    }
}
