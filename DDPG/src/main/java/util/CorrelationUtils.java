package util;

public class CorrelationUtils {
    public static double[] pearsonCorrelation(double[][] input, double[][] outputPool) {
        int inputCols = input[0].length;
        int outputCols = outputPool[0].length;

        if (input.length != outputPool.length) {
            throw new IllegalArgumentException("Input and OutputPool must have the same batch size (rows)");
        }

        double[] correlations = new double[Math.min(inputCols, outputCols)];

        for (int i = 0; i < correlations.length; i++) {
            double[] inputCol = getColumn(input, i);
            double[] outputCol = getColumn(outputPool, i);
            correlations[i] = pearsonCorrelation(inputCol, outputCol);
        }

        return correlations;
    }

    private static double pearsonCorrelation(double[] x, double[] y) {
        int n = x.length;
        if (n != y.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        double meanX = mean(x);
        double meanY = mean(y);
        double sumXY = 0, sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            sumXY += dx * dy;
            sumX2 += dx * dx;
            sumY2 += dy * dy;
        }

        return sumXY / (Math.sqrt(sumX2) * Math.sqrt(sumY2));
    }

    private static double mean(double[] arr) {
        double sum = 0;
        for (double val : arr) {
            sum += val;
        }
        return sum / arr.length;
    }

    private static double[] getColumn(double[][] matrix, int colIndex) {
        double[] col = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            col[i] = matrix[i][colIndex % matrix[i].length]; // 防止索引溢出
        }
        return col;
    }
}
