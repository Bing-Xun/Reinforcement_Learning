package ddpg.action.position;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class Position {

    private BigDecimal amount = BigDecimal.ZERO;
    private double positionCnt = 0.0;
    private BigDecimal price = BigDecimal.ZERO;

    public Position() {

    }

    public Position(BigDecimal amount, BigDecimal price, double positionCnt) {
        this.amount = amount;
        this.positionCnt = positionCnt;
        this.price = price;
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

            this.amount = this.amount.add(price.multiply(BigDecimal.valueOf(positionCnt)));
            this.positionCnt = this.positionCnt - positionCnt;

            if(this.positionCnt == 0) this.price = BigDecimal.ZERO;

            return this.price.multiply(BigDecimal.valueOf(positionCnt)).doubleValue();
        }

        return 0;
    }

    public Position getDoAfter(BigDecimal price, double positionCnt, Integer side) {
        Position position = new Position(this.amount, this.price, this.positionCnt);

        if(side == 0) {
            if( positionCnt == 0) {
                return position;
            }
            if (amount.compareTo((price.multiply(BigDecimal.valueOf(positionCnt)))) < 0) {
                return position;
            }

            position.amount = this.amount.subtract(price.multiply(BigDecimal.valueOf(positionCnt)));
            position.price = (this.price.multiply(BigDecimal.valueOf(this.positionCnt))).add (price.multiply(BigDecimal.valueOf(positionCnt))).divide(BigDecimal.valueOf(this.positionCnt + positionCnt), 10, RoundingMode.HALF_UP);
            position.positionCnt = this.positionCnt + positionCnt;
        }

        if(side == 1) {
            if (this.positionCnt == 0) {
                return position;
            }
            if (this.positionCnt < positionCnt) {
                return position;
            }

            position.amount = this.amount.add(this.price.multiply(BigDecimal.valueOf(positionCnt)));
            position.positionCnt = this.positionCnt - positionCnt;

            if(position.positionCnt == 0) {
                position.price = BigDecimal.ZERO;
            }

            position.price.multiply(BigDecimal.valueOf(positionCnt)).doubleValue();
        }

        return position;
    }
}
