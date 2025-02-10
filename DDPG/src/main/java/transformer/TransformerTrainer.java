package transformer;

public class TransformerTrainer {
    private final TransformerEncoder encoder;
    private final double learningRate;

    public TransformerTrainer(TransformerEncoder encoder, double learningRate) {
        this.encoder = encoder;
        this.learningRate = learningRate;
    }

    public void train(double[][] input, double[][] target, int epochs) {
        for (int epoch = 0; epoch < epochs; epoch++) {
            // 前向传播
            double[][] output = encoder.forward(input);

            // 确保维度匹配
            if (output.length != target.length || output[0].length != target[0].length) {
                throw new IllegalArgumentException("Mismatch: output = " +
                    output.length + "x" + output[0].length + ", target = " +
                    target.length + "x" + target[0].length);
            }

            // 计算损失
            double loss = MatrixUtils.meanSquaredError(output, target);

            // 计算梯度
            double[][] lossGrad = MatrixUtils.subtractMatrix(output, target);

            // 确保梯度维度匹配
//            System.out.println("lossGrad shape: " + lossGrad.length + "x" + lossGrad[0].length);

            // 计算梯度
            double[][] gradW2 = MatrixUtils.matMul(MatrixUtils.transposeMatrix(encoder.W1), lossGrad);
            double[][] gradW1 = MatrixUtils.matMul(MatrixUtils.transposeMatrix(input), MatrixUtils.matMul(lossGrad, MatrixUtils.transposeMatrix(encoder.W2)));

            // 梯度裁剪（防止梯度爆炸）
            double maxNorm = 1.0;
            gradW1 = MatrixUtils.clipGradients(gradW1, maxNorm);
            gradW2 = MatrixUtils.clipGradients(gradW2, maxNorm);

            // 确保 W1, W2, gradW1, gradW2 形状匹配
            if (gradW1.length != encoder.W1.length || gradW1[0].length != encoder.W1[0].length) {
                throw new IllegalArgumentException("gradW1 mismatch: " +
                    gradW1.length + "x" + gradW1[0].length + " vs W1: " +
                    encoder.W1.length + "x" + encoder.W1[0].length);
            }

            if (gradW2.length != encoder.W2.length || gradW2[0].length != encoder.W2[0].length) {
                throw new IllegalArgumentException("gradW2 mismatch: " +
                    gradW2.length + "x" + gradW2[0].length + " vs W2: " +
                    encoder.W2.length + "x" + encoder.W2[0].length);
            }

            // 更新参数
            encoder.W2 = MatrixUtils.subtractMatrix(encoder.W2, MatrixUtils.scaleMatrix(gradW2, learningRate));
            encoder.W1 = MatrixUtils.subtractMatrix(encoder.W1, MatrixUtils.scaleMatrix(gradW1, learningRate));

            System.out.println("Epoch " + (epoch + 1) + ": Loss = " + loss);
        }
    }

    public static void main(String[] args) {
        TransformerEncoder encoder = new TransformerEncoder(4, 8);
        TransformerTrainer trainer = new TransformerTrainer(encoder, 0.01);

        double[][] input = {
            {1.0, 2.0, 3.0, 4.0},
            {5.0, 6.0, 7.0, 8.0},
            {9.0, 10.0, 11.0, 12.0}
        };

        double[][] target = {
            {0.5, 1.5, 2.5, 3.5},
            {4.5, 5.5, 6.5, 7.5},
            {8.5, 9.5, 10.5, 11.5}
        };

        trainer.train(input, target, 100);
    }
}
