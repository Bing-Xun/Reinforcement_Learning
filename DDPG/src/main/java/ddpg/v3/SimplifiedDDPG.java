package ddpg.v3;

import Indicators.MovingAverage;
import Indicators.OBV;
import Indicators.RSI;
import com.fasterxml.jackson.databind.ObjectMapper;
import db.config.MyBatisConfig;
import db.entity.QuoteEntity;
import db.mapper.QuoteMapper;
import ddpg.v3.action.actor.Actor;
import ddpg.v3.action.actor.ActorReward;
import ddpg.v3.action.critic.Critic;
import ddpg.v3.action.enums.ActionEnum;
import ddpg.v3.action.history.ActionHistory;
import ddpg.v3.position.Position;
import ddpg.v3.util.Utils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

import static Indicators.MACD.calculateEMA;

public class SimplifiedDDPG {

    private static final int STATE_SIZE = 9; // 假設狀態包含3個特徵
    private static final int ACTION_SIZE = 3; // 行動：買入、賣出、持倉, 成交量

    private static double gamma = 0.8; // 折扣因子
    private static double learningRate = 0.01;  // actor 的學習率
    private static double criticLearningRate = 0.01;  // Critic 的學習率

    private static Actor actionActor;
    private static Critic actionCritic;
    private static Actor volumeActor;
    private static Critic volumeCritic;

