package binace.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuoteVO {

    private Long openTime; // 开盘时间 (Unix 时间戳)
    private BigDecimal open; // 开盘价
    private BigDecimal high; // 最高价
    private BigDecimal low; // 最低价
    private BigDecimal close; // 收盘价
    private BigDecimal volume; // 成交量
    private Long closeTime; // 收盘时间 (Unix 时间戳)
    private BigDecimal quoteAssetVolume; // 成交额 (计价货币)
    private Long trades; // 成交笔数
    private BigDecimal takerBuyBaseAssetVolume; // 主动买入成交量 (基础货币)
    private BigDecimal takerBuyQuoteAssetVolume; // 主动买入成交额 (计价货币)

    public QuoteVO() {

    }

    public QuoteVO(double[] d) {
        this.openTime = (long) d[0];
        this.open = BigDecimal.valueOf(d[1]);
        this.high = BigDecimal.valueOf(d[2]);
        this.low = BigDecimal.valueOf(d[3]);
        this.close = BigDecimal.valueOf(d[4]);
        this.volume = BigDecimal.valueOf(d[5]);
        this.closeTime = (long) d[6];
        this.quoteAssetVolume = BigDecimal.valueOf(d[7]);
        this.trades = (long) d[8];
        this.takerBuyBaseAssetVolume = BigDecimal.valueOf(d[9]);
        this.takerBuyQuoteAssetVolume = BigDecimal.valueOf(d[10]);
    }

    public double[] toArray () {
        return new double[] {
            this.openTime.doubleValue()
            , this.open.doubleValue()
            , this.high.doubleValue()
            , this.low.doubleValue()
            , this.close.doubleValue()
            , this.volume.doubleValue()
            , this.closeTime.doubleValue()
            , this.quoteAssetVolume.doubleValue()
            , this.trades.doubleValue()
            , this.takerBuyBaseAssetVolume.doubleValue()
            , this.takerBuyQuoteAssetVolume.doubleValue()
        };
    }
}
