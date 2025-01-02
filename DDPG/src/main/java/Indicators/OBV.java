package Indicators;

public class OBV {
    public static int calculateOBV(double[] prices, double[] volumes) {
        int obv = 0;
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] > prices[i - 1]) {
                obv += volumes[i];
            } else if (prices[i] < prices[i - 1]) {
                obv -= volumes[i];
            }
        }
        return obv;
    }

    /**
     * prices 是價格數據，volumes 是對應的成交量數據。
     * calculateOBV 方法會計算每一天的 OBV，並累積成交量。
     * OBV 從第二天開始計算，根據價格的變化來增減成交量。
     * @param args
     */
    public static void main(String[] args) {
        double[] prices = {10, 12, 11, 14, 13};
        double[] volumes = {1000, 1500, 1200, 2000, 1800};
        System.out.println("OBV: " + calculateOBV(prices, volumes));
    }
}
