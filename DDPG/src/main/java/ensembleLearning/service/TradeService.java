package ensembleLearning.service;

import ddpg.action.position.Position;

import java.math.BigDecimal;

public class TradeService {

    private BigDecimal buyPrice = BigDecimal.ZERO;
    private BigDecimal sellPrice = BigDecimal.ZERO;
    private BigDecimal initAmount;
    private double buyRate = 0.1;
    private double rateTick = 0.01;
    private int buySellCnt = 0;

    public TradeService(BigDecimal initAmount) {
        this.initAmount = initAmount;
    }

    public String getAction(String strategyAction, BigDecimal price, Position position) throws Exception {
        String action = strategyAction;

        if("BUY".equals(strategyAction)) {
            action = checkBuyAction(price, position) ? "BUY" : "HOLD";
        }

        if("SELL".equals(action)) {
            action = checkSellAction(price, position) ? "SELL" : "HOLD";
        }

        return action;
    }

    public void trade(String action, BigDecimal price, Position position) {
        if("BUY".equals(action) && checkBuyAction(price, position)) {
            position.modifyPosition(price, getBuyTradePosition(price, initAmount), 0);
            setTradeVolume(action, price);
        }

        if("SELL".equals(action) && checkSellAction(price, position)) {
            Double positionCnt = position.getPositionCnt();
            Double priceD = price.doubleValue();
            boolean isPCntOver = positionCnt * priceD > initAmount.doubleValue() / 3;
            Double sellVolume = isPCntOver ? positionCnt / 4 : positionCnt;

            position.modifyPosition(price, sellVolume, 1);
            setTradeVolume(action, price);
        }
    }

    private boolean checkBuyAction(BigDecimal price, Position position) {
        boolean b = false;
        if(Math.abs(price.doubleValue() - buyPrice.doubleValue()) > price.doubleValue() * 0.015) {
            if(position.getAmount().doubleValue() > getBuyTradePosition(price, initAmount) * price.doubleValue()) {
                b = true;
            }
        }
        return b;
    }

    private boolean checkSellAction(BigDecimal price, Position position) {
        boolean b = false;
        if(Math.abs(price.doubleValue() - sellPrice.doubleValue()) > price.doubleValue() * 0.015) {
            if(position.getPositionCnt() > 0.0) {
                b = true;
            }
        }
        return b;
    }

    private void setTradeVolume(String action, BigDecimal price) {
        if("BUY".equals(action)) {
            buySellCnt += 1;
            buySellCnt = Math.min(5, buySellCnt);
            buyPrice = price;
        }

        if("SELL".equals(action)) {
            buySellCnt -= 1;
            buySellCnt = Math.max(-5, buySellCnt);
            sellPrice = price;
        }
    }

    private Double getBuyTradePosition(BigDecimal price, BigDecimal initAmount) {
        double rate = buyRate + ((rateTick * (buySellCnt < 0 ? 0 : buySellCnt)));
        return initAmount.doubleValue() * rate / price.doubleValue();
    }
}
