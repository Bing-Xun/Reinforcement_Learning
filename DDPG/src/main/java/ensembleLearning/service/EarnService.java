package ensembleLearning.service;

import ddpg.action.position.Position;

import java.math.BigDecimal;

public class EarnService {

    private BigDecimal initAmount;

    public EarnService(BigDecimal initAmount) {
        this.initAmount = initAmount;
    }

    public BigDecimal setEarn(Position position) {
        BigDecimal earn = BigDecimal.ZERO;

        if(position.getAmount().compareTo(initAmount) == 1) {
            earn = position.getAmount().subtract(initAmount);
            BigDecimal earnPer = new BigDecimal(earn.doubleValue() * 1);
            position.setAmount(position.getAmount().subtract(earnPer));
        }

        return earn;
    }
}
