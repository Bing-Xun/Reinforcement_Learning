package lstm;


import java.util.Arrays;

public class SimpleLSTM {

    private static final int inputSize = 2; // 输入特征数量
    private static final int hiddenSize = 16; // LSTM单元的隐藏层维度

    private double[][] Wf, Wi, Wc, Wo; // 权重矩阵
    private double[] bf, bi, bc, bo;   // 偏置
    private double[] h, c; // 隐藏状态和记忆单元

    // 用于存储梯度
    private double[][] dWf, dWi, dWc, dWo;
    private double[] dbf, dbi, dbc, dbo;

    // 需要添加声明这些变量来在forward和backward中共享
    private double[] f, inputGate, c_prime, o;

    private double[] c_prev;
    private double[] h_prev;

    public SimpleLSTM() {
        Wf = randomMatrix(hiddenSize, inputSize + hiddenSize);
        Wi = randomMatrix(hiddenSize, inputSize + hiddenSize);
        Wc = randomMatrix(hiddenSize, inputSize + hiddenSize);
        Wo = randomMatrix(hiddenSize, inputSize + hiddenSize);

        bf = randomArray(hiddenSize);
        bi = randomArray(hiddenSize);
        bc = randomArray(hiddenSize);
        bo = randomArray(hiddenSize);

        h = new double[hiddenSize];
        c = new double[hiddenSize];

        // 初始化梯度矩阵和偏置梯度
        dWf = new double[hiddenSize][inputSize + hiddenSize];
        dWi = new double[hiddenSize][inputSize + hiddenSize];
        dWc = new double[hiddenSize][inputSize + hiddenSize];
        dWo = new double[hiddenSize][inputSize + hiddenSize];

        dbf = new double[hiddenSize];
        dbi = new double[hiddenSize];
        dbc = new double[hiddenSize];
        dbo = new double[hiddenSize];

        // 初始化用于存储门的输出
        f = new double[hiddenSize];
        inputGate = new double[hiddenSize];
        c_prime = new double[hiddenSize];
        o = new double[hiddenSize];
    }

    // forward 方法不变，更新前向传播计算逻辑
    public double[] forward(double[] x) {
        // 先記錄之前的c跟h
        this.c_prev = c;
        this.h_prev = h;

        // 拼接当前输入和上一时间步的隐藏状态
        double[] concat = new double[inputSize + hiddenSize];
        System.arraycopy(x, 0, concat, 0, inputSize);  // 将当前时间步的输入复制到 concat 中
        System.arraycopy(h, 0, concat, inputSize, hiddenSize);  // 将上一时间步的隐藏状态复制到 concat 中

        // 计算遗忘门
        for (int i = 0; i < hiddenSize; i++) {
            f[i] = sigmoid(dot(Wf[i], concat) + bf[i]);
        }

        // 计算输入门
        for (int i = 0; i < hiddenSize; i++) {
            inputGate[i] = sigmoid(dot(Wi[i], concat) + bi[i]);
        }

        // 计算候选记忆单元
        for (int i = 0; i < hiddenSize; i++) {
            c_prime[i] = tanh(dot(Wc[i], concat) + bc[i]);
        }

        // 更新当前记忆单元
        for (int i = 0; i < hiddenSize; i++) {
            c[i] = f[i] * c[i] + inputGate[i] * c_prime[i]; // 這裡的c -> 之後的c_prev
        }

        // 计算输出门
        for (int i = 0; i < hiddenSize; i++) {
            o[i] = sigmoid(dot(Wo[i], concat) + bo[i]);
        }

        // 更新隐藏状态
        for (int i = 0; i < hiddenSize; i++) {
            h[i] = o[i] * tanh(c[i]);
        }

        // 返回当前时间步的隐藏状态作为输出
        return h;
    }

