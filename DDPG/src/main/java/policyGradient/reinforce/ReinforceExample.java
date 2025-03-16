package policyGradient.reinforce;

import java.util.*;

public class ReinforceExample {

    // 環境 (Environment): 簡化的迷宮 (同之前的例子)
    private static final int[][] MAZE = {
        {0, 0, 0, 0, 0},
        {0, 1, 1, 1, 0},
        {0, 0, 0, 0, 0},
        {0, 1, 0, 1, 0},
        {0, 0, 0, 2, 0}
    };
    private static final int MAZE_ROWS = MAZE.length;
    private static final int MAZE_COLS = MAZE[0].length;

    // 動作 (Actions)
    private static final int UP = 0;
    private static final int DOWN = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;
    private static final int[] ACTIONS = {UP, DOWN, LEFT, RIGHT};

    // REINFORCE 參數
    private static final double LEARNING_RATE = 0.01; // 學習率
    private static final double DISCOUNT_FACTOR = 0.99; // 折扣因子
    private static final int EPISODES = 5000;  // 訓練回合數

    // 策略 (Policy):  HashMap<String, double[]>
    // Key:  狀態 (state)，表示為 "row,col"
    // Value: 動作機率分佈 (action probabilities)，double[4] 分別對應 UP, DOWN, LEFT, RIGHT 的機率
    private final Map<String, double[]> policy = new HashMap<>();
    private final Random random = new Random();

    // 取得策略 (若狀態不存在，則初始化為均勻分佈)
    private double[] getPolicy(int row, int col) {
        String state = row + "," + col;
        return policy.computeIfAbsent(state, k -> {
            double[] probabilities = new double[ACTIONS.length];
            Arrays.fill(probabilities, 1.0 / ACTIONS.length); // 初始為均勻分佈
            return probabilities;
        });
    }

    // 根據策略選擇動作 (依機率分佈)
    private int chooseAction(int row, int col) {
        double[] probabilities = getPolicy(row, col);
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < ACTIONS.length; i++) {
            cumulativeProbability += probabilities[i];
            if (randomValue < cumulativeProbability) {
                return i;
            }
        }
        return ACTIONS.length - 1; // 應該永遠不會執行到這裡，但為了安全起見
    }

    // 檢查是否為終止狀態
    private boolean isTerminalState(int row, int col) {
        return MAZE[row][col] == 2;
    }

    // 取得獎勵
    private double getReward(int row, int col) {
        if (isTerminalState(row, col)) {
            return 10.0; // 到達目標，大獎勵
        } else if (MAZE[row][col] == 1) {
            return -5.0; // 撞牆，大懲罰
        } else {
            return -0.1;  // 每走一步，小懲罰
        }
    }

    // 檢查移動是否有效
    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < MAZE_ROWS && col >= 0 && col < MAZE_COLS && MAZE[row][col] != 1;
    }

    // softmax 函數 (將 logits 轉換為機率)
    private double[] softmax(double[] logits) {
        double maxLogit = Arrays.stream(logits).max().orElse(0.0); // 避免數值溢位
        double[] expLogits = Arrays.stream(logits).map(l -> Math.exp(l - maxLogit)).toArray();
        double sumExpLogits = Arrays.stream(expLogits).sum();
        return Arrays.stream(expLogits).map(e -> e / sumExpLogits).toArray();
    }

    // 計算回報 (Return)
    private List<Double> calculateReturns(List<Double> rewards) {
        List<Double> returns = new ArrayList<>();
        double G = 0;
        for (int i = rewards.size() - 1; i >= 0; i--) {
            G = rewards.get(i) + DISCOUNT_FACTOR * G;
            returns.add(0, G); // 在開頭插入，保持與原順序一致
        }
        return returns;
    }


    // 訓練 (Training)
    public void train() {
        for (int episode = 0; episode < EPISODES; episode++) {
            // 收集軌跡 (Collect Trajectory)
            List<String> states = new ArrayList<>();
            List<Integer> actions = new ArrayList<>();
            List<Double> rewards = new ArrayList<>();

            int currentRow = 0;
            int currentCol = 0;

            while (!isTerminalState(currentRow, currentCol)) {
                String state = currentRow + "," + currentCol;
                states.add(state);

                int action = chooseAction(currentRow, currentCol);
                actions.add(action);

                // 執行動作，獲得下一個狀態和獎勵
                int nextRow = currentRow, nextCol = currentCol;
                switch (action) {
                    case UP:    nextRow--; break;
                    case DOWN:  nextRow++; break;
                    case LEFT:  nextCol--; break;
                    case RIGHT: nextCol++; break;
                }

                // 檢查是否有效移動
                if (isValidMove(nextRow, nextCol)) {
                    currentRow = nextRow;
                    currentCol = nextCol;
                }
                //無效移動，給予嚴厲懲罰，並保持在原位
                double reward = getReward(currentRow, currentCol);
                rewards.add(reward);
            }


            // 計算回報 (Calculate Returns)
            List<Double> returns = calculateReturns(rewards);

            // 更新策略 (Update Policy)
            for (int t = 0; t < states.size(); t++) {
                String state = states.get(t);
                int action = actions.get(t);
                double G = returns.get(t);

                double[] probabilities = getPolicy(
                    Integer.parseInt(state.split(",")[0]),
                    Integer.parseInt(state.split(",")[1])
                );

                // 計算策略梯度 (簡化版，直接在機率上操作)
                double[] gradients = new double[ACTIONS.length];
                for (int a = 0; a < ACTIONS.length; a++) {
                    gradients[a] = (a == action ? 1 : 0) - probabilities[a]; // One-hot encoding
                    gradients[a] *= G;  //乘以回報
                }
                //更新策略(加上梯度)
                double [] logits = new double[ACTIONS.length];
                for(int a = 0; a<ACTIONS.length; a++){
                    logits[a] = Math.log(probabilities[a]) + LEARNING_RATE * gradients[a];
                }

                double[] updatedProbabilities = softmax(logits); //套用Softmax

                policy.put(state, updatedProbabilities); // 更新策略

            }
        }
        System.out.println("訓練完成!");
        printPolicy();

    }

    //顯示策略
    public void printPolicy() {
        System.out.println("Learned Policy:");
        for (int row = 0; row < MAZE_ROWS; row++) {
            for (int col = 0; col < MAZE_COLS; col++) {
                String state = row + "," + col;
                if (policy.containsKey(state)) {
                    double[] probabilities = policy.get(state);
                    String bestAction = "";
                    switch (getMaxIndex(probabilities)) {
                        case UP:    bestAction = "↑"; break;
                        case DOWN:  bestAction = "↓"; break;
                        case LEFT:  bestAction = "←"; break;
                        case RIGHT: bestAction = "→"; break;
                    }
                    System.out.print(bestAction + " ");
                } else {
                    System.out.print("■ "); // 牆壁或其他不可達狀態
                }
            }
            System.out.println();
        }
    }

    // 找到陣列中最大值的索引
    private int getMaxIndex(double[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }



    public static void main(String[] args) {
        ReinforceExample agent = new ReinforceExample();
        agent.train();
    }
}