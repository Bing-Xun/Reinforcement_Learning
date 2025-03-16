package policyGradient.reinforce;

import java.util.*;

public class TradingAgent {

    // 行動 (Actions)
    private static final int BUY = 0;
    private static final int SELL = 1;
    private static final int HOLD = 2;
    private static final int[] ACTIONS = {BUY, SELL, HOLD};

    // REINFORCE 參數
    private static final double LEARNING_RATE = 0.001; // 更小的學習率
    private static final double DISCOUNT_FACTOR = 0.95; // 更高的折扣因子
    private static final int EPISODES = 1000;
    private static final int MAX_HOLDING = 1000; // 最大持倉量

    // 策略 (Policy):  Map<String, double[]>
    // Key: 狀態 (state)，例如 "price=100,volume=500,holding=100"
    // Value: 動作機率 (action probabilities), double[3] 對應 BUY, SELL, HOLD
    private final Map<String, double[]> policy = new HashMap<>();
    private final Random random = new Random();

    // 取得策略 (Policy)
    private double[] getPolicy(String state) {
        return policy.computeIfAbsent(state, k -> {
            double[] probabilities = new double[ACTIONS.length];
            Arrays.fill(probabilities, 1.0 / ACTIONS.length); // 初始均勻分佈
            return probabilities;
        });
    }
    // softmax 函數
    private double[] softmax(double[] logits) {
        double maxLogit = Arrays.stream(logits).max().orElse(0.0);
        double[] expLogits = Arrays.stream(logits).map(l -> Math.exp(l - maxLogit)).toArray();
        double sumExpLogits = Arrays.stream(expLogits).sum();
        return Arrays.stream(expLogits).map(e -> e / sumExpLogits).toArray();
    }

