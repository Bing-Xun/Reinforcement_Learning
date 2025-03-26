package ensembleLearning.service;

import ddpg.action.position.Position;

import java.math.BigDecimal;

public class ActionService {

    // trade volume
    private double buyRate = 0.1;
    private double rateTick = 0.01;
    private int buySellCnt = 0;

    public String getAction(String strategyAction, BigDecimal price, Position position, BigDecimal buyPrice, BigDecimal sellPrice) throws Exception {
        String action = strategyAction;

        if("BUY".equals(strategyAction)) {
            action = "HOLD";
            if(Math.abs(price.doubleValue() - buyPrice.doubleValue()) > price.doubleValue() * 0.015) {
                if(position.getAmount().doubleValue() > getBuyTradePosition(price, new BigDecimal(1000)) * price.doubleValue()) {
                    position.modifyPosition(price, getBuyTradePosition(price, new BigDecimal(1000)), 0);
                    action = "BUY";
                }
            }
        }

        if("SELL".equals(action)) {
            action = "HOLD";
            if(Math.abs(price.doubleValue() - sellPrice.doubleValue()) > price.doubleValue() * 0.015) {
                if(position.getPositionCnt() > 0.0) {
                    position.modifyPosition(price, position.getPositionCnt(), 1);
                    action = "SELL";
                }
            }
        }

        return action;
    }

    private Double getBuyTradePosition(BigDecimal price, BigDecimal initAmount) {
        double rate = buyRate + ((rateTick * (buySellCnt < 0 ? 0 : buySellCnt)));
        return initAmount.doubleValue() * rate / price.doubleValue();
    }
}
