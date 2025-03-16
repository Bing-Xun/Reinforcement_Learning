package policyGradient.reinforce;

import java.util.*;

public class TransformerReinforce {

    // 环境、动作、参数等定义 (与之前的例子类似)
    private static final int[][] MAZE = {
        {0, 0, 0, 0, 0},
        {0, 1, 1, 1, 0},
        {0, 0, 0, 0, 0},
        {0, 1, 0, 1, 0},
        {0, 0, 0, 2, 0}
    };
    private static final int MAZE_ROWS = MAZE.length;
    private static final int MAZE_COLS = MAZE[0].length;
    private static final int UP = 0;    // 动作
    private static final int DOWN = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;
    private static final int[] ACTIONS = {UP, DOWN, LEFT, RIGHT};
    private static final double LEARNING_RATE = 0.001;
    private static final double DISCOUNT_FACTOR = 0.95;
    private static final int EPISODES = 5000;
    private static final Random random = new Random();

    // 简化 Transformer
    static class SimplifiedTransformer {
        private final int stateSize; // 状态的维度 (这里简化为 1，表示 "row,col" 字符串的哈希)
        private final int actionSize; // 动作的数量
        private final int embeddingSize; // 嵌入维度
        private final int headSize;      // 注意力头的维度

        // 权重矩阵 (Weights)
        private final double[][] Wq; // Query 权重
        private final double[][] Wk; // Key 权重
        private final double[][] Wv; // Value 权重
        private final double[][] Wo; // Output 权重 (W1, W2 的组合简化)

        public SimplifiedTransformer(int stateSize, int actionSize, int embeddingSize, int headSize) {
            this.stateSize = stateSize;
            this.actionSize = actionSize;
            this.embeddingSize = embeddingSize;
            this.headSize = headSize;

            // 初始化权重矩阵 (简单起见，使用随机初始化)
            this.Wq = initializeWeights(embeddingSize, headSize);
            this.Wk = initializeWeights(embeddingSize, headSize);
            this.Wv = initializeWeights(embeddingSize, headSize);
            this.Wo = initializeWeights(headSize, actionSize);
        }