    // 選擇動作 (依機率)
    private int chooseAction(String state) {
        double[] probabilities = getPolicy(state);
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < ACTIONS.length; i++) {
            cumulativeProbability += probabilities[i];
            if (randomValue < cumulativeProbability) {
                return i;
            }
        }
        return ACTIONS.length - 1;
    }

    // 計算回報 (Return)
    private List<Double> calculateReturns(List<Double> rewards) {
        List<Double> returns = new ArrayList<>();
        double G = 0;
        for (int i = rewards.size() - 1; i >= 0; i--) {
            G = rewards.get(i) + DISCOUNT_FACTOR * G;
            returns.add(0, G);
        }
        return returns;
    }
    // 狀態表示 (State Representation)
    private String getState(double price, int volume, int holding) {
        // 簡化的狀態表示，可以根據需要擴展
        return String.format("price=%.2f,volume=%d,holding=%d", price, volume, holding);
    }

    // 獎勵函數 (Reward Function) -  *非常* 簡化
    private double getReward(int action, double currentPrice, double previousPrice, int holding, int tradeVolume) {
        double profit = 0.0;
        //基於交易的獎勵
        if(action == BUY){
            profit = (currentPrice - previousPrice) * tradeVolume; //買了後漲了，賺了
        } else if (action == SELL) {
            profit = (previousPrice-currentPrice) * tradeVolume; //賣了後跌了，賺了
        }

        //持倉的獎勵/懲罰
        double holdingReward = holding * (currentPrice - previousPrice);

        // 綜合獎勵 (可以調整權重)
        return profit + holdingReward;
    }


    // 訓練 (Training)
    public void train(List<MarketData> marketData) {
        for (int episode = 0; episode < EPISODES; episode++) {
            // 模擬一個交易日/交易時段
            List<String> states = new ArrayList<>();
            List<Integer> actions = new ArrayList<>();
            List<Double> rewards = new ArrayList<>();
            List<Integer> tradeVolumes = new ArrayList<>(); // 記錄每次交易量

            int holding = 0; // 初始持倉
            double previousPrice = marketData.get(0).price; // 初始價格


            for (int t = 0; t < marketData.size(); t++) {
                MarketData currentData = marketData.get(t);
                String state = getState(currentData.price, currentData.volume, holding);
                states.add(state);

                int action = chooseAction(state);
                actions.add(action);

                // 根據動作決定交易量 (簡化)
                int tradeVolume = 0;
                if (action == BUY) {
                    tradeVolume = Math.min(currentData.volume, MAX_HOLDING - holding); // 考慮最大持倉
                    holding += tradeVolume;
                } else if (action == SELL) {
                    tradeVolume = Math.min(holding, currentData.volume); // 賣出不能超過持倉
                    holding -= tradeVolume;
                }
                tradeVolumes.add(tradeVolume);

                // 計算獎勵
                double reward = getReward(action, currentData.price, previousPrice, holding, tradeVolume);
                rewards.add(reward);

                previousPrice = currentData.price; //更新先前價格
            }

            // 計算回報
            List<Double> returns = calculateReturns(rewards);

            // 更新策略
            for (int t = 0; t < states.size(); t++) {
                String state = states.get(t);
                int action = actions.get(t);
                double G = returns.get(t);

                double[] probabilities = getPolicy(state);

                //策略梯度計算
                double[] gradients = new double[ACTIONS.length];
                for (int a = 0; a < ACTIONS.length; a++) {
                    gradients[a] = (a == action ? 1 : 0) - probabilities[a];
                    gradients[a] *= G;
                }
                //更新策略
                double [] logits = new double[ACTIONS.length];
                for(int a = 0; a<ACTIONS.length; a++){
                    logits[a] = Math.log(probabilities[a]) + LEARNING_RATE * gradients[a];
                }
                double[] updatedProbabilities = softmax(logits);

                policy.put(state, updatedProbabilities);
            }
        }
        System.out.println("訓練完成");
        //訓練後，你可以選擇印出policy，或者進行模擬交易
    }

    // 市場數據 (Market Data) - 簡化
    static class MarketData {
        double price;
        int volume;

        public MarketData(double price, int volume) {
            this.price = price;
            this.volume = volume;
        }
    }

    public static void main(String[] args) {
        // 模擬市場數據 (實際應用中應從交易所或數據提供商獲取)
        List<MarketData> marketData = new ArrayList<>();
        Random priceGen = new Random();
        double currentPrice = 100.0;
        for (int i = 0; i < 100; i++) {
            // 模擬價格波動 (正態分佈)
            double priceChange = priceGen.nextGaussian() * 2.0; // 標準差為 2.0
            currentPrice += priceChange;
            currentPrice = Math.max(90.0, Math.min(110.0, currentPrice)); // 限制價格範圍

            // 模擬成交量 (隨機)
            int volume = priceGen.nextInt(500) + 100; // 成交量在 100 到 600 之間
            marketData.add(new MarketData(currentPrice, volume));
        }

        TradingAgent agent = new TradingAgent();
        agent.train(marketData);

        // 在訓練數據上進行簡單的策略評估 (非回測)
        evaluatePolicy(agent, marketData);
    }

    // 策略評估 (簡化版，非嚴格回測)
    public static void evaluatePolicy(TradingAgent agent, List<MarketData> marketData) {
        int holding = 0;
        double cash = 0.0; // 初始現金
        double initialCash = 0.0; // 初始現金（用於計算總收益）
        double totalAssetValue = 0.0; // 總資產價值

        System.out.println("\n策略評估:");
        for (int i = 0; i < marketData.size(); i++) {
            MarketData data = marketData.get(i);
            String state = agent.getState(data.price, data.volume, holding);
            int action = agent.chooseAction(state);  // 根據訓練好的策略選擇動作

            int tradeVolume = 0;
            if (action == BUY) {
                tradeVolume = Math.min(data.volume, MAX_HOLDING - holding);
                holding += tradeVolume;
                cash -= tradeVolume * data.price;
                System.out.printf("時間 %d: 買入 %d 股，價格 %.2f\n", i, tradeVolume, data.price);
            } else if (action == SELL) {
                tradeVolume = Math.min(holding, data.volume);
                holding -= tradeVolume;
                cash += tradeVolume * data.price;
                System.out.printf("時間 %d: 賣出 %d 股，價格 %.2f\n", i, tradeVolume, data.price);
            } else {
                System.out.printf("時間 %d: 持有\n", i);
            }

            totalAssetValue = cash + holding * data.price;
            System.out.printf("  持倉: %d, 現金: %.2f, 總資產: %.2f\n", holding, cash, totalAssetValue);
        }

        double totalReturn = totalAssetValue - initialCash;  // 總收益
        System.out.printf("總收益: %.2f\n", totalReturn);
    }
}