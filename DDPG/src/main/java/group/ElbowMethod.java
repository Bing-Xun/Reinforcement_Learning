package group;

import group.util.ClusteringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ElbowMethod {
    // K-means 聚类算法实现
    private static class KMeans {
        private int numClusters;
        private double[][] centroids;

        public KMeans(int numClusters) {
            this.numClusters = numClusters;
        }

        // K-means 聚类
        public double fit(double[][] data) {
            int numSamples = data.length;
            int numFeatures = data[0].length;

            centroids = initializeCentroids(data);
            double prevError = Double.MAX_VALUE;
            double error = 0.0;

            boolean converged = false;
            int[] assignments = new int[numSamples];

            while (!converged) {
                // 步骤 1: 分配每个数据点到最近的簇
                for (int i = 0; i < numSamples; i++) {
                    assignments[i] = getClosestCentroid(data[i]);
                }

                // 步骤 2: 更新簇中心
                updateCentroids(data, assignments);

                // 步骤 3: 计算误差（失真度）
                error = calculateError(data, assignments);

                // 如果误差变化很小，则认为算法收敛
                if (Math.abs(prevError - error) < 1e-6) {
                    converged = true;
                }
                prevError = error;
            }
            return error;
        }

        // 随机初始化簇中心
        private double[][] initializeCentroids(double[][] data) {
            Random rand = new Random();
            double[][] centroids = new double[numClusters][data[0].length];
            for (int k = 0; k < numClusters; k++) {
                int randomIndex = rand.nextInt(data.length);
                centroids[k] = data[randomIndex];
            }
            return centroids;
        }

        // 计算每个点到所有簇中心的距离，返回最近的簇
        private int getClosestCentroid(double[] dataPoint) {
            double minDistance = Double.MAX_VALUE;
            int closestCentroid = -1;

            for (int k = 0; k < numClusters; k++) {
                double distance = calculateDistance(dataPoint, centroids[k]);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCentroid = k;
                }
            }
            return closestCentroid;
        }

        // 计算两个点之间的欧氏距离
        private double calculateDistance(double[] point1, double[] point2) {
            double sum = 0;
            for (int i = 0; i < point1.length; i++) {
                sum += Math.pow(point1[i] - point2[i], 2);
            }
            return Math.sqrt(sum);
        }

        // 更新簇中心
        private void updateCentroids(double[][] data, int[] assignments) {
            double[][] newCentroids = new double[numClusters][data[0].length];
            int[] counts = new int[numClusters];

            for (int i = 0; i < data.length; i++) {
                int cluster = assignments[i];
                for (int j = 0; j < data[i].length; j++) {
                    newCentroids[cluster][j] += data[i][j];
                }
                counts[cluster]++;
            }

            // 计算新的中心点
            for (int k = 0; k < numClusters; k++) {
                for (int j = 0; j < newCentroids[k].length; j++) {
                    newCentroids[k][j] /= counts[k];
                }
            }
            centroids = newCentroids;
        }

        // 计算失真度（每个数据点到其最近簇中心的距离的平方和）
        private double calculateError(double[][] data, int[] assignments) {
            double error = 0;
            for (int i = 0; i < data.length; i++) {
                int cluster = assignments[i];
                error += calculateDistance(data[i], centroids[cluster]);
            }
            return error;
        }
    }

    // 使用肘部法则来选择最优的簇数
    public static int determineOptimalClusters(double[][] data, int maxClusters) {
        List<Double> errors = new ArrayList<>();

        for (int k = 1; k <= maxClusters; k++) {
            KMeans kMeans = new KMeans(k);
            double error = kMeans.fit(data);
            errors.add(error);
//            System.out.println("Error for " + k + " clusters: " + error);
        }

        // 寻找肘部（失真度下降的拐点）
        int optimalClusters = 1;
        double minSlope = Double.MAX_VALUE;
        for (int i = 1; i < errors.size() - 1; i++) {
            double slope = (errors.get(i + 1) - errors.get(i - 1)) / 2;
            if (slope < minSlope) {
                minSlope = slope;
                optimalClusters = i + 1; // 簇数从1开始
            }
        }

        System.out.println("determineOptimalClusters:"+optimalClusters);
        return optimalClusters;
    }

//    public static int determineOptimalClusters(double[][] data, int maxClusters) {
//        List<Double> sseList = new ArrayList<>();
//        List<Double> dbiList = new ArrayList<>();
//        double minDBI = Double.MAX_VALUE;
//        int optimalClusterElbow = 1;
//        int optimalClusterDBI = 1;
//
//        for (int k = 1; k <= maxClusters; k++) {
//            List<double[][]> clusters = ClusteringUtils.performClustering(data, k);
//
//            // 计算 SSE（Elbow Method）
//            double sse = ClusteringUtils.computeSSE(clusters);
//            sseList.add(sse);
//
//            // 计算 DBI
//            if (k > 1) {
//                double dbi = DaviesBouldinIndex.daviesBouldinIndex(clusters);
//                dbiList.add(dbi);
//                if (dbi < minDBI) {
//                    minDBI = dbi;
//                    optimalClusterDBI = k;
//                }
//            }
//        }
//
//        // 找到 Elbow Method 的最佳点
//        optimalClusterElbow = ClusteringUtils.findElbowPoint(sseList);
//
//        // 综合评分，融合结果
//        return combineResults(optimalClusterElbow, optimalClusterDBI);
//    }

    // 融合结果（可以基于权重、优先级等逻辑）
    private static int combineResults(int elbow, int dbi) {
        // 简单示例：取两者平均值，向下取整
        return (int) Math.floor((elbow + dbi) / 2.0);
    }

}
