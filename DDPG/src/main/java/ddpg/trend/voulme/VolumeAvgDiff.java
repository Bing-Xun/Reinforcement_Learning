package ddpg.trend.voulme;

import java.util.List;

public class VolumeAvgDiff {

    public static double calculate(List<Double> buyVolumes, List<Double> sellVolumes) {
        // 確保買入和賣出成交量列表大小相同
        if (buyVolumes.size() != sellVolumes.size()) {
            throw new IllegalArgumentException("買入和賣出成交量列表大小必須相同");
        }

        double totalBuyVolume = 0;
        double totalSellVolume = 0;

        // 計算買入和賣出成交量總和
        for (int i = 0; i < buyVolumes.size(); i++) {
            totalBuyVolume += buyVolumes.get(i);
            totalSellVolume += sellVolumes.get(i);
        }

        // 計算總成交量
        double totalVolume = totalBuyVolume + totalSellVolume;

        // 避免除以零
        if (totalVolume == 0) {
            return 0;  // 如果總成交量為零，則返回 0
        }

        // 計算成交量不對稱性
        return (totalBuyVolume - totalSellVolume) / totalVolume;
    }

    public static void main(String[] args) {
        // 測試例子
        List<Double> buyVolumes = List.of(120.5, 150.3, 130.2, 140.0, 110.1);
        List<Double> sellVolumes = List.of(100.0, 90.2, 95.5, 80.0, 85.0);

        double imbalance = calculate(buyVolumes, sellVolumes);

        System.out.println("成交量不對稱性: " + imbalance);
    }
}
