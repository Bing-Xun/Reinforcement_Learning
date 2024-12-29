package group.gmm.vo;


import java.util.Arrays;
import java.util.Random;

public class GMMVO {
    private double[][] centroids;
    private double[][][] covariances;
    private double[] weights;
    private double[][] responsibilities;

    public void init(double[][] data, int numClusters) {
        int numFeatures = data[0].length;

        centroids = initializeCentroids(data, numClusters);
        covariances = initializeCovariances(numClusters, numFeatures);
        weights = initializeWeights(numClusters);
    }

    private double[][] initializeCentroids(double[][] data, int numClusters) {
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
    private double euclideanDistance(double[] point1, double[] point2) {
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

    public double[][] getCentroids() {
        return centroids;
    }

    public void setCentroids(double[][] centroids) {
        this.centroids = centroids;
    }

    public double[][][] getCovariances() {
        return covariances;
    }

    public void setCovariances(double[][][] covariances) {
        this.covariances = covariances;
    }

    public double[] getWeights() {
        return weights;
    }

    public void setWeights(double[] weights) {
        this.weights = weights;
    }

    public double[][] getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(double[][] responsibilities) {
        this.responsibilities = responsibilities;
    }
}
