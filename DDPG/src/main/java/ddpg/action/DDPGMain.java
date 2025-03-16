package ddpg.action;

import binace.vo.QuoteVO;
import ddpg.action.actor.Actor;
import ddpg.action.actor.ActorReward;
import ddpg.action.actor.ActorRewardVO;
import ddpg.action.critic.Critic;
import ddpg.action.position.Position;
import ddpg.v3.util.Utils;
import lombok.Builder;
import lombok.Data;
import transformer.TransformerEncoder;
import transformer.TransformerTrainer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DDPGMain {

    private static Actor actor;
    private static Critic critic;
    private static TransformerEncoder encoder;
    public static double trainPer = 0.003;
    public static int doListCnt = 6; // 幾筆判定一個趨勢

    public static void main(String[] args) throws Exception {
        DDPGMain ddpgMain = new DDPGMain();
        ddpgMain.init();

        Position position = new Position();
        position.setAmount(BigDecimal.valueOf(1000.0)); // 初始化資金
        encoder = new TransformerEncoder(14, 14*3, 3, 3, 1); // 原始11向量 + 資金 + 持倉價 + 持倉量 = 14, 輸出買or賣or持倉, 成交量

        List<QuoteVO> quoteVOList = ddpg.trend.DDPGMain.getDataList();
        List<QuoteVO> trainVOList = new ArrayList<>();
        List<QuoteVO> rewardVOList = new ArrayList<>();
        for(int i=0; i<quoteVOList.size()-doListCnt; i++) {
            trainVOList.add(quoteVOList.get(i));
            rewardVOList.add(quoteVOList.get(i+doListCnt));

            if(trainVOList.size() == doListCnt && rewardVOList.size() == doListCnt) {
                TrainVO trainVO = ddpgMain.train(trainVOList, rewardVOList, position);
                position.modifyPosition(trainVO.getPrice(), trainVO.getVolume(), trainVO.getAction());

                trainVOList = new ArrayList<>();
                rewardVOList = new ArrayList<>();
            }
        }

//        for(int i=0; i<ddpgVolumeVOList.size(); i++) {
//            DDPGVolStateVO vo = ddpgVolumeVOList.get(i);
//            Double[] state = new Double[]{vo.getVolumeAvgDiff(), vo.getVolumeSideDiff(), vo.getVwapDiff()};
//            Double d = volumnActor.predict(state);
//            int action = actorReward.getAction(d);
//
//            System.out.println("###");
//            System.out.println(vo.getPrice());
//            System.out.println(position);
//            double profit = position.modifyPosition(BigDecimal.valueOf(vo.getPrice()), 0.001, action);
//            System.out.println(action);
//            System.out.println(position);
////            totalReward += profit;
//
////            if(action == 0) {
////                System.out.println("###");
////                System.out.println(action);
////                System.out.println(position);
////                System.out.println(position.getAmount().doubleValue() + position.getPositionCnt() * vo.getPrice());
////            }
//        }

        System.out.println(position);
    }

    public void init() {
        encoder = new TransformerEncoder(14, 14*3, 3, 7, 1); // 原始11向量 + 資金 + 持倉價 + 持倉量 = 14, 輸出買or賣, 成交量
        TransformerTrainer trainer = new TransformerTrainer(encoder, 0.01);

        actor = new Actor(encoder);
//        critic = new Critic();
    }

    public double[][] predict(double[][] state) {
        return actor.predict(state);
    }

    public TrainVO train(List<QuoteVO> trainVOList, List<QuoteVO> rewardVOList, Position position) throws Exception {
//        List<TrainData> trainDataList = getTrainData(trainVOList, position);
//
//        double[][] state = getState(trainDataList);
//        double[][] outputPool = encoder.forward(state).outputPool;
//        int action = Utils.getMaxIndex(outputPool[0]);
//        BigDecimal price = getPrice(state[state.length-1]);
//        double volume = sumDoubleArr(outputPool[1]);
//        double[][] nextState = getNextState(state, position, action, price, volume);
//        ActorRewardVO reward = ActorReward.getRewards(state, action, volume, rewardVOList);
//        ActorRewardVO nextReward = ActorReward.getRewards(nextState, action, volume, rewardVOList);
//
//        // Critic
//        double cTdError = critic.calculateTDError(reward.getReward(), nextReward.getReward());
//        critic.updateQValues(reward.getState(), cTdError);
//
//        // actor
//        double[][] aTdError = critic.calculateTDError(reward.getReward(), reward.getState(), reward.getNextState());
//        actor.train(reward.getState(), aTdError);
//
//        return TrainVO.builder()
//            .price(price)
//            .action(action)
//            .volume(volume)
//            .build();

        return null;
    }

    private double sumDoubleArr(double[] d) {
        double sum = 0;
        for (double num : d) {
            sum += num;
        }

        return sum;
    }

    private List<TrainData> getTrainData(List<QuoteVO> quoteVOList, Position position) throws Exception{
        List<TrainData> trainDataList = new ArrayList<>();
        for(QuoteVO vo : quoteVOList) {
            double[] d = vo.toArray();
            trainDataList.add(new TrainData(d));
        }
        trainDataList.add(new TrainData(position.getAmount(), position.getPrice(), new BigDecimal(position.getPositionCnt())));

        return trainDataList;
    }

    private double[][] getState(List<TrainData> trainDataList) {
        double[][] state = new double[trainDataList.size()][trainDataList.get(0).toArray().length];
        for(int i=0; i<trainDataList.size(); i++) {
            state[i] = trainDataList.get(i).toArray();
        }

        return state;
    }

    private double[][] getNextState(double[][] state, Position position, int action, BigDecimal price, double volume) {
        TrainData lastTrainData = new TrainData(state[state.length-1]);
        Position doAfterPosition = position.getDoAfter(price, volume, action);

        lastTrainData.setAmount(doAfterPosition.getAmount());
        lastTrainData.setPositionPrice(doAfterPosition.getPrice());
        lastTrainData.setPositionCnt(new BigDecimal(doAfterPosition.getPositionCnt()));

        double[][] nextState = state;
        nextState[nextState.length-1] = lastTrainData.toArray();

        return nextState;
    }

    private BigDecimal getPrice(double[] state) {
        TrainData lastTrainData = new TrainData(state);
        return lastTrainData.getClose();
    }

    @Data
    public static class TrainData {
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
        private BigDecimal amount = BigDecimal.ZERO;
        private BigDecimal positionPrice = BigDecimal.ZERO;
        private BigDecimal positionCnt = BigDecimal.ZERO;

        public TrainData() {

        }

        public TrainData(double[] d) {
            this.openTime = (long) d[0];
            this.open = BigDecimal.valueOf(d[1]);
            this.high = BigDecimal.valueOf(d[2]);
            this.low = BigDecimal.valueOf(d[3]);
            this.close = BigDecimal.valueOf(d[4]);
            this.volume = BigDecimal.valueOf(d[4]);
            this.closeTime = (long) d[5];
            this.quoteAssetVolume = BigDecimal.valueOf(d[6]);
            this.trades = (long) d[7];
            this.takerBuyBaseAssetVolume = BigDecimal.valueOf(d[8]);
            this.takerBuyQuoteAssetVolume = BigDecimal.valueOf(d[9]);
        }

        public TrainData(BigDecimal amount, BigDecimal positionPrice, BigDecimal positionCnt) {
            this.openTime = 0L;
            this.open = BigDecimal.ZERO;
            this.high = BigDecimal.ZERO;
            this.low = BigDecimal.ZERO;
            this.close = BigDecimal.ZERO;
            this.volume = BigDecimal.ZERO;
            this.closeTime = 0L;
            this.quoteAssetVolume = BigDecimal.ZERO;
            this.trades = 0L;
            this.takerBuyBaseAssetVolume = BigDecimal.ZERO;
            this.takerBuyQuoteAssetVolume = BigDecimal.ZERO;
            this.amount = amount;
            this.positionPrice = positionPrice;
            this.positionCnt = positionCnt;
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
                , this.amount.doubleValue()
                , this.positionPrice.doubleValue()
                , this.positionCnt.doubleValue()
            };
        }
    }

    @Builder
    @Data
    public static class TrainVO {
        private BigDecimal price;
        private int action;
        private double volume;
    }
}
