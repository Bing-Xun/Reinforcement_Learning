package ddpg.action.voulme;

import java.util.List;

public class VolumeSideDiff {

    public static double calculate(List<Double> list) {
        int size = list.size();

        // 計算前半部分的總和
        int mid = size / 2;
        double firstHalfTotal = 0;
        for (int i = 0; i <= mid; i++) {
            firstHalfTotal += list.get(i);
        }

        // 計算後半部分的總和
        double secondHalfTotal = 0;
        for (int i = mid + 1; i < size; i++) {
            secondHalfTotal += list.get(i);
        }

        // 計算總成交量
        double totalVolume = 0;
        for (double value : list) {
            totalVolume += value;
        }

        // 計算指標
        return (secondHalfTotal - firstHalfTotal) / totalVolume;
    }

    public static void main(String[] args) {
        // 測試例子
        List<Double> volumeList = List.of(1.2, 2.5, 3.8, 4.1, 5.3);
        double indicator = calculate(volumeList);

        System.out.println("指標值: " + indicator);
    }
}
