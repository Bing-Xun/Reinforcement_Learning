package group.gmm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import group.ElbowMethod;
import group.gmm.vo.GMMVO;
import group.util.ClusteringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GMM {
    private static final int MAX_ITERATIONS = 100;
    private GMMVO gMMVO;

    private static double[][] getData() {
        return new double[][] {
            {1.85, 1.82}, {2.88, 1.46}, {1.2, 1.39}, {2.35, 2.37}, {2.58, 1.05},
            {2.21, 1.69}, {2.32, 2.28}, {1.64, 2.08}, {2.36, 1.38}, {1.82, 2.01},
            {1.15, 2.62}, {1.81, 1.76}, {1.35, 2.25}, {1.32, 1.63}, {1.12, 2.61},
            {1.92, 2.21}, {1.08, 1.51}, {1.34, 2.2}, {2.33, 1.5}, {2.62, 3.02},
            {1.85, 1.83}, {1.55, 2.14}, {3.22, 2.23}, {1.7, 1.93}, {1.53, 2.25},
            {1.7, 2.56}, {2.19, 1.22}, {2.06, 1.83}, {2.77, 2.28}, {0.97, 1.7},
            {1.77, 1.58}, {1.65, 2.33}, {2.29, 2.35}, {-5.17, -4.88}, {-4.68, -5.85},
            {-4.64, -4.93}, {-4.7, -4.72}, {-5.22, -3.99}, {-5.13, -5.22}, {-4.26, -4.84},
            {-5.28, -4.37}, {-4.77, -5.78}, {-4.39, -5.02}, {-4.98, -4.84}, {-5.16, -4.44},
            {-3.84, -5.26}, {-5.42, -5.92}, {-4.8, -5.22}, {-5.44, -5.72}, {-3.94, -4.51},
            {-5.2, -5.31}, {-4.83, -5.47}, {-3.97, -4.79}, {-4.67, -5.22}, {-5.38, -4.3},
            {-5.53, -4.39}, {-4.98, -5.02}, {-5.39, -5.47}, {-5.55, -4.99}, {-4.8, -4.62},
            {-3.59, -5.08}, {-5.69, -5.87}, {-4.67, -5.34}, {-5.93, -5.42}, {-3.84, -4.29},
            {-4.47, -4.87}, {7.39, -3.08}, {6.51, -2.9}, {6.82, -3.2}, {7.0, -3.37},
            {7.43, -2.84}, {7.49, -3.6}, {6.81, -1.64}, {7.27, -2.54}, {7.47, -3.2},
            {5.95, -3.93}, {7.14, -2.13}, {6.98, -3.49}, {7.58, -2.64}, {6.58, -2.98},
            {6.05, -3.41}, {7.06, -2.36}, {6.54, -3.21}, {7.68, -3.86}, {6.79, -3.26},
            {5.67, -3.18}, {7.1, -3.06}, {6.58, -3.39}, {7.55, -3.45}, {7.94, -1.77},
            {7.15, -2.88}, {6.9, -2.71}, {7.64, -2.93}, {6.46, -2.36}, {7.55, -2.84},
            {6.96, -3.14}, {7.04, -2.86}, {6.75, -1.42}, {7.89, -2.52}, {6.32, -3.94}
        };
    }

    public static void main(String[] args) {
        // 示例数据
//        double[][] data = generateRandomDataPoints(20);
        double[][] data = getData();

        // 使用肘部法则选择簇数
        int numClusters = ClusteringUtils.determineOptimalClusters(data, 30);
        System.out.println("numClusters:"+numClusters);

        GMM gmm = new GMM();
        GMMVO vo = gmm.train(data, numClusters);

        double d = predictCluster(new double[]{-4.8, -5.22}, vo);
        System.out.println("d:"+d);


        List<List<double[]>> list = getGrraphList(data, numClusters, vo);
        try {
            // 使用 Jackson 将 List<List<double[]>> 转换为 JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(list);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        graph(list);
    }

    public static List<List<double[]>> getGrraphList(double[][] data, int numClusters, GMMVO vo) {
        int numSamples = data.length;
        double[][] responsibilities = vo.getResponsibilities();

        // 创建一个 List 用于存储每个簇的列表
        List<List<double[]>> clusters = new ArrayList<>();
        for (int i = 0; i < numClusters; i++) {
            clusters.add(new ArrayList<>());  // 每个簇初始化为一个空的 List
        }

        // 根据每个数据点的责任（responsibility）分配它们到对应的簇
        for (int i = 0; i < numSamples; i++) {
            int cluster = findMaxIndex(responsibilities[i]);  // 找到每个数据点所属的簇
            clusters.get(cluster).add(data[i]);  // 将数据点添加到相应的簇中
        }

        return clusters;
    }

    public GMMVO train(double[][] data, int numClusters) {
        int numSamples = data.length;
        int numFeatures = data[0].length;

        // 1. 数据标准化
        standardizeData(data);

        // 2. 初始化簇中心、协方差矩阵和权重
        GMMVO gMMVO = new GMMVO();
        gMMVO.init(data, numClusters);
        double[][] centroids = gMMVO.getCentroids();
        double[][][] covariances = gMMVO.getCovariances();
        double[] weights = gMMVO.getWeights();

        double[][] responsibilities = new double[numSamples][numClusters];

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // 3. E 步骤：计算责任度
            for (int i = 0; i < numSamples; i++) {
                double totalProbability = 0;
                for (int k = 0; k < numClusters; k++) {
                    responsibilities[i][k] = weights[k] * gaussian(data[i], centroids[k], covariances[k]);
                    totalProbability += responsibilities[i][k];
                }
                for (int k = 0; k < numClusters; k++) {
                    responsibilities[i][k] /= totalProbability;  // 归一化责任度
                }
            }

            // 4. M 步骤：更新参数
            for (int k = 0; k < numClusters; k++) {
                double Nk = 0;
                double[] newCentroid = new double[numFeatures];
                double[][] newCovariance = new double[numFeatures][numFeatures];

                for (int i = 0; i < numSamples; i++) {
                    Nk += responsibilities[i][k];
                    for (int j = 0; j < numFeatures; j++) {
                        newCentroid[j] += responsibilities[i][k] * data[i][j];
                    }
                }
                // 更新质心
                for (int j = 0; j < numFeatures; j++) {
                    newCentroid[j] /= Nk;
                }
                centroids[k] = newCentroid;

                // 更新协方差矩阵
                for (int i = 0; i < numSamples; i++) {
                    double[] diff = subtract(data[i], centroids[k]);
                    for (int j = 0; j < numFeatures; j++) {
                        for (int l = 0; l < numFeatures; l++) {
                            newCovariance[j][l] += responsibilities[i][k] * diff[j] * diff[l];
                        }
                    }
                }
                // 均值化协方差
                for (int j = 0; j < numFeatures; j++) {
                    for (int l = 0; l < numFeatures; l++) {
                        newCovariance[j][l] /= Nk;
                    }
                }
                covariances[k] = newCovariance;
                weights[k] = Nk / numSamples;
            }

            // 检查收敛性
            if (hasConverged(responsibilities, centroids, data)) {
                break;
            }
        }

        gMMVO.setCentroids(centroids);
        gMMVO.setCovariances(covariances);
        gMMVO.setWeights(weights);
        gMMVO.setResponsibilities(responsibilities);

        return gMMVO;
    }

    public static int predictCluster(double[] x, GMMVO vo) {
        int numClusters = vo.getCentroids().length;
        double[] responsibilities = new double[numClusters];
        double totalProbability = 0;

        // 计算每个簇的责任值
        for (int k = 0; k < numClusters; k++) {
            responsibilities[k] = vo.getWeights()[k] * gaussian(x, vo.getCentroids()[k], vo.getCovariances()[k]);
            totalProbability += responsibilities[k];
        }

        // 归一化责任值
        for (int k = 0; k < numClusters; k++) {
            responsibilities[k] /= totalProbability;
        }

        // 找到最大责任值对应的簇
        int maxCluster = 0;
        double maxResponsibility = responsibilities[0];
        for (int k = 1; k < numClusters; k++) {
            if (responsibilities[k] > maxResponsibility) {
                maxResponsibility = responsibilities[k];
                maxCluster = k;
            }
        }

        return maxCluster;
    }

    private void standardizeData(double[][] data) {
        int numFeatures = data[0].length;
        for (int j = 0; j < numFeatures; j++) {
            double mean = 0, std = 0;
            for (double[] datum : data) {
                mean += datum[j];
            }
            mean /= data.length;

            for (double[] datum : data) {
                std += Math.pow(datum[j] - mean, 2);
            }
            std = Math.sqrt(std / data.length);

            for (double[] datum : data) {
                datum[j] = (datum[j] - mean) / std;
            }
        }
    }



    private static double gaussian(double[] x, double[] mean, double[][] covariance) {
        int n = x.length;
        double det = determinant(covariance);
        double[] diff = subtract(x, mean);
        double[][] invCov = invertMatrix(covariance);
        double exponent = -0.5 * dotProduct(diff, matrixVectorProduct(invCov, diff));
        return Math.exp(exponent) / Math.sqrt(Math.pow(2 * Math.PI, n) * det);
    }

    private double calculateNewCentroid(int featureIndex, double[][] responsibilities, double[][] data, int clusterIndex) {
        double numerator = 0.0;
        double denominator = 0.0;

        for (int i = 0; i < data.length; i++) {
            numerator += responsibilities[i][clusterIndex] * data[i][featureIndex];
            denominator += responsibilities[i][clusterIndex];
        }

        // 防止分母为零
        if (denominator == 0.0) {
            throw new IllegalStateException("Responsibility sum is zero for cluster: " + clusterIndex);
        }

        return numerator / denominator;
    }


    private boolean hasConverged(double[][] responsibilities, double[][] centroids, double[][] data) {
        double tolerance = 1e-4;
        double maxChange = 0.0;

        for (int i = 0; i < centroids.length; i++) {
            for (int j = 0; j < centroids[i].length; j++) {
                double change = Math.abs(centroids[i][j] - calculateNewCentroid(j, responsibilities, data, i));
                maxChange = Math.max(maxChange, change);
            }
        }

        return maxChange < tolerance;
    }

    private static int findMaxIndex(double[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private static double[] subtract(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    private static double[][] invertMatrix(double[][] matrix) {
        int n = matrix.length;

        if (n != matrix[0].length) {
            throw new IllegalArgumentException("Matrix must be square.");
        }

        // 初始化单位矩阵
        double[][] identity = new double[n][n];
        for (int i = 0; i < n; i++) {
            identity[i][i] = 1.0;
        }

        // 拷贝原矩阵（避免修改原矩阵）
        double[][] copy = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, n);
        }

        // 高斯-约当消元法
        for (int i = 0; i < n; i++) {
            // 寻找主元（最大值）
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(copy[k][i]) > Math.abs(copy[maxRow][i])) {
                    maxRow = k;
                }
            }

            // 交换行
            double[] temp = copy[i];
            copy[i] = copy[maxRow];
            copy[maxRow] = temp;

            temp = identity[i];
            identity[i] = identity[maxRow];
            identity[maxRow] = temp;

            // 检查主元是否为零
            if (Math.abs(copy[i][i]) < 1e-10) {
                throw new IllegalArgumentException("Matrix is singular or nearly singular.");
            }

            // 归一化主元行
            double diagValue = copy[i][i];
            for (int j = 0; j < n; j++) {
                copy[i][j] /= diagValue;
                identity[i][j] /= diagValue;
            }

            // 消去其他行的该列
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = copy[k][i];
                    for (int j = 0; j < n; j++) {
                        copy[k][j] -= factor * copy[i][j];
                        identity[k][j] -= factor * identity[i][j];
                    }
                }
            }
        }

        return identity; // 返回逆矩阵
    }


    private static double dotProduct(double[] a, double[] b) {
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        return result;
    }

    private static double[] matrixVectorProduct(double[][] matrix, double[] vector) {
        double[] result = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < vector.length; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }

    // 计算矩阵的行列式
    public static double determinant(double[][] matrix) {
        int n = matrix.length;

        // 如果是 1x1 矩阵，直接返回唯一元素
        if (n == 1) {
            return matrix[0][0];
        }

        // 如果是 2x2 矩阵，使用快速公式
        if (n == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        }

        // 对于 n > 2，递归展开
        double det = 0.0;
        for (int col = 0; col < n; col++) {
            // 递归计算子矩阵的行列式
            det += Math.pow(-1, col) * matrix[0][col] * determinant(minor(matrix, 0, col));
        }
        return det;
    }

    // 生成子矩阵（去掉指定行和列后的矩阵）
    private static double[][] minor(double[][] matrix, int row, int col) {
        int n = matrix.length;
        double[][] minor = new double[n - 1][n - 1];

        for (int i = 0, mi = 0; i < n; i++) {
            if (i == row) continue; // 跳过指定行

            for (int j = 0, mj = 0; j < n; j++) {
                if (j == col) continue; // 跳过指定列

                minor[mi][mj] = matrix[i][j];
                mj++;
            }
            mi++;
        }
        return minor;
    }

    private static int determineOptimalClusters(double[][] data) {
        return ElbowMethod.determineOptimalClusters(data, 10);
    }

    public static void graph(List<List<double[]>> clusters) {
        // 创建数据集
        XYSeriesCollection dataset = new XYSeriesCollection();

        // 为每个簇创建一个 XYSeries 并设置不同的颜色
        for (int i = 0; i < clusters.size(); i++) {
            XYSeries series = new XYSeries("Cluster " + (i + 1));

            // 设置不同的颜色
            Color clusterColor = getColorForCluster(i);

            // 向 series 中添加数据点
            for (double[] point : clusters.get(i)) {
                series.add(point[0], point[1]);
            }

            dataset.addSeries(series);
        }

        // 创建散点图
        JFreeChart chart = ChartFactory.createScatterPlot(
            "Scatter Plot",        // 图表标题
            "X-Axis",              // X轴标签
            "Y-Axis",              // Y轴标签
            dataset,               // 数据集
            PlotOrientation.VERTICAL, // 图表方向
            true,                  // 显示图例
            true,                  // 生成提示工具
            false                  // 生成URLs
        );

        // 修改图表的外观，设置颜色
        XYPlot plot = chart.getXYPlot();
        plot.setDomainPannable(true);  // 允许在X轴上平移
        plot.setRangePannable(true);   // 允许在Y轴上平移

        // 显示图表
        JFrame frame = new JFrame("Scatter Plot Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    // 为每个簇设置一个不同的颜色
    private static Color getColorForCluster(int clusterIndex) {
        // 示例: 为不同的簇设置不同的颜色
        switch (clusterIndex) {
            case 0: return Color.RED;
            case 1: return Color.BLUE;
            case 2: return Color.GREEN;
            case 3: return Color.ORANGE;
            case 4: return Color.MAGENTA;
            default: return Color.CYAN;
        }
    }


    private static double[][] generateRandomDataPoints(int n) {
        Random random = new Random();
        double[][] dataPoints = new double[n][2];
        for (int i = 0; i < n; i++) {
            dataPoints[i][0] = random.nextDouble() * 10; // X范围 [0, 10)
            dataPoints[i][1] = random.nextDouble() * 10; // Y范围 [0, 10)
        }
        return dataPoints;
    }
}