    // 仅预测的 forward 方法
    public double[] predict(double[] x) {
        double[] concat = new double[inputSize + hiddenSize];
        System.arraycopy(x, 0, concat, 0, inputSize);
        System.arraycopy(h, 0, concat, inputSize, hiddenSize);

        double[] c_temp = new double[hiddenSize]; // 临时记忆单元状态
        double[] h_temp = new double[hiddenSize]; // 临时隐藏状态

        for (int i = 0; i < hiddenSize; i++) {
            double f = sigmoid(dot(Wf[i], concat) + bf[i]);
            double inputGate = sigmoid(dot(Wi[i], concat) + bi[i]);
            double c_prime = tanh(dot(Wc[i], concat) + bc[i]);

            c_temp[i] = f * c_temp[i] + inputGate * c_prime;
            double o = sigmoid(dot(Wo[i], concat) + bo[i]);
            h_temp[i] = o * tanh(c_temp[i]);
        }

        // 返回隐藏状态
        return h_temp;
    }

    public double forwardH(double[] h) {
        // 隐藏层 -> 输出层
        double weightedSum = 0;
        for (int i = 0; i < hiddenSize; i++) {
            weightedSum += h[i] * (i + 1);  // 用索引或其他方法加权
        }

        // 使用 sigmoid 或 tanh 映射到 [-1, 1]
        return tanh(weightedSum);
    }

    // 反向传播计算梯度
//    public void backward(double[] x, double[] dL_dh) {
//        // 初始化上一时间步梯度
//        double[] dL_dh_prev = new double[hiddenSize];
//        double[] dL_dc = new double[hiddenSize]; // 对记忆单元的梯度
//
//        // 更新记忆单元梯度
//        for (int i = 0; i < hiddenSize; i++) {
//            dL_dc[i] = dL_dh[i] * o[i] * (1 - Math.pow(tanh(c[i]), 2));
//        }
//
//        // 更新各门的梯度
//        double[] combinedInput = getCombinedInput(x, h_prev);
//        for (int i = 0; i < hiddenSize; i++) {
//            for (int j = 0; j < inputSize + hiddenSize; j++) {
//                // 遗忘门
//                dWf[i][j] += dL_dc[i] * c_prev[i] * f[i] * (1 - f[i]) * combinedInput[j];
//                // 输入门
//                dWi[i][j] += dL_dc[i] * c_prime[i] * inputGate[i] * (1 - inputGate[i]) * combinedInput[j];
//                // 候选记忆
//                dWc[i][j] += dL_dc[i] * inputGate[i] * (1 - Math.pow(c_prime[i], 2)) * combinedInput[j];
//                // 输出门
//                dWo[i][j] += dL_dh[i] * tanh(c[i]) * o[i] * (1 - o[i]) * combinedInput[j];
//            }
//        }
//
//        // 更新偏置梯度
//        for (int i = 0; i < hiddenSize; i++) {
//            dbf[i] += dL_dc[i] * f[i] * (1 - f[i]);
//            dbi[i] += dL_dc[i] * inputGate[i] * (1 - inputGate[i]);
//            dbc[i] += dL_dc[i] * (1 - Math.pow(c_prime[i], 2));
//            dbo[i] += dL_dh[i] * tanh(c[i]) * o[i] * (1 - o[i]);
//        }
//
//        // 计算上一时间步的隐藏状态梯度
//        for (int i = 0; i < hiddenSize; i++) {
//            for (int j = 0; j < hiddenSize; j++) {
//                dL_dh_prev[i] += dL_dc[j] * Wf[j][inputSize + i];
//            }
//        }
//
//        // 此时 dL_dh_prev 已更新完毕，可以传递给上一时间步
//    }


