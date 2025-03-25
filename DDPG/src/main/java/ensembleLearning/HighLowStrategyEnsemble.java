package ensembleLearning;

import binace.vo.QuoteVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ddpg.action.position.Position;
import ensembleLearning.strategy.Strategy;
import ensembleLearning.strategy.impl.*;
import ensembleLearning.strategy.util.StrategyUtil;
import ensembleLearning.strategy.vo.ActionVO;
import ensembleLearning.strategy.vo.StrategyVO;
import graph.DataPoint;
import graph.PriceChart;
import org.reflections.Reflections;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class HighLowStrategyEnsemble {

    private static ObjectMapper objectMapper = new ObjectMapper();;

    public static void main(String[] args) throws Exception {
        HighLowStrategyEnsemble ensemble = new HighLowStrategyEnsemble();
        // 動態加載所有策略
//        List<Strategy> strategies = loadStrategies();
        List<Strategy> strategies = List.of(
            new HighLowLongStrategy()
            , new TrendStrategy()
        );

        BigDecimal initAmount = new BigDecimal(1000);
        Position position = new Position();
        position.setAmount(initAmount);
        BigDecimal buyPrice = BigDecimal.ZERO;
        BigDecimal sellPrice = BigDecimal.ZERO;
        BigDecimal earnAmount = BigDecimal.ZERO;

        List<DataPoint> dataPoints = new ArrayList<>();
        List<DataPoint> earnPoints = new ArrayList<>();
        List<DataPoint> amountPoints = new ArrayList<>();
        List<QuoteVO> quoteVOList = getDataList();
        int holdCnt = 0;
        int sellCnt = 0;
        int buyCnt = 0;
        int tickI = 7200;
        for(int i=tickI; i<quoteVOList.size(); i++) {
            List<QuoteVO> longList = quoteVOList.subList(i-tickI, i);
            BigDecimal price = longList.getLast().getClose();
            Long closeTime = longList.getLast().getCloseTime();

            List<StrategyVO> strategyVOList = new ArrayList<>();
            // 執行每個策略的 predict 方法
            for (Strategy strategy : strategies) {
                StrategyVO vo = strategy.predict(longList);
                strategyVOList.add(vo);
            }

            ActionVO actionVO = StrategyUtil.getAction(strategyVOList);
            String action = actionVO.getAction();

            if("HOLD".equals(action)) {
                holdCnt++;
            }

            if("BUY".equals(action)) {
                action = "HOLD";
//                if(Math.abs(price.doubleValue() - position.getPrice().doubleValue()) > price.doubleValue() * 0.06) {
                if(Math.abs(price.doubleValue() - buyPrice.doubleValue()) > price.doubleValue() * 0.015) {
                    if(position.getAmount().doubleValue() > getBuyTradePosition(price, new BigDecimal(1000)) * price.doubleValue()) {
                        position.modifyPosition(price, getBuyTradePosition(price, new BigDecimal(1000)), 0);
                        buyPrice = price;
                        buyCnt++;
                        buySellCnt += 1;
                        buySellCnt = Math.min(5, buySellCnt);

                        maxAmount = Math.max(position.getAmount().doubleValue(), maxAmount);
                        minAmount = Math.min(position.getAmount().doubleValue(), minAmount);
                        action = "BUY";
                    }
                }
            }

            if("SELL".equals(action)) {
                action = "HOLD";
                if(Math.abs(price.doubleValue() - sellPrice.doubleValue()) > price.doubleValue() * 0.015) {
//                    if(position.getPositionCnt() > getSellTradePosition(price, new BigDecimal(1000))) {
//                        position.modifyPosition(price, getSellTradePosition(price, new BigDecimal(1000)), 1);
                    if(position.getPositionCnt() > 0.0) {
//                        System.out.println(String.format("price:%s, amount%s, positionPrice:%s, positionCnt:%s", price, position.getAmount(), position.getPrice(), position.getPositionCnt()));
                        position.modifyPosition(price, position.getPositionCnt(), 1);
//                        System.out.println(String.format("positionAmount:%s", position.getAmount()));

                        if(position.getAmount().doubleValue() > 1000.0) {
                            System.out.println(position);
                        }

                        sellPrice = price;
                        sellCnt++;
                        buySellCnt -= 1;
                        buySellCnt = Math.max(-5, buySellCnt);

                        if(position.getAmount().compareTo(initAmount) == 1) {
                            BigDecimal earn = position.getAmount().subtract(initAmount);
                            BigDecimal earnPer = new BigDecimal(earn.doubleValue() * 1);
                            earnAmount = earnAmount.add(earnPer);
                            position.setAmount(position.getAmount().subtract(earnPer));
                        }

                        maxAmount = Math.max(position.getAmount().doubleValue(), maxAmount);
                        minAmount = Math.min(position.getAmount().doubleValue(), minAmount);

                        action = "SELL";
                    }
                }
            }

            // 多兩個風控條件
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
//            if((positionPrice + (positionPrice * 0.05)) > priceD) {
//                position.modifyPosition(price, position.getPositionCnt() / 10, 1);
//            }

//            System.out.println("###");
//            System.out.println(position.getAmount());
//            System.out.println(position.getPositionCnt());

            DataPoint dataPoint = new DataPoint(
                price
                , new BigDecimal(position.getPositionCnt())
                , closeTime
                , action.equals("HOLD") ? "" : actionVO.getAction() + ":" + actionVO.getCloseTime());
//                , "");
            dataPoints.add(dataPoint);

            double b = position.getPositionCnt() * price.doubleValue();
            DataPoint earnPoint = new DataPoint(
                new BigDecimal(b).add(position.getAmount())
                , new BigDecimal(position.getPositionCnt())
                , closeTime
//                , action.equals("HOLD") ? "" : actionVO.getAction() + ":" + actionVO.getCloseTime());
                , "");
            earnPoints.add(earnPoint);

            DataPoint amountPoint = new DataPoint(
                position.getAmount()
                , new BigDecimal(position.getPositionCnt())
                , closeTime
//                , action.equals("HOLD") ? "" : actionVO.getAction() + ":" + actionVO.getCloseTime());
                , "");
            amountPoints.add(amountPoint);

            if(Set.of("BUY", "SELL").contains(action)) {
                System.out.println(position.getAmount().add(new BigDecimal(b)) + ":" + position.getAmount());
            }
        }

        System.out.println("holdCnt:"+holdCnt);
        System.out.println("sellCnt:"+sellCnt);
        System.out.println("buyCnt:"+buyCnt);
        System.out.println("earnAmount:"+earnAmount);
        System.out.println("minAmount:"+minAmount);
        System.out.println("maxAmount:"+maxAmount);

//        PriceChart.plot(dataPoints);
//        PriceChart.plot(earnPoints);
//        PriceChart.plot(amountPoints);
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

    private static double buyRate = 0.1;
    private static double sellRate = 0.1;
    private static double rateTick = 0.01;
    private static double maxAmount = 0.0;
    private static double minAmount = 1000;
    private static int buySellCnt = 0;

    private static Double getBuyTradePosition(BigDecimal price, BigDecimal initAmount) {
        double rate = buyRate + ((rateTick * (buySellCnt < 0 ? 0 : buySellCnt)));
//        System.out.println("buySellCnt:"+buySellCnt);
//        System.out.println("buyRate:"+rate);
        return initAmount.doubleValue() * rate / price.doubleValue();
    }

    private static Double getSellTradePosition(BigDecimal price, BigDecimal initAmount) {
        double rate = sellRate + ((rateTick * (buySellCnt > 0 ? 0 : -buySellCnt)));
        return initAmount.doubleValue() * rate / price.doubleValue();
    }
}