        // 初始化权重矩阵 (He 初始化)
        private double[][] initializeWeights(int rows, int cols) {
            double[][] weights = new double[rows][cols];
            double stddev = Math.sqrt(2.0 / rows); // He 初始化
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    weights[i][j] = random.nextGaussian() * stddev;
                }
            }
            return weights;
        }

        // 矩阵乘法
        private double[] matrixMultiply(double[][] matrix, double[] vector) {
            int rows = matrix.length;
            int cols = matrix[0].length;
            double[] result = new double[rows];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    result[i] += matrix[i][j] * vector[j];
                }
            }
            return result;
        }
        //计算点积注意力
        private double[] scaledDotProductAttention(double[] query, double[][] keys, double[][] values) {
            int headSize = query.length;
            int seqLength = keys.length; // 序列长度 (时间步数)

            // 计算注意力分数 (query 与每个 key 的点积)
            double[] attentionScores = new double[seqLength];
            for (int i = 0; i < seqLength; i++) {
                for (int j = 0; j < headSize; j++) {
                    attentionScores[i] += query[j] * keys[i][j];
                }
                attentionScores[i] /= Math.sqrt(headSize); // 缩放
            }

            // Softmax 得到注意力权重
            double[] attentionWeights = softmax(attentionScores);

            // 加权求和得到 Value 的加权平均
            double[] contextVector = new double[headSize];
            for (int i = 0; i < seqLength; i++) {
                for (int j = 0; j < headSize; j++) {
                    contextVector[j] += attentionWeights[i] * values[i][j];
                }
            }
            return contextVector;
        }


        // 计算 Logits (核心方法)
        public double[] computeLogits(List<String> states, List<Integer> actions, List<Double> returns) {

            int seqLength = states.size(); // 序列长度

            // 1. 将状态、动作、回报嵌入到 embeddingSize 维度
            double[][] stateEmbeddings = new double[seqLength][embeddingSize];
            double[][] actionEmbeddings = new double[seqLength][embeddingSize];
            double[][] returnEmbeddings = new double[seqLength][embeddingSize];

            for (int t = 0; t < seqLength; t++) {
                // 简单的嵌入：使用哈希码 (实际应用中会更复杂)
                stateEmbeddings[t] = embed(states.get(t).hashCode(), embeddingSize);
                actionEmbeddings[t] = embed(actions.get(t), embeddingSize);
                returnEmbeddings[t] = embed(returns.get(t).hashCode(), embeddingSize); //回报也做嵌入
            }

            // 2. 将三种嵌入合并 (Concatenate)
            double[][] combinedEmbeddings = new double[seqLength][embeddingSize * 3];
            for (int t = 0; t < seqLength; t++) {
                System.arraycopy(stateEmbeddings[t], 0, combinedEmbeddings[t], 0, embeddingSize);
                System.arraycopy(actionEmbeddings[t], 0, combinedEmbeddings[t], embeddingSize, embeddingSize);
                System.arraycopy(returnEmbeddings[t], 0, combinedEmbeddings[t], embeddingSize * 2, embeddingSize);
            }


            // 3. 计算 Q, K, V
            double[][] queries = new double[seqLength][headSize];
            double[][] keys = new double[seqLength][headSize];
            double[][] values = new double[seqLength][headSize];

            for (int t = 0; t < seqLength; t++) {
                queries[t] = matrixMultiply(Wq, combinedEmbeddings[t]);
                keys[t] = matrixMultiply(Wk, combinedEmbeddings[t]);
                values[t] = matrixMultiply(Wv, combinedEmbeddings[t]);
            }

            // 4. 计算注意力 (Scaled Dot-Product Attention)
            //    (这里简化为只计算最后一个时间步的 context vector)
            double[] contextVector = scaledDotProductAttention(queries[seqLength-1], keys, values);

            // 5. 计算 Logits (通过 Wo 矩阵)
            double[] logits = matrixMultiply(Wo, contextVector);

            return logits;
        }


        // 嵌入函数 (简化)
        private double[] embed(int value, int embeddingSize) {
            double[] embedding = new double[embeddingSize];
            // 简单映射：将 value 映射到 embedding 的第一个元素
            embedding[0] = (double) value;
            // 其余元素随机初始化 (实际应用中会更复杂)
            for (int i = 1; i < embeddingSize; i++) {
                embedding[i] = random.nextGaussian() * 0.1;
            }
            return embedding;
        }

        // Softmax 函数
        private double[] softmax(double[] logits) {
            double maxLogit = Arrays.stream(logits).max().orElse(0.0);
            double[] expLogits = Arrays.stream(logits).map(l -> Math.exp(l - maxLogit)).toArray();
            double sumExpLogits = Arrays.stream(expLogits).sum();
            return Arrays.stream(expLogits).map(e -> e / sumExpLogits).toArray();
        }
    }

    // 初始化 Transformer
    private final SimplifiedTransformer transformer = new SimplifiedTransformer(
        1, // stateSize (简化)
        ACTIONS.length, // actionSize
        16, // embeddingSize
        8   // headSize
    );

    // 选择动作
    private int chooseAction(double[] logits) {
        double[] probabilities = transformer.softmax(logits); // 使用 Transformer 的 softmax
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

    // 其他辅助函数 (与之前的例子相同)
    private boolean isTerminalState(int row, int col) {
        return MAZE[row][col] == 2;
    }
    private double getReward(int row, int col) {
        if (isTerminalState(row, col)) {  return 10.0;
        } else if (MAZE[row][col] == 1) { return -5.0;
        } else { return -0.1; }
    }
    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < MAZE_ROWS && col >= 0 && col < MAZE_COLS && MAZE[row][col] != 1;
    }
    private List<Double> calculateReturns(List<Double> rewards) {
        List<Double> returns = new ArrayList<>();
        double G = 0;
        for (int i = rewards.size() - 1; i >= 0; i--) {
            G = rewards.get(i) + DISCOUNT_FACTOR * G;
            returns.add(0, G);
        }
        return returns;
    }

    // 训练
    public void train() {
        for (int episode = 0; episode < EPISODES; episode++) {
            List<String> states = new ArrayList<>();
            List<Integer> actions = new ArrayList<>();
            List<Double> rewards = new ArrayList<>();
            int currentRow = 0; int currentCol = 0;

            while (!isTerminalState(currentRow, currentCol)) {
                String state = currentRow + "," + currentCol;
                states.add(state);
                double[] logits = transformer.computeLogits(states, actions, rewards);
                int action = chooseAction(logits);
                actions.add(action);

                int nextRow = currentRow, nextCol = currentCol;
                switch (action) {
                    case UP:    nextRow--; break;
                    case DOWN:  nextRow++; break;
                    case LEFT:  nextCol--; break;
                    case RIGHT: nextCol++; break;
                }
                if (isValidMove(nextRow, nextCol)) {
                    currentRow = nextRow;
                    currentCol = nextCol;
                }
                rewards.add(getReward(currentRow, currentCol));
            }

            List<Double> returns = calculateReturns(rewards);

            // 更新 Transformer (梯度计算和更新，这里简化)
            double[] logits = transformer.computeLogits(states, actions, returns);
            // 假设我们有一个优化器 (optimizer)
            // optimizer.update(transformer, states, actions, returns, logits); // 伪代码

            if ((episode + 1) % 100 == 0) {
                System.out.println("Episode " + (episode + 1) + "/" + EPISODES);
            }
        }
        System.out.println("訓練完成!");
    }

    public static void main(String[] args) {
        TransformerReinforce agent = new TransformerReinforce();
        agent.train();
    }
}