package ensembleLearning.service;

import ddpg.action.position.Position;
import ensembleLearning.event.graph.EnsembleInfoEvent;

import java.math.BigDecimal;

public class InfoService {

    public static EnsembleInfoEvent setTradeInfo(String action, Position position, BigDecimal earn, EnsembleInfoEvent infoEvent) {
        if("HOLD".equals(action)) {
            infoEvent.setHoldCnt(infoEvent.getHoldCnt()+1);
        }

        if("BUY".equals(action)) {
            infoEvent.setBuyCnt(infoEvent.getBuyCnt()+1);
            infoEvent.setMaxAmount(Math.max(position.getAmount().doubleValue(), infoEvent.getMaxAmount()));
            infoEvent.setMinAmount(Math.min(position.getAmount().doubleValue(), infoEvent.getMinAmount()));
        }

        if("SELL".equals(action)) {
            infoEvent.setSellCnt(infoEvent.getSellCnt()+1);
            infoEvent.setMaxAmount(Math.max(position.getAmount().doubleValue(), infoEvent.getMaxAmount()));
            infoEvent.setMinAmount(Math.min(position.getAmount().doubleValue(), infoEvent.getMinAmount()));
        }

        if(earn != null && earn.compareTo(BigDecimal.ZERO) == 1) {
            infoEvent.setEarnAmount(infoEvent.getEarnAmount().add(earn));
        }

        return infoEvent;
    }
}
