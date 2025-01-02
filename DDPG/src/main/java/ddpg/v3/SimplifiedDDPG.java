package ddpg.v3;

import Indicators.MACD;
import Indicators.MovingAverage;
import Indicators.OBV;
import Indicators.RSI;
import db.config.MyBatisConfig;
import db.entity.QuoteEntity;
import db.mapper.QuoteMapper;
import ddpg.v1.SimpleActorCritic;
import ddpg.v3.action.actor.ActionActor;
import ddpg.v3.action.critic.ActionCritic;
import ddpg.v3.action.enums.ActionEnum;
import ddpg.v3.action.history.ActionHistory;
import ddpg.v3.reward.Reward;
import ddpg.v3.position.Position;
import ddpg.v3.util.Utils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static Indicators.MACD.calculateEMA;
import static ddpg.v3.graph.ChartDrawer.plotKdGraph;

public class SimplifiedDDPG {

    private static final int STATE_SIZE = 1; // 假設狀態包含3個特徵
    private static final int ACTION_SIZE = 3; // 行動：買入、賣出、持倉, 成交量

    private static double gamma = 0.8; // 折扣因子
    private static double learningRate = 0.01;  // actor 的學習率
    private static double criticLearningRate = 0.01;  // Critic 的學習率

    public static void main(String[] args) {
        Position position = new Position();
        position.setAmount(BigDecimal.valueOf(1000.0)); // 初始化資金

        // 倉位跟展示相關參數
        double totalReward = 0.0;
        double maxProfit = 0.0;
        double minProfit = 0.0;

        // 初始化 行情模型
        ActionActor actionActor = new ActionActor(STATE_SIZE, ACTION_SIZE);
        actionActor.setEpsilon(0.9);
        ActionCritic actionCritic = new ActionCritic(STATE_SIZE);

        // 模擬行情
//        double[][] states = SimpleActorCritic.getStates(600);
//        double[][] states = getQuoteList();
        double[][] states = getIndicatorsList();

//        plotKdGraph(states); // 畫kd圖
        ActionHistory actionHistory = new ActionHistory();

        for(int i=0; i<states.length-1; i++) {
            double[] state = new double[]{states[i][0]};
            double price = state[0];

            // 行情
            double[] actionProbs = actionActor.predict(state); // 預測行動方向的概率

            ActionHistory.History history = new ActionHistory.History();
            history.setState(state);
            history.setAction(actionProbs);
            history.setPrice(BigDecimal.valueOf(price));
            history.setPosition(position.getPositionCnt());
            actionHistory.getHistoryList().add(history);

            if(Utils.getMaxIndex(actionProbs) == ActionEnum.SELL.getValue()) {
                List<Reward> rewardList = Reward.getRewards(actionHistory);

                for(Reward reward : rewardList) {
                    double actionTdError = actionCritic.getTdError(reward.getReward(), gamma, reward.getState(), reward.getNextState()); // TD 誤差計算
                    // 更新 action 權重
                    actionActor.updateWeights(state, actionProbs, actionTdError, learningRate);
                    actionCritic.updateWeights(state, actionTdError, criticLearningRate);
                }
            }

            int action = Utils.getMaxIndex(actionProbs);
            double volume = actionProbs[ACTION_SIZE];
            double profit = position.modifyPosition(BigDecimal.valueOf(price), volume, action);
            if (profit > maxProfit) maxProfit = profit;
            if (profit < minProfit) minProfit = profit;
            totalReward += profit;

            System.out.println("");
            System.out.println("##########");
            System.out.println("狀態: " + java.util.Arrays.toString(state));
            System.out.println("方向概率 (買, 賣, 持倉): " + java.util.Arrays.toString(actionProbs));
//            System.out.println("方向(買, 賣, 持倉): " + action);
//            System.out.println("獎勵: " + reward);
//            if(reward > 1) System.out.println("獎勵aa: " + reward);
            System.out.printf("交易量: %.2f\n", volume);
            System.out.printf("現金餘額: %.2f\n", position.getAmount());
            System.out.printf("持倉量: %.2f\n", position.getPositionCnt());
            System.out.printf("持倉均價: %.2f\n", position.getPrice());
            System.out.printf("單次收益: %.2f\n", profit);
            System.out.printf("最高收益: %.2f\n", maxProfit);
            System.out.printf("最低收益: %.2f\n", minProfit);
            System.out.printf("總收益: %.2f\n", totalReward);
        }
    }

