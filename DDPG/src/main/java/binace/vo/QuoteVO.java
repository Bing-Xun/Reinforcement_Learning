package binace.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
