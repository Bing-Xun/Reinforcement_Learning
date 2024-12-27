package group;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final double CONVERGENCE_THRESHOLD = 1e-4;

    public static void main(String[] args) {
        // 示例数据
        double[][] data = generateRandomDataPoints(20);

        // 使用肘部法则选择簇数
        int numClusters = ClusteringUtils.determineOptimalClusters(data, 30);
        System.out.println("numClusters:"+numClusters);

        GMM gmm = new GMM();
        List<List<double[]>> list = gmm.runGMM(data, numClusters);

        try {
            // 使用 Jackson 将 List<List<double[]>> 转换为 JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(list);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        graph(data, list);
    }

    public List<List<double[]>> runGMM(double[][] data, int numClusters) {
        int numSamples = data.length;
        int numFeatures = data[0].length;

        // 1. 数据标准化
        standardizeData(data);

        // 2. 初始化簇中心、协方差矩阵和权重
        double[][] centroids = initializeCentroids(data, numClusters);
        double[][][] covariances = initializeCovariances(numClusters, numFeatures);
        double[] weights = initializeWeights(numClusters);

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

    private static double[][] initializeCentroids(double[][] data, int numClusters) {
        Random random = new Random();
        double[][] centroids = new double[numClusters][data[0].length];

        // 使用 K-means++ 初始化质心
        centroids[0] = data[random.nextInt(data.length)];  // 随机选择第一个质心
        for (int k = 1; k < numClusters; k++) {
            double[] newCentroid = null;
            double maxDist = Double.MIN_VALUE;
            for (double[] point : data) {
                double minDist = Double.MAX_VALUE;
                // 计算每个点与现有质心的距离
                for (int i = 0; i < k; i++) {
                    double dist = euclideanDistance(point, centroids[i]);
                    minDist = Math.min(minDist, dist);
                }
                // 选择与现有质心距离最远的点作为新的质心
                if (minDist > maxDist) {
                    maxDist = minDist;
                    newCentroid = point;
                }
            }
            centroids[k] = newCentroid;
        }
        return centroids;
    }

    // 计算两点之间的欧几里得距离
    private static double euclideanDistance(double[] point1, double[] point2) {
        double sum = 0.0;
        for (int i = 0; i < point1.length; i++) {
            sum += Math.pow(point1[i] - point2[i], 2);
        }
        return Math.sqrt(sum);
    }

    private double[][][] initializeCovariances(int numClusters, int numFeatures) {
        double[][][] covariances = new double[numClusters][numFeatures][numFeatures];
        for (int k = 0; k < numClusters; k++) {
            for (int i = 0; i < numFeatures; i++) {
                covariances[k][i][i] = 1.0;
            }
        }
        return covariances;
    }

    private double[] initializeWeights(int numClusters) {
        double[] weights = new double[numClusters];
        Arrays.fill(weights, 1.0 / numClusters);
        return weights;
    }

    private double gaussian(double[] x, double[] mean, double[][] covariance) {
        int n = x.length;
        double det = determinant(covariance);
        double[] diff = subtract(x, mean);
        double[][] invCov = invertMatrix(covariance);
        double exponent = -0.5 * dotProduct(diff, matrixVectorProduct(invCov, diff));
        return Math.exp(exponent) / Math.sqrt(Math.pow(2 * Math.PI, n) * det);
    }

    private static boolean hasConverged(double[][] responsibilities, double[][] centroids, double[][] data) {
        double threshold = 1e-4;
        double totalChange = 0;
        for (int i = 0; i < centroids.length; i++) {
            totalChange += euclideanDistance(centroids[i], data[i]); // 使用质心和数据点的距离来判断变化
        }
        return totalChange < threshold;  // 如果变化小于阈值，则认为收敛
    }


    private int findMaxIndex(double[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private double[] subtract(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    private double[][] invertMatrix(double[][] matrix) {
        // 矩阵求逆（略）
        return matrix;
    }

    private double dotProduct(double[] a, double[] b) {
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        return result;
    }

    private double[] matrixVectorProduct(double[][] matrix, double[] vector) {
        double[] result = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < vector.length; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }

    private double determinant(double[][] matrix) {
        // 简单的行列式计算（略）
        return 1.0;
    }

    private static int determineOptimalClusters(double[][] data) {
        // 使用肘部法则计算最优簇数（这里是示例，假设为3）
//        return 3;
        return ElbowMethod.determineOptimalClusters(data, 10);
    }

//    private static void graph(double[][] data) {
//        // 创建数据集
//        XYSeries series = new XYSeries("Data Points");
//        for (double[] point : data) {
//            series.add(point[0], point[1]);
//        }
//        XYSeriesCollection dataset = new XYSeriesCollection(series);
//
//        // 创建散点图
//        JFreeChart chart = ChartFactory.createScatterPlot(
//            "Scatter Plot", // 图表标题
//            "X-Axis",       // X轴标签
//            "Y-Axis",       // Y轴标签
//            dataset          // 数据集
//        );
//
//        // 显示图表
//        JFrame frame = new JFrame("Scatter Plot Example");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.add(new ChartPanel(chart));
//        frame.setSize(800, 600);
//        frame.setVisible(true);
//    }

    public static void graph(double[][] data, List<List<double[]>> clusters) {
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

