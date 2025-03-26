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

    public void trade(String action, BigDecimal price, Position position) {
        if("BUY".equals(action)) {
            if(Math.abs(price.doubleValue() - buyPrice.doubleValue()) > price.doubleValue() * 0.015) {
                if(position.getAmount().doubleValue() > getBuyTradePosition(price, initAmount) * price.doubleValue()) {
                    position.modifyPosition(price, getBuyTradePosition(price, initAmount), 0);
                    setTradeVolume(action, price);
                }
            }
        }

        if("SELL".equals(action)) {
            if(Math.abs(price.doubleValue() - sellPrice.doubleValue()) > price.doubleValue() * 0.015) {
                if(position.getPositionCnt() > 0.0) {
                    position.modifyPosition(price, position.getPositionCnt(), 1);
                    setTradeVolume(action, price);
                }
            }
        }
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

    public BigDecimal getBuyPrice() {
        return this.buyPrice;
    }

    public BigDecimal getSellPrice() {
        return this.sellPrice;
    }
}
