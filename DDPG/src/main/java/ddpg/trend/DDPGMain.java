package ddpg.trend;

import binace.vo.QuoteVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import ddpg.trend.actor.Actor;
import ddpg.trend.actor.ActorReward;
import ddpg.trend.critic.Critic;
import ddpg.trend.price.VWAPDiff;
import ddpg.trend.voulme.VolumeAvgDiff;
import ddpg.trend.voulme.VolumeSideDiff;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class DDPGMain {

    private static Actor volumnActor;
    private static Critic volumnCritic;
    private static int n = 3;

    public static void main(String[] args) throws Exception {
        List<QuoteVO> quoteVOList = getDataList();
        init();
        train(quoteVOList);

        List<DDPGVolStateVO> ddpgVolumeVOList = getQuoteVOList(quoteVOList);

        for(int i=0; i<ddpgVolumeVOList.size(); i++) {
            DDPGVolStateVO vo = ddpgVolumeVOList.get(i);
            Double[] state = new Double[]{vo.getVolumeAvgDiff(), vo.getVolumeSideDiff(), vo.getVwapDiff()};
            Double d = volumnActor.predict(state);
//            System.out.println("###");
//            System.out.println(vo.getPrice());
//            System.out.println(vo.getNextPrice());
//            System.out.println("#"+d);
        }
    }

    public static void init() {
        volumnActor = new Actor(3, 1);
        volumnCritic = new Critic();
    }

    public static Double predict(Double[] state) {
        return volumnActor.predict(state);
    }

    public static void train(List<QuoteVO> quoteVOList) throws Exception {
        List<DDPGVolStateVO> ddpgVolumeVOList = getQuoteVOList(quoteVOList);

        for(int i=(n-1); i<ddpgVolumeVOList.size()-1-n-1; i++) { // -n-1 state -nextState
            DDPGVolStateVO vo = ddpgVolumeVOList.get(i-n+1);
            DDPGVolStateVO nextVo = ddpgVolumeVOList.get(i-n+2);
            Double[] state = new Double[]{vo.getVolumeAvgDiff(), vo.getVolumeSideDiff(), vo.getVwapDiff()};
            Double[] nextState = new Double[]{nextVo.getVolumeAvgDiff(), nextVo.getVolumeSideDiff(), vo.getVwapDiff()};
            Double predict = volumnActor.train(state);
            Double nextPredict = volumnActor.train(nextState);
            Double nextVwapDiff = vo.getNextVwapDiff();

            ActorReward reward = ActorReward.getRewards(state, nextState, predict, nextVwapDiff);

            // Critic
            {
                Double[] tdError = volumnCritic.calculateTDError(reward.getReward(), new Double[]{predict}, new Double[]{nextPredict});
                volumnCritic.updateQValues(new Double[]{predict}, tdError);
            }

            // actor
            {
                Double tdError = volumnCritic.calculateTDError(reward.getReward(), predict, nextPredict);
                volumnActor.updateWeights(reward.getState(), tdError);
            }
        }
    }

    public static List<QuoteVO> getDataList() throws Exception {
        String filePath = "DDPG/data2.json";
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<>(){});
    }

    private static List<DDPGVolStateVO> getQuoteVOList(List<QuoteVO> quoteVOList) {
        List<DDPGVolStateVO> ddpgVolumeVOList = new ArrayList<>();

        for(int i=(n-1); i<quoteVOList.size()-n+1; i++) {
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

            Double nextVwap = VWAPDiff.calculatePer(quoteVOList.subList(i - n + 2, i + 2).stream()
                .map(quote -> new VWAPDiff.VWAPDiffVO(
                    quote.getClose().doubleValue(),
                    quote.getVolume().doubleValue()
                ))
                .collect(Collectors.toList()));

            // 假設 ddpgVolumeVOList 是一個 List，用於儲存計算結果
            ddpgVolumeVOList.add(DDPGVolStateVO.builder()
                .volumeAvgDiff(volumeAvgDiff)
                .volumeSideDiff(volumeSideDiff)
                .vwapDiff(vwap)
                .nextVwapDiff(nextVwap)
                .price(quoteVOList.get(i+1).getClose().doubleValue())
                .nextPrice(quoteVOList.get(i+2).getClose().doubleValue())
                .build());
        }

        return ddpgVolumeVOList;
    }

    @Data
    @Builder
    public static class DDPGVolStateVO {
        private Double volumeAvgDiff;
        private Double volumeSideDiff;
        private Double vwapDiff; // 成交量加权平均价格
        private Double nextVwapDiff; // 成交量加权平均价格
        private Double price;
        private Double nextPrice;
    }
}
