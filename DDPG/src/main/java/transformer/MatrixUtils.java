package transformer;

import java.util.Random;
import java.util.concurrent.*;

public class MatrixUtils {

    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final Random random = new Random();

    public static double[][] matMul(double[][] A, double[][] B) {
        int m = A.length, n = B[0].length, p = B.length;
        double[][] result = new double[m][n];

        Future<?>[] futures = new Future[m];
        for (int i = 0; i < m; i++) {
            final int row = i;
            futures[i] = executor.submit(() -> {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < p; k++) {
                        result[row][j] += A[row][k] * B[k][j];
                    }
                }
            });
        }
        waitForCompletion(futures);
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
}