    private static double getVolume(Position position, Integer action, BigDecimal price) {
        if(action == 0) {
            Double maxPosition = position.getAmount().divide(price, 4, RoundingMode.DOWN).doubleValue();
            return Math.random() * ((maxPosition / 10.0) + 1);
        }
        if(action == 1) {
            return Math.random() * (position.getPositionCnt() + 1);
        }

        return 0;
    }

    public static List<QuoteEntity> getQuoteList(String tableName, int limit) {
        List<QuoteEntity> quotes = new ArrayList<>();

        // 使用 Java 配置创建 SqlSessionFactory
        MyBatisConfig myBatisConfig = new MyBatisConfig();
        SqlSessionFactory sqlSessionFactory = myBatisConfig.sqlSessionFactory();

        try (SqlSession session = sqlSessionFactory.openSession()) {
            QuoteMapper quoteMapper = session.getMapper(QuoteMapper.class);

            quotes = quoteMapper.getQuotes(tableName, limit);
//            for (QuoteEntity quote : quotes) {
//                System.out.println(quote);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return quotes;
    }

    public static double[][] getQuoteList() {
        int quoteCnt = 2900;

        List<QuoteEntity> quote1mList = getQuoteList("quote_btc_1m", quoteCnt);
        List<QuoteEntity> quote3mList = getQuoteList("quote_btc_3m", quoteCnt);
        List<QuoteEntity> quote5mList = getQuoteList("quote_btc_5m", quoteCnt);
        double[][] result = new double[quoteCnt][9];

        for(int i=0; i<quoteCnt; i++) {
            double[] arr = DoubleStream.concat(
                    DoubleStream.concat(DoubleStream.of(getDouble(quote1mList.get(i)))
                    , DoubleStream.of(getDouble(quote3mList.get(i))))
                    , DoubleStream.of(getDouble(quote5mList.get(i)))
            ).toArray();

            result[i] = arr;
        }

        return result;
    }

    /**
     * price, MACD, MovingAvg, OBV, RSI, VOl
     */
    public static double[][] getIndicatorsList() {
        int quoteCnt = 12000;
        List<QuoteEntity> quote1mList = getQuoteList("quote_btc_1m", quoteCnt);
        List<Double> priceList = quote1mList.stream().map(o -> {
            return Double.valueOf(o.getOpen().doubleValue());
        }).toList();
        List<Double> volList = quote1mList.stream().map(o -> {
            return Double.valueOf(o.getVolume().doubleValue());
        }).toList();

        List<double[]> result = new ArrayList<>();
        for(int i=30; i<quoteCnt; i++) {
            // macd
            double[] priceArray = priceList.subList(i - 26, i).stream()
                    .mapToDouble(Double::doubleValue)  // 转换为原始类型
                    .toArray();

            double[] obvPriceArray = priceList.subList(i - 5, i).stream()
                    .mapToDouble(Double::doubleValue)  // 转换为原始类型
                    .toArray();

            double[] obvVolArr = volList.subList(i - 5, i).stream()
                    .mapToDouble(Double::doubleValue)  // 转换为原始类型
                    .toArray();

            double fastEMA = calculateEMA(priceArray, 12); // 快線
            double slowEMA = calculateEMA(priceArray, 26); // 慢線
            double macd = fastEMA - slowEMA;

            double ma = MovingAverage.calculateMA(priceArray, 3);

            double obv = OBV.calculateOBV(obvPriceArray, obvVolArr);

            double rsi = RSI.calculateRSIFromPrices(priceArray, 5);
            result.add(new double[]{priceList.get(i), macd, ma, obv, rsi, volList.get(i)});
        }

        return result.toArray(new double[0][]);
    }

    private static double[] getDouble(QuoteEntity entity) {
        return new double[]{
                entity.getOpen().doubleValue(),
                entity.getHigh().doubleValue(),
                entity.getLow().doubleValue(),
                entity.getClose().doubleValue(),
                entity.getVolume().doubleValue(),
                entity.getQuoteAssetVolume().doubleValue(),
                entity.getTrades().doubleValue(),
                entity.getTakerBuyBaseAssetVolume().doubleValue(),
                entity.getTakerBuyQuoteAssetVolume().doubleValue(),
        };
    }
}
