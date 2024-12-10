package ddpg.v3.position;

import ddpg.v3.util.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Position {

    private BigDecimal amount = BigDecimal.ZERO;
    private double positionCnt = 0.0;
    private BigDecimal price = BigDecimal.ZERO;

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public double getPositionCnt() {
        return positionCnt;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /**
     *
     * @param side 0:buy, 1:sell, 2:hold
     */
    public double getPredictReward(BigDecimal price, double positionCnt, Integer side, double mixProfit, double maxProfit) {
        if(side == 0) {
            if( positionCnt == 0) return -1;
            if (amount.compareTo((price.multiply(BigDecimal.valueOf(positionCnt)))) < 0) return -1;

            double v = (this.price.subtract(price)).multiply(BigDecimal.valueOf(positionCnt)).doubleValue();
            return Utils.mapNonLinear(v, mixProfit, maxProfit);
        }

        if(side == 1) {
            if (this.positionCnt == 0) return -1;
            if (this.positionCnt < positionCnt) return -1;

            double v = (price.subtract(this.price)).multiply(BigDecimal.valueOf(positionCnt)).doubleValue();
            return Utils.mapNonLinear(v, mixProfit, maxProfit);
        }

        return 0;
    }

    public double modifyPosition(BigDecimal price, double positionCnt, Integer side) {
        if(side == 0) {
            if( positionCnt == 0) return 0;
            if (amount.compareTo((price.multiply(BigDecimal.valueOf(positionCnt)))) < 0) return 0;

            this.amount = this.amount.subtract(price.multiply(BigDecimal.valueOf(positionCnt)));
            this.price = (this.price.multiply(BigDecimal.valueOf(this.positionCnt))).add (price.multiply(BigDecimal.valueOf(positionCnt))).divide(BigDecimal.valueOf(this.positionCnt + positionCnt), 10, RoundingMode.HALF_UP);
            this.positionCnt = this.positionCnt + positionCnt;
        }

        if(side == 1) {
            if (this.positionCnt == 0) return 0;
            if (this.positionCnt < positionCnt) return 0;

            this.amount = this.amount.add(this.price.multiply(BigDecimal.valueOf(positionCnt)));
            this.positionCnt = this.positionCnt - positionCnt;

            if(this.positionCnt == 0) this.price = BigDecimal.ZERO;

            return this.price.multiply(BigDecimal.valueOf(positionCnt)).doubleValue();
        }

        return 0;
    }
}
