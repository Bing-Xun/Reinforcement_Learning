package NeuralNetwork;

import java.util.Random;

public class DeepNeuralNetwork {

    // 定義層數和神經元數
    private int inputSize;
    private int hiddenSize;
    private int outputSize;

    private double[][] weights1;
    private double[][] weights2;
    private double[] bias1;
    private double[] bias2;

    private double learningRate;
    private Random rand;

    // 初始化網絡
    public DeepNeuralNetwork(int inputSize, int hiddenSize, int outputSize, double learningRate) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        this.learningRate = learningRate;

        rand = new Random();

        // 隨機初始化權重和偏置
        weights1 = new double[inputSize][hiddenSize];
        weights2 = new double[hiddenSize][outputSize];
        bias1 = new double[hiddenSize];
        bias2 = new double[outputSize];

        initializeWeights();
    }

    // 隨機初始化權重
    private void initializeWeights() {
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weights1[i][j] = rand.nextDouble() * 0.01;
            }
        }

        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weights2[i][j] = rand.nextDouble() * 0.01;
            }
        }

        for (int i = 0; i < hiddenSize; i++) {
            bias1[i] = rand.nextDouble() * 0.01;
        }

        for (int i = 0; i < outputSize; i++) {
            bias2[i] = rand.nextDouble() * 0.01;
        }
    }

    // ReLU 激活函數
    private double relu(double x) {
        return Math.max(0, x);
    }

    // ReLU 激活函數的導數
    private double reluDerivative(double x) {
        return x > 0 ? 1 : 0;
    }

    // 預測過程
    public double[] predict(double[] input) {
        // 隱藏層
        double[] hiddenLayer = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            hiddenLayer[i] = 0;
            for (int j = 0; j < inputSize; j++) {
                hiddenLayer[i] += input[j] * weights1[j][i];
            }
            hiddenLayer[i] += bias1[i];
            hiddenLayer[i] = relu(hiddenLayer[i]);
        }

        // 輸出層
        double[] outputLayer = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            outputLayer[i] = 0;
            for (int j = 0; j < hiddenSize; j++) {
                outputLayer[i] += hiddenLayer[j] * weights2[j][i];
            }
            outputLayer[i] += bias2[i];
        }

        return outputLayer;
    }

    // 計算誤差和反向傳播
    public void backpropagate(double[] input, double[] target) {
        // 前向傳播
        double[] hiddenLayer = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            hiddenLayer[i] = 0;
            for (int j = 0; j < inputSize; j++) {
                hiddenLayer[i] += input[j] * weights1[j][i];
            }
            hiddenLayer[i] += bias1[i];
            hiddenLayer[i] = relu(hiddenLayer[i]);
        }

        double[] outputLayer = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            outputLayer[i] = 0;
            for (int j = 0; j < hiddenSize; j++) {
                outputLayer[i] += hiddenLayer[j] * weights2[j][i];
            }
            outputLayer[i] += bias2[i];
        }

        // 計算輸出層的誤差
        double[] outputError = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            outputError[i] = target[i] - outputLayer[i];
        }

        // 計算隱藏層的誤差
        double[] hiddenError = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            hiddenError[i] = 0;
            for (int j = 0; j < outputSize; j++) {
                hiddenError[i] += outputError[j] * weights2[i][j];
            }
            hiddenError[i] *= reluDerivative(hiddenLayer[i]);
        }

        // 更新權重和偏置
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weights2[i][j] += learningRate * outputError[j] * hiddenLayer[i];
            }
        }

        for (int i = 0; i < outputSize; i++) {
            bias2[i] += learningRate * outputError[i];
        }

        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weights1[i][j] += learningRate * hiddenError[j] * input[i];
            }
        }

        for (int i = 0; i < hiddenSize; i++) {
            bias1[i] += learningRate * hiddenError[i];
        }
    }
}