    public static void main(String[] args) throws Exception {
//        train();
        String filePath = "/Users/wubingxun/Desktop/Reinforcement_Learning/DDPG/data.json";

        ObjectMapper objectMapper = new ObjectMapper();
        double[][] states = getIndicatorsList();
        objectMapper.writeValue(new File(filePath), states);

        double[][] _states = objectMapper.readValue(new File(filePath), double[][].class);

        // 打印读取的数据
        System.out.println("从 JSON 文件读取的二维数组:");
        for (double[] row : _states) {
            for (double value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }

    public static Object[] predict(double[] state) {
        double[] actionProbs = actor.predict(state); // 預測行動方向的概率
        int action = Utils.getMaxIndex(Arrays.copyOf(actionProbs, actionProbs.length - 1));
        double volume = actionProbs[ACTION_SIZE];

        return new Object[]{action, volume};
    }

    public static void train() {
        Position position = new Position();
        position.setAmount(BigDecimal.valueOf(1000.0)); // 初始化資金

        // 倉位跟展示相關參數
        double totalReward = 0.0;
        double maxProfit = 0.0;
        double minProfit = 0.0;

        // 初始化 行情模型
        actionActor = new Actor(STATE_SIZE, ACTION_SIZE);
        actionActor.setEpsilon(0.9);
        actionCritic = new Critic(STATE_SIZE);

        volumeActor = new Actor(STATE_SIZE, ACTION_SIZE);;
        volumeCritic = new Critic(STATE_SIZE);;

        // 模擬行
//        double[][] states = SimpleActorCritic.getStates(600);
//        double[][] states = getQuoteList();
        double[][] states = getIndicatorsList();
        int changeCpsilonCal1 = (int) (states.length * 0.1);
        int changeCpsilonCal2 = (int) (states.length * 0.3);


//        plotKdGraph(states); // 畫kd圖
//        ActionHistory actionHistory = new ActionHistory();

        double holdDiffPrice = 100;
        double maxVolumeRewardSource = (holdDiffPrice * 3) * position.getAmount().doubleValue();
        for(int i=0; i<states.length-2; i++) {
            if(i > changeCpsilonCal1 && i < changeCpsilonCal2) {
                actionActor.setEpsilon(0.3);
            }
            if( i > changeCpsilonCal2) {
                actionActor.setEpsilon(0.1);
            }

            List<Double> list = new ArrayList<>();
            list.addAll(Utils.toDoubleList(states[i]));
            list.addAll(List.of(position.getAmount().doubleValue(), position.getPositionCnt(), position.getPrice().doubleValue()));
            double[] state = list.stream().mapToDouble(Double::doubleValue).toArray();
            double price = state[0];

            // action
            double[] actionProbs = actionActor.predict(state); // 預測行動方向的概率
            ActionHistory.History history = getHistory(state, position, actionProbs, price);

            Position nextPosition = new Position(position.getAmount(), position.getPositionCnt(), position.getPrice());
            List<Double> nextList = new ArrayList<>();
            nextList.addAll(Utils.toDoubleList(states[i+1]));
            nextList.addAll(List.of(nextPosition.getAmount().doubleValue(), nextPosition.getPositionCnt(), nextPosition.getPrice().doubleValue()));
            double[] nextState = nextList.stream().mapToDouble(Double::doubleValue).toArray();


            // action
            {
                // 1. 更新 Critic
                ActorReward actionActorReward = ActorReward.getRewards(history, nextState, holdDiffPrice);
                double actionTdError = actionCritic.getTdError(actionActorReward.getReward(), gamma, actionActorReward.getState(), actionActorReward.getNextState());
                actionCritic.updateWeights(state, actionTdError, criticLearningRate);

                // 2. 用更新後的 Critic 計算新的 TD 誤差
                actionTdError = actionCritic.getTdError(actionActorReward.getReward(), gamma, actionActorReward.getState(), actionActorReward.getNextState());

                // 3. 更新 Actor，使用更新後的 TD 誤差
                actionActor.updateWeights(state, actionProbs, actionTdError, learningRate);
            }

            // volume
            {
                maxVolumeRewardSource;
            }



            System.out.printf("\n");
            System.out.printf("##########\n");
            System.out.printf("交易前現金餘額: %.2f\n", position.getAmount());
            System.out.printf("交易前持倉量: %.2f\n", position.getPositionCnt());
            System.out.printf("交易前持倉均價: %.2f\n", position.getPrice());

            int action = Utils.getMaxIndex(Arrays.copyOf(actionProbs, actionProbs.length - 1));
            double volume = actionProbs[ACTION_SIZE];
            double profit = position.modifyPosition(BigDecimal.valueOf(price), volume, action);
            if (profit > maxProfit) maxProfit = profit;
            if (profit < minProfit) minProfit = profit;
            totalReward += profit;

            System.out.println("狀態: " + java.util.Arrays.toString(state));
            System.out.println("方向概率 (買, 賣, 持倉):" + java.util.Arrays.toString(actionProbs));
            System.out.println("方向(買, 賣, 持倉): " + action);
//            System.out.println("獎勵: " + reward);
            if(profit > 1) System.out.println("獎勵aa: " + profit);
            System.out.printf("交易量: %.2f\n", volume);
            System.out.printf("現金餘額: %.2f\n", position.getAmount());
            System.out.printf("持倉量: %.2f\n", position.getPositionCnt());
            System.out.printf("持倉均價: %.2f\n", position.getPrice());
            System.out.printf("單次收益: %.2f\n", profit);
            System.out.printf("最高收益: %.2f\n", maxProfit);
            System.out.printf("最低收益: %.2f\n", minProfit);
            System.out.printf("總收益: %.2f\n", totalReward);
        }

        actor.setEpsilon(0.1);
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
        int quoteCnt = 19000;
        List<QuoteEntity> quote1mList = getQuoteList("quote_btc_1m", quoteCnt);
        List<Double> priceList = quote1mList.stream().map(o -> {
            return Double.valueOf(o.getOpen().doubleValue());
        }).toList();
        List<Double> volList = quote1mList.stream().map(o -> {
            return Double.valueOf(o.getVolume().doubleValue());
        }).toList();

        return getIndicatorsList(priceList, volList, quoteCnt);
    }

    public static double[][] getIndicatorsList(List<Double> priceList, List<Double> volList, int quoteCnt) {
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

    private static ActionHistory.History getHistory(double[] state, Position position, double[] actionProbs, double price) {
        ActionHistory.History history = new ActionHistory.History();
        ActionHistory.Action hAction = new ActionHistory.Action();
        ActionHistory.Position hPosition = new ActionHistory.Position();
        history.setState(state);
        history.setAmount(position.getAmount());

        hAction.setAction(actionProbs);
        hAction.setActionEnum(ActionEnum.values()[Utils.getMaxIndex(actionProbs)]);
        hAction.setPrice(BigDecimal.valueOf(price));
        hAction.setVolume(actionProbs[actionProbs.length-1]);
        history.setAction(hAction);

        hPosition.setPrice(position.getPrice());
        hPosition.setCnt(position.getPositionCnt());
        history.setPosition(hPosition);

        return history;
    }
}
