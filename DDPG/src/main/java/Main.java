import binace.vo.QuoteVO;
import ddpg.v3.position.Position;
import ddpg.trend.DDPGMain;
import ddpg.trend.price.VWAPDiff;
import ddpg.trend.voulme.VolumeAvgDiff;
import ddpg.trend.voulme.VolumeSideDiff;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static Position position;
    private static List<double[]> pList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        List<QuoteVO> quoteVOList = DDPGMain.getDataList();
        DDPGMain.init();
        DDPGMain.train(quoteVOList);

        position = new Position();
        position.setAmount(BigDecimal.valueOf(1000.0)); // 初始化資金

        for(int i=2; i<quoteVOList.size(); i++) {
            List<QuoteVO> subList = quoteVOList.subList(i-2, i+1);
            List<DDPGMain.DDPGVolStateVO> ddpgVolStateVOList = getDDPGVolStateVOList(subList);
            BigDecimal price = quoteVOList.get(i).getClose();

            for(DDPGMain.DDPGVolStateVO vo : ddpgVolStateVOList) {
                Double[] state = new Double[]{vo.getVolumeAvgDiff(), vo.getVolumeSideDiff(), vo.getVwapDiff()};
                Double d = DDPGMain.predict(state);
                int action = getAction(vo.getVwapDiff(), d);
                double volume = 0.0001;

                if(action == 0 || action == 1) {
                    double pre_v = position.getPositionCnt();
                    position.modifyPosition(price, volume, action);
                    double suf_v = position.getPositionCnt();

                    pList.add(new double[]{action, price.doubleValue()});

                    if(pre_v != suf_v) {
                        System.out.println("==========");
                        System.out.println("vo.getVwapDiff():"+vo.getVwapDiff());
                        System.out.println("d:"+d);
                        System.out.println("action:"+action);
                        System.out.println("volume:"+volume);
                        System.out.println("getAmount:"+position.getAmount());
                        System.out.println("getPositionCnt:"+position.getPositionCnt());
                        System.out.println("getPrice:"+position.getPrice());
                        System.out.println("getTotal:"+ ((position.getPositionCnt() * position.getPrice().doubleValue()) + position.getAmount().doubleValue()));
                    }
                }
            }
        }

        for(double[] d : pList) {
            System.out.println(Arrays.toString(d));
        }
    }

    public static List<DDPGMain.DDPGVolStateVO> getDDPGVolStateVOList(List<QuoteVO> quoteVOList) {
        int n = 3;
        List<DDPGMain.DDPGVolStateVO> ddpgVolumeVOList = new ArrayList<>();
        for(int i=(n-1); i<quoteVOList.size(); i++) {
            // 提取從 i-n+1 到 i 的子列表
            List<QuoteVO> subList = quoteVOList.subList(i - n + 1, i + 1);
            subList = subList.stream().filter(o -> o.getVolume().compareTo(BigDecimal.ZERO) == 1).toList();

            // 反轉列表
            List<QuoteVO> reversedSubList = new ArrayList<>(subList);
            Collections.reverse(reversedSubList);

            // 使用 Stream 提取數據，計算 volumeAvgDiff
            Double volumeAvgDiff = VolumeAvgDiff.calculate(
                reversedSubList.stream()
                    .map(vo -> vo.getTakerBuyBaseAssetVolume().doubleValue())
                    .collect(Collectors.toList()),  // 提取 buy base volume
                reversedSubList.stream()
                    .map(vo -> vo.getVolume().subtract(vo.getTakerBuyBaseAssetVolume()).doubleValue())
                    .collect(Collectors.toList())   // 計算 volume - buy base volume
            );

            Double volumeSideDiff = VolumeSideDiff.calculate(
                reversedSubList.stream()
                    .map(vo -> vo.getVolume().doubleValue())  // 轉換為 double
                    .collect(Collectors.toList())  // 收集為 List<Double>
            );

            Double vwap = VWAPDiff.calculatePer(subList.stream()
                .map(quote -> new VWAPDiff.VWAPDiffVO(
                    quote.getClose().doubleValue(),
                    quote.getVolume().doubleValue()
                ))
                .collect(Collectors.toList()));

            // 假設 ddpgVolumeVOList 是一個 List，用於儲存計算結果
            ddpgVolumeVOList.add(DDPGMain.DDPGVolStateVO.builder()
                .volumeAvgDiff(volumeAvgDiff)
                .volumeSideDiff(volumeSideDiff)
                .vwapDiff(vwap)
                .build());
        }

        return ddpgVolumeVOList;
    }

    /**
     * 0買, 1賣
     */
    public static int getAction(Double pre_d, Double suf_d) {
        if(pre_d < -0 && suf_d > 0.1) {
            return 0;
        }
        if(pre_d > 0.1 && suf_d < 0) {
            return 1;
        }

        return 2;
    }
}