    // 将当前隐藏状态的梯度通过 Wf 的隐藏状态部分传播到上一时间步
    public void updateHiddenState(double[] dL_dh_prev, double[] dL_dh) {
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                // Wf 的隐藏状态部分索引是 [inputSize, inputSize + hiddenSize)
                dL_dh_prev[i] += dL_dh[j] * Wf[j][inputSize + i];
            }
        }
    }

    // 随机生成矩阵
    private double[][] randomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 0.1; // 随机小数值
            }
        }
        return matrix;
    }

    // 随机生成数组
    private double[] randomArray(int size) {
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = Math.random() * 0.1; // 随机小数值
        }
        return array;
    }

    // Sigmoid 激活函数
    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    // 双曲正切激活函数
    private double tanh(double x) {
        return Math.tanh(x);
    }

    // 计算点积
    private double dot(double[] a, double[] b) {
        double result = 0.0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        return result;
    }

    public double[] getDL_dh(double tdError) {
        // 计算损失的梯度，假设当前网络输出的值为 V(s_t)
        double[] dL_dh = new double[hiddenSize];  // 存储梯度

        // 使用链式法则计算梯度
        for (int i = 0; i < hiddenSize; i++) {
            // 假设使用简单的梯度传递，dL_dh = tdError * (某种函数的导数)
            dL_dh[i] = tdError * h[i];  // 这是一个简化的例子，你的梯度计算可能会更复杂
        }

        return dL_dh;
    }

    public double[] getCombinedInput(double[] x, double[] h_prev) {
        // 创建拼接后的向量
        double[] combinedInput = new double[inputSize + hiddenSize];
        // 将 h_prev 放在 combinedInput 的前部分
        System.arraycopy(h_prev, 0, combinedInput, 0, hiddenSize);
        // 将 x 放在 combinedInput 的后部分
        System.arraycopy(x, 0, combinedInput, hiddenSize, inputSize);

        return combinedInput;
    }

    public double[] getH() {
        return this.h;
    }

    public double[][] getDL_next(double[] dL_dh) {
        // 初始化梯度
        double[] dL_dc = new double[inputSize]; // 对记忆单元的梯度
        double[] dL_dc_next = new double[hiddenSize];
        double[] dL_dh_next = new double[hiddenSize];

        // 更新记忆单元梯度
        for (int i = 0; i < inputSize; i++) {
            dL_dc[i] = dL_dh[i] * o[i] * (1 - Math.pow(tanh(c[i]), 2));
        }

        // 计算 dL_dc_next
        for (int k = 0; k < hiddenSize; k++) {
            for (int i = 0; i < inputSize; i++) {
                dL_dc_next[k] += dL_dc[i] * Wf[i][k];
                dL_dc_next[k] += dL_dh[i] * o[i] * (1 - Math.pow(Math.tanh(c[i]), 2)) * Wf[i][k];
            }
        }

        // 计算当前时间步对隐藏状态的梯度 dL_dh_next
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                dL_dh_next[i] += dL_dc[j] * Wf[j][inputSize + i]; // 传播来自遗忘门的影响
                dL_dh_next[i] += dL_dh[j] * Wo[j][inputSize + i]; // 传播来自输出门的影响
            }
        }

        return new double[][]{dL_dc_next, dL_dh_next};
    }

    public double[] backward(double[] dL_dh_input) {
        // 1. 从 inputSize 到 hiddenSize 的梯度传播
        double[] dL_dh_hidden = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            dL_dh_hidden[i] = 0;
            for (int j = 0; j < inputSize; j++) {
                dL_dh_hidden[i] += dL_dh_input[j] * (Wf[i][j] + Wi[i][j] + Wc[i][j] + Wo[i][j]);
            }
        }

        // 2. 执行 LSTM 的反向传播
        double[] dL_dc = new double[hiddenSize];
        double[] dL_dx = new double[inputSize];

        for (int i = 0; i < hiddenSize; i++) {
            dL_dc[i] = dL_dh_hidden[i] * o[i] * (1 - tanh(c[i]) * tanh(c[i]));

            double dL_df = dL_dc[i] * c_prev[i] * f[i] * (1 - f[i]);
            double dL_di = dL_dc[i] * c_prime[i] * inputGate[i] * (1 - inputGate[i]);
            double dL_dc_prime = dL_dc[i] * inputGate[i] * (1 - c_prime[i] * c_prime[i]);
            double dL_do = dL_dh_hidden[i] * tanh(c[i]) * o[i] * (1 - o[i]);

            // 回传到输入层
            for (int j = 0; j < inputSize; j++) {
                dL_dx[j] += dL_df * Wf[i][j] + dL_di * Wi[i][j] + dL_dc_prime * Wc[i][j] + dL_do * Wo[i][j];
            }
        }

        return dL_dx;
    }

    public void backward(double[] x, double[] dL_dh) {
        double[][] d = getDL_next(dL_dh);
        backward(backward(dL_dh), x, d[1], d[0]);
    }

    // backward 方法，从输入层梯度更新隐藏层梯度
    private void backward(double[] dL_dx, double[] x, double[] dL_dh_next, double[] dL_dc_next) {
        // 拼接输入和上一时间步的隐藏状态
        double[] combinedInput = new double[inputSize + hiddenSize];
        System.arraycopy(x, 0, combinedInput, 0, inputSize);
        System.arraycopy(h_prev, 0, combinedInput, inputSize, hiddenSize);

        // 初始化当前时间步梯度
        double[] dL_dh = new double[hiddenSize];
        double[] dL_dc = new double[hiddenSize];

        // 将输入梯度传播到隐藏层
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                dL_dh[i] += dL_dx[j] * Wf[i][j]; // 遗忘门权重贡献
                dL_dh[i] += dL_dx[j] * Wi[i][j]; // 输入门权重贡献
                dL_dh[i] += dL_dx[j] * Wc[i][j]; // 候选记忆权重贡献
                dL_dh[i] += dL_dx[j] * Wo[i][j]; // 输出门权重贡献
            }
        }

        // 对当前时间步的记忆单元进行梯度反传
        for (int i = 0; i < hiddenSize; i++) {
            dL_dc[i] = dL_dh[i] * o[i] * (1 - Math.pow(tanh(c[i]), 2)); // 激活函数的链式法则
        }

        // 更新门的梯度
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize + hiddenSize; j++) {
                // 遗忘门梯度更新
                dWf[i][j] += dL_dc[i] * f[i] * (1 - f[i]) * combinedInput[j];
                // 输入门梯度更新
                dWi[i][j] += dL_dc[i] * c_prime[i] * inputGate[i] * (1 - inputGate[i]) * combinedInput[j];
                // 候选记忆梯度更新
                dWc[i][j] += dL_dc[i] * inputGate[i] * (1 - Math.pow(c_prime[i], 2)) * combinedInput[j];
                // 输出门梯度更新
                dWo[i][j] += dL_dh[i] * tanh(c[i]) * o[i] * (1 - o[i]) * combinedInput[j];
            }
        }

        // 更新偏置梯度
        for (int i = 0; i < hiddenSize; i++) {
            dbf[i] += dL_dc[i] * f[i] * (1 - f[i]);
            dbi[i] += dL_dc[i] * inputGate[i] * (1 - inputGate[i]);
            dbc[i] += dL_dc[i] * (1 - Math.pow(c_prime[i], 2));
            dbo[i] += dL_dh[i] * tanh(c[i]) * o[i] * (1 - o[i]);
        }

        // 将当前梯度传递到上一时间步
        for (int i = 0; i < hiddenSize; i++) {
            dL_dh_next[i] = 0;
            for (int j = 0; j < hiddenSize; j++) {
                dL_dh_next[i] += dL_dc[j] * Wf[j][inputSize + i];
            }
        }

        // 更新记忆单元的状态梯度
        for (int i = 0; i < hiddenSize; i++) {
            dL_dc_next[i] = dL_dc[i] * f[i];
        }
    }


    public static void main(String[] args) {
        SimpleLSTM lstm = new SimpleLSTM();

        // 输入数据
        double[] input = {0.1, 0.2}; // 输入序列的第一项

        // 前向传播
        double[] output = lstm.forward(input);
        System.out.println("Output: " + Arrays.toString(output));

        double criticTdErr = 0.3; //ex
        double[] dL_dh = lstm.getDL_dh(criticTdErr);

        lstm.backward(input, dL_dh);

        // 你可以在这个时候使用 lstm.getH() 获取当前时间步的隐藏状态输出
        double[] currentOutput = lstm.getH();
        System.out.println("Current Output after Backward: " + Arrays.toString(currentOutput));

        double d = lstm.forwardH(currentOutput);
        System.out.println(d);
    }
}
