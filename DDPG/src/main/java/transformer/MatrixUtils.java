package transformer;

import java.util.Random;
import java.util.concurrent.*;

public class MatrixUtils {

    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final Random random = new Random();

    /**
     * 先不用多緒
     */
//    public static double[][] matMul(double[][] A, double[][] B) {
//        int m = A.length, n = B[0].length, p = B.length;
//        System.out.println(String.format("m:%s, n:%s, p:%s, a:%s", m,n,p, A[0].length));
//        if (A.length == 0 || A[0].length == 0 || B.length == 0 || B[0].length == 0) {
//            throw new IllegalArgumentException("矩阵不能为空");
//        }
//        if (A[0].length != B.length) {
//            throw new IllegalArgumentException(String.format(
//                "矩阵尺寸不匹配: A[%d x %d], B[%d x %d]",
//                A.length, A[0].length, B.length, B[0].length));
//        }
//
//        double[][] result = new double[m][n];
//
//        Future<?>[] futures = new Future[m];
//        for (int i = 0; i < m; i++) {
//            final int row = i;
//            futures[i] = executor.submit(() -> {
//                for (int j = 0; j < n; j++) {
//                    for (int k = 0; k < p; k++) {
//                        result[row][j] += A[row][k] * B[k][j];
//                    }
//                }
//            });
//        }
//        waitForCompletion(futures);
//        return result;
//    }

    // 矩阵乘法
    public static double[][] matMul(double[][] A, double[][] B) {
        int rowsA = A.length, colsA = A[0].length;
        int rowsB = B.length, colsB = B[0].length;
        System.out.println(String.format("a1:%s, a2:%s, b1:%s, b2:%s", A.length,A[0].length,B.length, B[0].length));
        if (colsA != rowsB) throw new IllegalArgumentException("Matrix dimensions do not match for multiplication");

        double[][] result = new double[rowsA][colsB];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0.0;
                for (int k = 0; k < colsA; k++) {
                    sum += A[i][k] * B[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    public static double[][] transposeMatrix(double[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    public static double[][] softmax(double[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        double[][] softmaxMatrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            double maxVal = Double.NEGATIVE_INFINITY;
            for (double val : matrix[i]) maxVal = Math.max(maxVal, val);

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

    public static double[][] addMatrix(double[][] A, double[][] B) {
        int rows = A.length, cols = A[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = A[i][j] + B[i][j];
            }
        }
        return result;
    }

    public static double[][] layerNorm(double[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        double[][] normMatrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            double mean = 0, variance = 0;
            for (double val : matrix[i]) mean += val;
            mean /= cols;
            for (double val : matrix[i]) variance += Math.pow(val - mean, 2);
            variance /= cols;
            double stdDev = Math.sqrt(variance + 1e-6);

            for (int j = 0; j < cols; j++) {
                normMatrix[i][j] = (matrix[i][j] - mean) / stdDev;
            }
        }
        return normMatrix;
    }

    public static double[][] gelu(double[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = matrix[i][j];
                result[i][j] = 0.5 * x * (1 + Math.tanh(Math.sqrt(2 / Math.PI) * (x + 0.044715 * Math.pow(x, 3))));
            }
        }
        return result;
    }

    private static void waitForCompletion(Future<?>[] futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double val : row) {
                System.out.printf("%.4f ", val);
            }
            System.out.println();
        }
    }

    public static double[][] randomMatrix(int rows, int cols) {
        Random rand = new Random();
        double[][] matrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextGaussian() * 0.02; // 使用正态分布初始化
            }
        }
        return matrix;
    }

    public static double[][] scaleMatrix(double[][] matrix, double scale) {
        int rows = matrix.length, cols = matrix[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] /= scale;
            }
        }
        return matrix;
    }

    public static double[][] gradientDescentUpdate(double[][] weights, double[][] gradients, double learningRate) {
        int wRows = weights.length, wCols = weights[0].length;
        int gRows = gradients.length, gCols = gradients[0].length;

        // Debug: 打印矩陣形狀
        System.out.println("Weights Shape: " + wRows + "x" + wCols);
        System.out.println("Gradients Shape: " + gRows + "x" + gCols);

        if (wRows != gRows || wCols != gCols) {
            throw new IllegalArgumentException("Weights and gradients must have the same shape!");
        }

        double[][] updatedWeights = new double[wRows][wCols];

        for (int i = 0; i < wRows; i++) {
            for (int j = 0; j < wCols; j++) {
                updatedWeights[i][j] = weights[i][j] - learningRate * gradients[i][j];
            }
        }
        return updatedWeights;
    }

    /**
     * 计算均方误差 (MSE)
     * @param predicted 预测输出
     * @param target 目标输出
     * @return 均方误差
     */
    public static double meanSquaredError(double[][] predicted, double[][] target) {
        int rows = predicted.length;
        int cols = predicted[0].length;
        double sum = 0.0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double diff = predicted[i][j] - target[i][j];
                sum += diff * diff;
            }
        }

        return sum / (rows * cols);
    }

    /**
     * 矩阵相减 (A - B)
     * @param A 矩阵 A
     * @param B 矩阵 B
     * @return 结果矩阵 (A - B)
     */
    public static double[][] subtractMatrix(double[][] A, double[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int rowsB = B.length;
        int colsB = B[0].length;

        if (rowsA != rowsB || colsA != colsB) {
            throw new IllegalArgumentException("Matrix dimensions must match: " +
                "A is " + rowsA + "x" + colsA + ", B is " + rowsB + "x" + colsB);
        }

        double[][] result = new double[rowsA][colsA];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsA; j++) {
                result[i][j] = A[i][j] - B[i][j];
            }
        }

        return result;
    }

    public static double[][] heInit(int rows, int cols) {
        double stddev = Math.sqrt(2.0 / rows);
        return randomGaussianMatrix(rows, cols, 0, stddev);
    }

    public static double[][] randomGaussianMatrix(int rows, int cols, double mean, double stddev) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = mean + stddev * random.nextGaussian(); // 生成符合 N(mean, stddev^2) 的值
            }
        }
        return matrix;
    }

    public static double[][] clipGradients(double[][] grad, double maxNorm) {
        double norm = 0.0;
        for (int i = 0; i < grad.length; i++) {
            for (int j = 0; j < grad[i].length; j++) {
                norm += grad[i][j] * grad[i][j];
            }
        }
        norm = Math.sqrt(norm);

        if (norm > maxNorm) {
            double scale = maxNorm / norm;
            for (int i = 0; i < grad.length; i++) {
                for (int j = 0; j < grad[i].length; j++) {
                    grad[i][j] *= scale;
                }
            }
        }
        return grad;
    }

    /**
     * 降維度
     */
    // 投影矩阵 (Transformer 输出 -> 3 类趋势概率)
