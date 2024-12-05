package ddpg.v1.position;

import java.math.BigDecimal;

public class Position {

    private Integer positionCnt = 0;
    private BigDecimal price = BigDecimal.ZERO;

    public Integer getPositionCnt() {
        return positionCnt;
    }

    public void setPositionCnt(Integer positionCnt) {
        this.positionCnt = positionCnt;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void modifyPosition(BigDecimal price, Integer positionCnt, Integer side) {
        if(side == 1) {
            this.price = this.price.multiply(BigDecimal.valueOf(this.positionCnt))
                .add(price.multiply(BigDecimal.valueOf(positionCnt)))
                .divide(BigDecimal.valueOf(this.positionCnt).add(BigDecimal.valueOf(positionCnt)));
        }
        if(side == -1) {
            this.positionCnt = this.positionCnt - positionCnt;
            this.positionCnt = this.positionCnt < 0 ? 0 : this.positionCnt;
        }
    }
}
