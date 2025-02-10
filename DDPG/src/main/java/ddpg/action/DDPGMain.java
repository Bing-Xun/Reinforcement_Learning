package ddpg.action;

import binace.vo.QuoteVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ddpg.action.actor.Actor;
import ddpg.action.actor.ActorReward;
import ddpg.action.actor.ActorRewardVO;
import ddpg.action.critic.Critic;
import ddpg.action.price.PriceAvgDiff;
import ddpg.action.price.VWAP;
import ddpg.action.voulme.VolumeAvgDiff;
import ddpg.action.voulme.VolumeSideDiff;
import ddpg.v3.position.Position;
import lombok.Builder;
import lombok.Data;
import org.apache.ibatis.javassist.bytecode.analysis.Util;
import util.Utils;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DDPGMain {

    private static Actor volumnActor;
    private static Critic volumnCritic;
    private static int n = 3;
    private static int trainTick = 5;
    private static ActorReward actorReward = new ActorReward();

    public static void main(String[] args) throws Exception {
        List<QuoteVO> quoteVOList = getDataList();
        init();
        train(quoteVOList);

        Position position = new Position();
        position.setAmount(BigDecimal.valueOf(1000.0)); // 初始化資金
        List<DDPGVolStateVO> ddpgVolumeVOList = getQuoteVOList(quoteVOList);
        Double totalReward = 0.0;
        for(int i=0; i<ddpgVolumeVOList.size(); i++) {
            DDPGVolStateVO vo = ddpgVolumeVOList.get(i);
            Double[] state = new Double[]{vo.getVolumeAvgDiff(), vo.getVolumeSideDiff(), vo.getVwapDiff()};
            Double d = volumnActor.predict(state);
            int action = actorReward.getAction(d);

            System.out.println("###");
            System.out.println(vo.getPrice());
            System.out.println(position);
            double profit = position.modifyPosition(BigDecimal.valueOf(vo.getPrice()), 0.001, action);
            System.out.println(action);
            System.out.println(position);
//            totalReward += profit;

//            if(action == 0) {
//                System.out.println("###");
//                System.out.println(action);
//                System.out.println(position);
//                System.out.println(position.getAmount().doubleValue() + position.getPositionCnt() * vo.getPrice());
//            }
        }

        System.out.println(position);
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

        int doCnt = 0;
        List<TrainHisVO> trainHisVOList = new ArrayList<>();
        List<VWAP.VWAPDiffVO> vwapVOList = new ArrayList<>();
        List<Double> actionPerList = new ArrayList<>();
        for(int i=0; i<ddpgVolumeVOList.size()-1; i++) {
            DDPGVolStateVO vo = ddpgVolumeVOList.get(i);
            DDPGVolStateVO nextVo = ddpgVolumeVOList.get(i+1);
            Double[] state = new Double[]{
                vo.getVolumeAvgDiff()
                , vo.getVolumeSideDiff()
                , vo.getPriceAvgDiff()
                , vo.getVwapDiff()
            };
            Double[] nextState = new Double[]{
                nextVo.getVolumeAvgDiff()
                , nextVo.getVolumeSideDiff()
                , nextVo.getPriceAvgDiff()
                , nextVo.getVwapDiff()
            };

            Double action = volumnActor.train(state);
            actionPerList.add(action);
            trainHisVOList.add(TrainHisVO.builder()
                .state(state)
                .nextState(nextState)
                .action(action)
                .price(vo.getPrice())
                .build());

            vwapVOList.add(VWAP.VWAPDiffVO.builder()
                    .price(vo.getPrice())
                    .volume(vo.getVolume())
                .build());

            if(trainHisVOList.size() % trainTick == 0) {
                Double vwap = VWAP.calculate(vwapVOList);
                Double actionPre = Utils.calculatePercentileAverage(actionPerList, 30, false);
                Double actionSuf = Utils.calculatePercentileAverage(actionPerList, 20, true);
                actorReward.updateActionWeight(actionPre, actionSuf);

                for(TrainHisVO trainHisVO : trainHisVOList) {
                    ActorRewardVO reward = actorReward.getRewards(
                        trainHisVO.getState()
                        , trainHisVO.getNextState()
                        , actorReward.getAction(trainHisVO.getAction())
                        , vwap
                        , trainHisVO.getPrice()
                    );

//                    DecimalFormat df = new DecimalFormat("0.################");
//                    System.out.println("action:"+df.format(actorReward.getAction(trainHisVO.getAction())));
//                    System.out.println("reward:"+reward);
                    // Critic
                    {
                        Double[] tdError = volumnCritic.calculateTDError(reward.getReward(), trainHisVO.getState(), trainHisVO.getNextState());
                        volumnCritic.updateQValues(trainHisVO.getState(), tdError);
                    }

                    // actor
                    {
                        Double[] tdError = volumnCritic.calculateTDError(reward.getReward(), trainHisVO.getState(), trainHisVO.getNextState());
                        volumnActor.updateWeights(reward.getState(), tdError);
                    }
                }

                trainHisVOList = new ArrayList<>();
                vwapVOList = new ArrayList<>();
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

            BigDecimal priceAvgDiff = PriceAvgDiff.calculateAverageDifference(subList.stream()
                .map(QuoteVO::getClose).toList());

            Double vwapDiff = VWAP.calculateDiffPer(subList.stream()
                .map(quote -> new VWAP.VWAPDiffVO(
                    quote.getClose().doubleValue(),
                    quote.getVolume().doubleValue()
                ))
                .collect(Collectors.toList()));

            // 假設 ddpgVolumeVOList 是一個 List，用於儲存計算結果
            ddpgVolumeVOList.add(DDPGVolStateVO.builder()
                .volumeAvgDiff(volumeAvgDiff)
                .volumeSideDiff(volumeSideDiff)
                .priceAvgDiff(priceAvgDiff.doubleValue())
                .vwapDiff(vwapDiff)
                .price(quoteVOList.get(i).getClose().doubleValue())
                .volume(quoteVOList.get(i).getClose().doubleValue())
                .build());
        }

        return ddpgVolumeVOList;
    }

    @Data
    @Builder
    public static class DDPGVolStateVO {
        private Double volumeAvgDiff;
        private Double volumeSideDiff;
        private Double priceAvgDiff;
        private Double vwapDiff;
        private Double price;
        private Double volume;
    }

    @Data
    @Builder
    public static class TrainHisVO {
        private Double[] state;
        private Double[] nextState;
        private Double action;
        private Double price;
    }
}
