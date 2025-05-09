package transformer;

import java.util.Random;
import java.util.concurrent.*;

public class MatrixUtils {

    private static final Random random = new Random();

    // 矩阵乘法
    public static double[][] matMul(double[][] A, double[][] B) {
        int rowsA = A.length, colsA = A[0].length;
        int rowsB = B.length, colsB = B[0].length;
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
    public static double[][] poolProject(double[][] projected, int poolSizeRow, int poolSizeCol) {
        double[][] pooled = meanPooling(projected, poolSizeRow, poolSizeCol); // 然后进行池化操作（例如均值池化）
        return softmax(pooled); // 最后进行 softmax 归一化
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
    /**
     *  這個要用除的看比較清楚
     * 池化大小 poolSizeRow=2, poolSizeCol=1，表示 每 2 行求平均
     * double[][] a = {
     *     {1, 2, 3},
     *     {4, 5, 6},
     *     {7, 8, 9},
     *     {10, 11, 12}
     * };
     * double[][] result = meanPooling(a, 2, 1);
     * 结果: {{2.5, 3.5, 4.5}, {8.5, 9.5, 10.5}}
     *
     *
     * 池化大小 poolSizeRow=1, poolSizeCol=2，表示 每 2 列求平均
     * double[][] a = {
     *     {1, 2, 3, 4},
     *     {5, 6, 7, 8}
     * };
     * double[][] result = meanPooling(a, 1, 2);
     * // 结果: {{1.5, 3.5}, {5.5, 7.5}}
     *
     *
     * 池化大小 poolSizeRow=2, poolSizeCol=2，表示 每 2×2 塊求平均：
     * double[][] a = {
     *     {1, 2, 3, 4},
     *     {5, 6, 7, 8},
     *     {9, 10, 11, 12},
     *     {13, 14, 15, 16}
     * };
     * double[][] result = meanPooling(a, 2, 2);
     * // 结果: {{3.5, 5.5}, {11.5, 13.5}}
     *
     * @param a
     * @param poolSizeRow
     * @param poolSizeCol
     * @return
     */
    public static double[][] meanPooling(double[][] a, int poolSizeRow, int poolSizeCol) {
        int rows = a.length;
        int cols = a[0].length;

        // 計算池化後的新維度
        int newRows = rows / poolSizeRow;
        int newCols = cols / poolSizeCol;

        double[][] pooled = new double[newRows][newCols];

        for (int i = 0; i < newRows; i++) {
            for (int j = 0; j < newCols; j++) {
                double sum = 0.0;

                // 池化區塊內累加數值
                for (int x = 0; x < poolSizeRow; x++) {
                    for (int y = 0; y < poolSizeCol; y++) {
                        sum += a[i * poolSizeRow + x][j * poolSizeCol + y];
                    }
                }

                // 計算區塊內的平均值
                pooled[i][j] = sum / (poolSizeRow * poolSizeCol);
            }
        }
        return pooled;
    }
}
