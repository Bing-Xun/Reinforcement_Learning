package ddpg.v2.position;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Position {

    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal positionCnt = BigDecimal.ZERO;
    private BigDecimal price = BigDecimal.ZERO;

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPositionCnt() {
        return positionCnt;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public double modifyPosition(BigDecimal price, BigDecimal positionCnt, Integer side) {
        if(side == 0) {
            if(positionCnt.compareTo(BigDecimal.ZERO) == 0) {
                return -1;
            }

            if(amount.compareTo((price.multiply(positionCnt))) == -1) {
                return -1;
            }

            this.amount = this.amount.subtract(price.multiply(positionCnt));
            this.price = (this.price.multiply(this.positionCnt)).add (price.multiply(positionCnt)).divide(this.positionCnt.add(positionCnt), 10, RoundingMode.HALF_UP);
            this.positionCnt = this.positionCnt.add(positionCnt);
        }

        if(side == 1) {
            if(this.positionCnt.compareTo(BigDecimal.ZERO) == -1) {
                return -1;
            }

            if(this.positionCnt.compareTo(positionCnt) == -1) {
                return -1;
            }

            this.amount = this.amount.add(this.price.multiply(positionCnt));
            this.positionCnt = this.positionCnt.subtract(positionCnt);

            if(this.positionCnt.compareTo(BigDecimal.ZERO) == 0) {
                this.price = BigDecimal.ZERO;
            }

            return this.price.multiply(positionCnt).doubleValue();
        }

        return 0;
    }
}
