package transformer;

import java.util.Random;

public class Transformer {

    public static void main(String[] args) {
        // 假设输入数据是 (3, 4) 形状的矩阵 (3 个时间步，每个 4 维特征)
        double[][] input = {
            {1.0, 2.0, 3.0, 4.0},
            {5.0, 6.0, 7.0, 8.0},
            {9.0, 10.0, 11.0, 12.0}
        };

        // 1. 计算 Q, K, V
        double[][] Wq = randomMatrix(4, 4);  // (d_model, d_k)
        double[][] Wk = randomMatrix(4, 4);
        double[][] Wv = randomMatrix(4, 4);

        double[][] Q = matMul(input, Wq);
        double[][] K = matMul(input, Wk);
        double[][] V = matMul(input, Wv);

        // 2. 计算 QK^T / sqrt(d_k)
        double[][] K_T = transposeMatrix(K);
        double[][] scores = matMul(Q, K_T);
        scaleMatrix(scores, Math.sqrt(4));  // d_k = 4

        // 3. Softmax 归一化
        double[][] attentionWeights = softmax(scores);

        // 4. 计算 Attention(Q, K, V)
        double[][] attentionOutput = matMul(attentionWeights, V);

        // 5. 通过前馈神经网络（FFN）
        double[][] W1 = randomMatrix(4, 8);  // (d_model, hidden_size)
        double[][] W2 = randomMatrix(8, 4);  // (hidden_size, d_model)

        double[][] ffOutput = relu(matMul(attentionOutput, W1));
        ffOutput = matMul(ffOutput, W2);

        // 6. 添加残差连接 + LayerNorm
        double[][] finalOutput = layerNorm(addMatrix(attentionOutput, ffOutput));

        // 输出最终结果
        printMatrix(finalOutput);
    }

    // 矩阵乘法
    static double[][] matMul(double[][] A, double[][] B) {
        int m = A.length, n = B[0].length, p = B.length;
        double[][] result = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < p; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return result;
    }

    // 矩阵转置
    static double[][] transposeMatrix(double[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    // Softmax 归一化
    static double[][] softmax(double[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        double[][] softmaxMatrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            double maxVal = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] > maxVal) maxVal = matrix[i][j];
            }

            double sumExp = 0.0;
            for (int j = 0; j < cols; j++) {
                softmaxMatrix[i][j] = Math.exp(matrix[i][j] - maxVal);
                sumExp += softmaxMatrix[i][j];
            }

            for (int j = 0; j < cols; j++) {
                softmaxMatrix[i][j] /= sumExp;
            }
        }
        return softmaxMatrix;
    }

    // 生成随机矩阵
    static double[][] randomMatrix(int rows, int cols) {
        Random rand = new Random();
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextDouble();
            }
        }
        return matrix;
    }

    // 矩阵缩放
    static void scaleMatrix(double[][] matrix, double scale) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] /= scale;
            }
        }
    }

    // ReLU 激活函数
    static double[][] relu(double[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        double[][] reluMatrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                reluMatrix[i][j] = Math.max(0, matrix[i][j]);
            }
        }
        return reluMatrix;
    }

    // 矩阵相加
    static double[][] addMatrix(double[][] A, double[][] B) {
        int rows = A.length, cols = A[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = A[i][j] + B[i][j];
            }
        }
        return result;
    }

    // Layer Normalization
    static double[][] layerNorm(double[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        double[][] normMatrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            double mean = 0.0, variance = 0.0;

            for (int j = 0; j < cols; j++) {
                mean += matrix[i][j];
            }
            mean /= cols;

            for (int j = 0; j < cols; j++) {
                variance += Math.pow(matrix[i][j] - mean, 2);
            }
            variance /= cols;
            double stdDev = Math.sqrt(variance + 1e-6);

            for (int j = 0; j < cols; j++) {
                normMatrix[i][j] = (matrix[i][j] - mean) / stdDev;
            }
        }
        return normMatrix;
    }

    // 打印矩阵
    static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double val : row) {
                System.out.printf("%.4f ", val);
            }
            System.out.println();
        }
    }
}