//    public static double[][] projectMatrix(double[][] matrix, double[][] W_proj) {
//        System.out.println("W_proj shape: " + W_proj.length + " x " + W_proj[0].length);
//        double[][] pooled = meanPooling(matrix); // (1, d_model)
//        System.out.println(String.format("pooled:%s pooled[0]:%s", pooled.length, pooled[0].length));
//        double[][] projected = matMul(pooled, W_proj); // (1, 3)
//        return softmax(projected); // 归一化
//    }
    public static double[][] projectMatrix(double[][] matrix, double[][] W_proj) {
        System.out.println("matrix shape: " + matrix.length + " x " + matrix[0].length);
        System.out.println("W_proj shape: " + W_proj.length + " x " + W_proj[0].length);

        // 先与 W_proj 相乘，得到投影后的矩阵
        return matMul(matrix, W_proj); // (N, 3) 假设 N 是 matrix 的行数
    }

    public static double[][] poolProject(double[][] projected) {
        // 然后进行池化操作（例如均值池化）
        double[][] pooled = meanPooling(projected); // (1, 3)

        // 输出池化后的结果
        System.out.println(String.format("pooled:%s pooled[0]:%s", pooled.length, pooled[0].length));

        // 最后进行 softmax 归一化
        return softmax(pooled); // (1, 3)
    }

    // lossGrad: 当前池化后的梯度，形状是 (batch_size, output_dim)
    public static double[][] unpoolGrad(double[][] lossGrad, int batch_size, int poolSize) {
        int unpooledRows = batch_size * poolSize;  // 还原原始行数
        int unpooledCols = lossGrad[0].length;     // 列数不变

        double[][] unpooled = new double[unpooledRows][unpooledCols];

        for (int i = 0; i < batch_size; i++) {
            for (int j = 0; j < lossGrad[i].length; j++) {
                double gradValue = lossGrad[i][j] / poolSize;  // 反向传播均分梯度
                for (int k = 0; k < poolSize; k++) {
                    unpooled[i * poolSize + k][j] = gradValue; // 只扩展行数
                }
            }
        }

        return unpooled;
    }


    // 计算均值池化，适用于 Transformer 输出降维
    public static double[][] meanPooling(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[] pooled = new double[cols];

        for (int j = 0; j < cols; j++) {
            double sum = 0.0;
            for (int i = 0; i < rows; i++) {
                sum += matrix[i][j];
            }
            pooled[j] = sum / rows; // 取平均值
        }
        return new double[][] { pooled }; // 返回 (1, d_model)
    }

    public static double[][] inverse(double[][] A) {
        int n = A.length;
        double[][] I = new double[n][n]; // 單位矩陣
        double[][] copy = new double[n][n]; // A 的副本

        // 初始化副本和單位矩陣
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                copy[i][j] = A[i][j];
                I[i][j] = (i == j) ? 1 : 0;
            }
        }

        // 高斯-約當法
        for (int i = 0; i < n; i++) {
            // 尋找主對角線上的最大值
            double max = Math.abs(copy[i][i]);
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(copy[k][i]) > max) {
                    max = Math.abs(copy[k][i]);
                    maxRow = k;
                }
            }

            // 交換行
            double[] temp = copy[i];
            copy[i] = copy[maxRow];
            copy[maxRow] = temp;

            temp = I[i];
            I[i] = I[maxRow];
            I[maxRow] = temp;

            // 標準化主對角線元素為 1
            double diag = copy[i][i];
            if (diag == 0) throw new ArithmeticException("矩陣不可逆");
            for (int j = 0; j < n; j++) {
                copy[i][j] /= diag;
                I[i][j] /= diag;
            }

            // 消去其他行的對應列元素
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = copy[k][i];
                    for (int j = 0; j < n; j++) {
                        copy[k][j] -= factor * copy[i][j];
                        I[k][j] -= factor * I[i][j];
                    }
                }
            }
        }
        return I;
    }
}
