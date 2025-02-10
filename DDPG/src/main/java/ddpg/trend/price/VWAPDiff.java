package ddpg.trend.price;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Volume-Weighted Average Price Diff
 */
public class VWAPDiff {

    @Data
    public static class VWAPDiffVO {
        private final Double price;
        private final Double volume;
    }

    public static Double calculate(List<VWAPDiffVO> voList) {
        // 校验输入
        if (voList == null || voList.isEmpty()) {
            throw new IllegalArgumentException("输入列表不能为空");
        }

        int mid = voList.size() / 2; // 分割点

        // 计算前部分的加权和及成交量和
        Double[] frontSums = voList.stream()
            .limit(mid)
            .reduce(new Double[]{0.0, 0.0},
                (sums, vo) -> new Double[]{sums[0] + vo.getPrice() * vo.getVolume(), sums[1] + vo.getVolume()},
                (sums1, sums2) -> new Double[]{sums1[0] + sums2[0], sums1[1] + sums2[1]});

        // 计算后部分的加权和及成交量和
        Double[] backSums = voList.stream()
            .skip(mid)
            .reduce(new Double[]{0.0, 0.0},
                (sums, vo) -> new Double[]{sums[0] + vo.getPrice() * vo.getVolume(), sums[1] + vo.getVolume()},
                (sums1, sums2) -> new Double[]{sums1[0] + sums2[0], sums1[1] + sums2[1]});

        // 校验成交量是否有效
        if (frontSums[1] == 0 || backSums[1] == 0) {
//            throw new IllegalArgumentException("成交量不能为零");
            System.err.println("成交量不能为零");
            if(frontSums[1] == 0) frontSums[1] = Double.MIN_VALUE;
            if(backSums[1] == 0) backSums[1] = Double.MIN_VALUE;
        }

        // 计算加权平均
        Double frontVWAP = frontSums[0] / frontSums[1];
        Double backVWAP = backSums[0] / backSums[1];

        // 返回结果
        return (backVWAP - frontVWAP) / (frontSums[1] + backSums[1]);
    }

    public static Double calculatePer(List<VWAPDiffVO> voList) {
        // 校验输入
        if (voList == null || voList.isEmpty()) {
            throw new IllegalArgumentException("输入列表不能为空");
        }

        int n = voList.size() / 2; // n 为列表的一半

        // 分子：后半部分的加权平均减去前半部分的加权平均
        double frontWeightedSum = 0.0;
        double frontVolumeSum = 0.0;
        double backWeightedSum = 0.0;
        double backVolumeSum = 0.0;

        int mid = voList.size() / 2; // 列表分割点
        for (int i = 0; i < mid; i++) {
            VWAPDiffVO vo = voList.get(i);
            frontWeightedSum += vo.getPrice() * vo.getVolume();
            frontVolumeSum += vo.getVolume();
        }
        for (int i = mid; i < voList.size(); i++) {
            VWAPDiffVO vo = voList.get(i);
            backWeightedSum += vo.getPrice() * vo.getVolume();
            backVolumeSum += vo.getVolume();
        }

        double frontWeightedAvg = frontWeightedSum / frontVolumeSum;
        double backWeightedAvg = backWeightedSum / backVolumeSum;

        double numerator = backWeightedAvg - frontWeightedAvg; // 分子

        // 分母：价格最高的 n 个和价格最低的 n 个的加权平均差
        List<VWAPDiffVO> sortedByPrice = voList.stream()
            .sorted((vo1, vo2) -> Double.compare(vo2.getPrice(), vo1.getPrice())) // 按价格降序排序
            .collect(Collectors.toList());

        // 计算价格最高 n 个的加权平均
        double maxWeightedSum = 0.0;
        double maxVolumeSum = 0.0;
        for (int i = 0; i < n; i++) {
            VWAPDiffVO vo = sortedByPrice.get(i);
            maxWeightedSum += vo.getPrice() * vo.getVolume();
            maxVolumeSum += vo.getVolume();
        }
        double maxWeightedAvg = maxWeightedSum / maxVolumeSum;

        // 计算价格最低 n 个的加权平均
        double minWeightedSum = 0.0;
        double minVolumeSum = 0.0;
        for (int i = sortedByPrice.size() - n; i < sortedByPrice.size(); i++) {
            VWAPDiffVO vo = sortedByPrice.get(i);
            minWeightedSum += vo.getPrice() * vo.getVolume();
            minVolumeSum += vo.getVolume();
        }
        double minWeightedAvg = minWeightedSum / minVolumeSum;

        double denominator = maxWeightedAvg - minWeightedAvg; // 分母

        // 防止分母为零
        if (denominator == 0) {
            return 0.0;
//            throw new IllegalArgumentException("分母不能为零");
        }

        Double d = numerator / denominator;
//        System.out.println("###");
//        System.out.println("numerator:"+numerator);
//        System.out.println("denominator:"+denominator);
//        System.out.println("numerator / denominator:"+numerator / denominator);
//        System.out.println("d:"+d);

        // 返回结果
        return d;
    }
}
