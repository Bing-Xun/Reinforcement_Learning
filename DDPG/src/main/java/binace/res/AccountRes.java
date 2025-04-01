package binace.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略 JSON 中的未知字段
@Data
public class AccountRes {

    /**
     * Maker 手續費率（掛單，提供流動性），單位為 0.1%（例如 10 = 1%）
     */
    private Integer makerCommission;

    /**
     * Taker 手續費率（吃單，消耗流動性），單位同上。
     */
    private Integer takerCommission;

    /**
     * 買方手續費率（現已棄用，通常與 takerCommission 相同）。
     */
    private Integer buyerCommission;

    /**
     * 賣方手續費率（現已棄用，通常與 makerCommission 相同）。
     */
    private Integer sellerCommission;

    /**
     * 新版手續費結構（字符串格式，精確到小數點後 8 位）：
     *
     * 計算手續費
     * BigDecimal takerFee = new BigDecimal(data.getCommissionRates().getTaker());
     */
    private CommissionRates commissionRates;

    @Data
    public static class CommissionRates {

        /**
         * Maker 手續費率（例如 "0.00000000" 表示 0%）。
         */
        private String maker;

        /**
         * Taker 手續費率。
         */
        private String taker;

        /**
         * 買方手續費率。
         */
        private String buyer;

        /**
         * 賣方手續費率。
         */
        private String seller;
    }

    /**
     * 是否允許交易。
     *
     * if (!data.canTrade) {
     *     throw new Error("帳戶禁止交易！");
     * }
     */
    private Boolean canTrade;

    /**
     * 是否允許提幣。
     */
    private Boolean canWithdraw;

    /**
     * 是否允許充值。
     */
    private Boolean canDeposit;

    /**
     * 是否為經紀商帳戶（Broker Account）。
     */
    private Boolean brokered;

    /**
     * 是否啟用自成交保護（Self-Trade Prevention, STP）。
     */
    private Boolean requireSelfTradePrevention;

    /**
     * 是否禁止使用 Smart Order Router（SOR）。
     */
    private Boolean preventSor;

    /**
     * 帳戶資訊最後更新時間（Unix 時間戳，單位：毫秒）。
     */
    private Long updateTime;

    /**
     * 帳戶類型（例如 "SPOT" 現貨，其他可能值："MARGIN"、"FUTURES"）。
     */
    private String accountType;

    /**
     * 資產餘額（Balances）
     *
     * for balance in response['balances']:
     *     if float(balance['free']) > 0:
     *         print(f"可用 {balance['asset']}: {balance['free']}")
     */
    private List<Balance> balances;

    @Data
    public static class Balance {

        /**
         * 資產代碼（例如 "BTC"、"ETH"）。
         */
        private String asset;

        /**
         * 可用餘額（可交易或提現的金額）。
         */
        private String free;

        /**
         * 凍結餘額（當前掛單中或不可用的金額）。
         */
        private String locked;
    }

    /**
     * 帳戶權限列表（例如 ["SPOT"] 表示僅有現貨權限）。
     */
    private List<String> permissions;

    /**
     * 用戶唯一 ID。
     */
    private Long uid;
}
