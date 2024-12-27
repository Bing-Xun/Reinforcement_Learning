package group.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClusteringUtils {

    // 实现 K-means 算法
    public static List<List<double[]>> performClustering(double[][] data, int numClusters) {
        int numPoints = data.length;
        int dimensions = data[0].length;

        // 初始化质心
        double[][] centroids = new double[numClusters][dimensions];
        Random random = new Random();
        for (int i = 0; i < numClusters; i++) {
            centroids[i] = data[random.nextInt(numPoints)];
        }

        List<List<double[]>> clusters = null;
        boolean centroidsChanged;

        int maxIterations = 1000;  // 设置最大迭代次数
        int iterations = 0;

        do {
            // 初始化每个簇
            clusters = new ArrayList<>();
            for (int i = 0; i < numClusters; i++) {
                clusters.add(new ArrayList<>());
            }

            // 分配数据点到最近的质心
            for (double[] point : data) {
                int nearestCluster = findNearestCluster(point, centroids);
                clusters.get(nearestCluster).add(point);
            }

            // 重新计算质心
            centroidsChanged = false;
            for (int i = 0; i < numClusters; i++) {
                // 检查簇是否为空，如果为空，跳过计算或重新选择一个质心
                if (clusters.get(i).size() > 0) {
                    double[] newCentroid = computeCentroid(clusters.get(i), dimensions);
                    if (!isCentroidEqual(centroids[i], newCentroid)) {
                        centroids[i] = newCentroid;
                        centroidsChanged = true;
                    }
                } else {
                    // 如果簇为空，可以随机选择一个新的质心
                    centroids[i] = data[random.nextInt(numPoints)];
                    centroidsChanged = true;
                }
            }

            iterations++;
            if (iterations >= maxIterations) {
//                System.out.println("Maximum iterations reached!");
                break;
            }

        } while (centroidsChanged);

        return clusters;
    }


    // 计算误差平方和（SSE）
    public static double computeSSE(List<List<double[]>> clusters) {
        double sse = 0.0;

        for (List<double[]> cluster : clusters) {
            // 确保簇非空
            if (cluster.size() > 0) {
                double[] centroid = computeCentroid(cluster, cluster.get(0).length);
                for (double[] point : cluster) {
                    for (int i = 0; i < point.length; i++) {
                        sse += Math.pow(point[i] - centroid[i], 2);
                    }
                }
            }
        }

        return sse;
    }


    // 找到肘部点
    public static int findElbowPoint(List<Double> sseList) {
        int n = sseList.size();
        double maxDistance = 0.0;
        int elbowPoint = 1;

        double x1 = 1, y1 = sseList.get(0);
        double x2 = n, y2 = sseList.get(n - 1);

        for (int k = 1; k <= n; k++) {
            double x0 = k, y0 = sseList.get(k - 1);
            double distance = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1) /
                Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));

            if (distance > maxDistance) {
                maxDistance = distance;
                elbowPoint = k;
            }
        }

        return elbowPoint;
    }

    // 工具方法：找到最近的质心
    private static int findNearestCluster(double[] point, double[][] centroids) {
        int nearestCluster = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < centroids.length; i++) {
            double distance = 0.0;
            for (int j = 0; j < point.length; j++) {
                distance += Math.pow(point[j] - centroids[i][j], 2);
            }
            if (distance < minDistance) {
                minDistance = distance;
                nearestCluster = i;
            }
        }

        return nearestCluster;
    }

    // 工具方法：计算簇的质心
    private static double[] computeCentroid(List<double[]> cluster, int dimensions) {
        double[] centroid = new double[dimensions];
        if (cluster.isEmpty()) return centroid;

        for (double[] point : cluster) {
            for (int i = 0; i < dimensions; i++) {
                centroid[i] += point[i];
            }
        }

        for (int i = 0; i < dimensions; i++) {
            centroid[i] /= cluster.size();
        }

        return centroid;
    }

    // 工具方法：检查两个质心是否相同
    private static boolean isCentroidEqual(double[] centroid1, double[] centroid2) {
        for (int i = 0; i < centroid1.length; i++) {
            if (centroid1[i] != centroid2[i]) return false;
        }
        return true;
    }

    // 主方法：根据肘部法则计算最优簇数
    public static int determineOptimalClusters(double[][] data, int maxClusters) {
        List<Double> sseList = new ArrayList<>();

        for (int k = 1; k <= maxClusters; k++) {
            List<List<double[]>> clusters = performClustering(data, k);
            double sse = computeSSE(clusters);
            sseList.add(sse);
        }

        return findElbowPoint(sseList);
    }

    // 测试
    public static void main(String[] args) {
        // 示例数据
        double[][] data = generateRandomDataPoints(100);

        int optimalClusters = determineOptimalClusters(data, 10);
        System.out.println("Optimal number of clusters: " + optimalClusters);
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
