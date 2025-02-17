package transformer;

import binace.vo.QuoteVO;
import ddpg.trend.DDPGMain;
import ddpg.trend.price.VWAPDiff;
import ddpg.trend.voulme.VolumeAvgDiff;
import ddpg.trend.voulme.VolumeSideDiff;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TransformerTrainer {
    private final TransformerEncoder encoder;
    private final double learningRate;

    public TransformerTrainer(TransformerEncoder encoder, double learningRate) {
        this.encoder = encoder;
        this.learningRate = learningRate;
    }

//    public void train(double[][] input, double[][] target, int epochs) {
//        for (int epoch = 0; epoch < epochs; epoch++) {
//            // 前向传播
//            TransformerEncoder.TransformerForwardResult result = encoder.forward(input);
//            double[][] output = result.output;
////            double[][] attentionWeights = result.attentionWeights;
//            double[][] Q = result.Q;
//            double[][] K = result.K;
//            double[][] V = result.V;
//
//            // 投影到最终输出
//            double[][] projectedOutput = MatrixUtils.matMul(output, encoder.W_proj);
//            double[][] projectedOutputPool = MatrixUtils.poolProject(projectedOutput);
//
//            // 计算损失
//            double loss = MatrixUtils.meanSquaredError(projectedOutputPool, target);
//
//            // 计算投影层的梯度
//            double[][] lossGrad = MatrixUtils.subtractMatrix(projectedOutputPool, target); // (batch_size, 3), 1, 3
//            double[][] d = MatrixUtils.unpoolGrad(lossGrad,1 , 6); // 6, 3
//            double[][] gradW_proj = MatrixUtils.matMul(d, MatrixUtils.transposeMatrix(encoder.W_proj)); // 11, 3
//            double[][] gradW2 = MatrixUtils.matMul(
//                MatrixUtils.inverse(encoder.W1)
//                , MatrixUtils.matMul(
//                    MatrixUtils.inverse(encoder.Wv)
//                    , gradW_proj
//                )
//            );
//            double[][] gradW1 = MatrixUtils.matMul(
//                MatrixUtils.matMul(
//                    MatrixUtils.inverse(encoder.Wv)
//                    , gradW_proj
//                )
//                , MatrixUtils.inverse(encoder.W2)
//            );
//            double[][] gradWv = MatrixUtils.matMul(
//                MatrixUtils.matMul(gradW_proj, MatrixUtils.inverse(encoder.W2))
//                    , MatrixUtils.inverse(encoder.W1)
//            );
//
//            // 计算 Wq、Wk、Wv 的梯度
//            double[][] gradK = MatrixUtils.matMul(MatrixUtils.transposeMatrix(Q), gradOutput);
//            double[][] gradQ = MatrixUtils.matMul(MatrixUtils.transposeMatrix(K), gradOutput);
//
//            // 梯度裁剪（防止梯度爆炸）
//            double maxNorm = 1.0;
//            gradW1 = MatrixUtils.clipGradients(gradW1, maxNorm);
//            gradW2 = MatrixUtils.clipGradients(gradW2, maxNorm);
//            gradV = MatrixUtils.clipGradients(gradV, maxNorm);
//            gradK = MatrixUtils.clipGradients(gradK, maxNorm);
//            gradQ = MatrixUtils.clipGradients(gradQ, maxNorm);
//            gradW_proj = MatrixUtils.clipGradients(gradW_proj, maxNorm);
//
//            // 更新参数
//            System.out.println(String.format("encoder.W1 encoder.W1[0]:%s, %s", encoder.W1.length, encoder.W1[0].length));
//            System.out.println(String.format("gradW1 gradW1[0]:%s, %s", gradW1.length, gradW1[0].length));
//            System.out.println(String.format("gradK gradK[0]:%s, %s", gradK.length, gradK[0].length));
//            System.out.println(String.format("encoder.Wk encoder.Wk[0]:%s, %s", encoder.Wk.length, encoder.Wk[0].length));
//            System.out.println(String.format("gradV gradV[0]:%s, %s", gradV.length, gradV[0].length));
//            System.out.println(String.format("encoder.Wv encoder.Wv[0]:%s, %s", encoder.Wv.length, encoder.Wv[0].length));
//            System.out.println(String.format("encoder.Wk encoder.Wk[0]:%s, %s", encoder.Wk.length, encoder.Wk[0].length));
//            System.out.println(String.format("encoder.Wq encoder.Wq[0]:%s, %s", encoder.Wq.length, encoder.Wq[0].length));
//            encoder.W2 = MatrixUtils.subtractMatrix(encoder.W2, MatrixUtils.scaleMatrix(gradW2, learningRate));
//            encoder.W1 = MatrixUtils.subtractMatrix(encoder.W1, MatrixUtils.scaleMatrix(gradW1, learningRate));
//            encoder.Wv = MatrixUtils.subtractMatrix(encoder.Wv, MatrixUtils.scaleMatrix(gradV, learningRate));
//            encoder.Wk = MatrixUtils.subtractMatrix(encoder.Wk, MatrixUtils.scaleMatrix(gradK, learningRate));
//            encoder.Wq = MatrixUtils.subtractMatrix(encoder.Wq, MatrixUtils.scaleMatrix(gradQ, learningRate));
//
//            // 更新 W_proj
//            encoder.W_proj = MatrixUtils.subtractMatrix(encoder.W_proj, MatrixUtils.scaleMatrix(gradW_proj, learningRate));
//
//            System.out.println("Epoch " + (epoch + 1) + ": Loss = " + loss);
//        }
//    }

    public void train(double[][] input, double[][] target, int epochs) {
        for (int epoch = 0; epoch < epochs; epoch++) {
            // 前向传播
            TransformerEncoder.TransformerForwardResult result = encoder.forward(input);

            // 计算投影层的梯度
            double[][] lossGrad = MatrixUtils.subtractMatrix(result.outputPool, target); // (batch_size, 3), 1, 3
            double[][] d = MatrixUtils.unpoolGrad(lossGrad, 1, 6); // 6, 3
            double[][] gradW_proj = MatrixUtils.matMul(d, MatrixUtils.transposeMatrix(encoder.W_proj)); // 11, 3
            System.out.println(String.format("d d[0]:%s, %s", d.length, d[0].length));
            System.out.println(String.format("encoder.W_proj encoder.W_proj[0]:%s, %s", encoder.W_proj.length, encoder.W_proj[0].length));
            double[][] dWp = MatrixUtils.matMul(MatrixUtils.transposeMatrix(result.attentionOutput), d);
            System.out.println(String.format("gradW_proj gradW_proj[0]:%s, %s", gradW_proj.length, gradW_proj[0].length));


            // 步骤 1: 计算前馈网络部分的反向传播
            // 1.1 计算第一个权重矩阵 W1 的梯度
            double[][] dAttentionOutput = MatrixUtils.matMul(gradW_proj, MatrixUtils.transposeMatrix(encoder.W2));
            System.out.println(String.format("dAttentionOutput dAttentionOutput[0]:%s, %s", dAttentionOutput.length, dAttentionOutput[0].length));
            System.out.println(String.format("result.attentionOutput result.attentionOutput[0]:%s, %s", result.attentionOutput.length, result.attentionOutput[0].length));
            double[][] dW1 = MatrixUtils.matMul(MatrixUtils.transposeMatrix(result.attentionOutput), dAttentionOutput);
            System.out.println(String.format("dW1 dW1[0]:%s, %s", dW1.length, dW1[0].length));
            System.out.println(String.format("result.attentionOutput result.attentionOutput[0]:%s, %s", result.attentionOutput.length, result.attentionOutput[0].length));

            // 1.2 计算第二个权重矩阵 W2 的梯度
            System.out.println(String.format("dAttentionOutput dAttentionOutput[0]:%s, %s", dAttentionOutput.length, dAttentionOutput[0].length));
            System.out.println(String.format("encoder.W1 encoder.W1[0]:%s, %s", encoder.W1.length, encoder.W1[0].length));
            double[][] dFFOutput = MatrixUtils.matMul(dAttentionOutput, MatrixUtils.transposeMatrix(encoder.W1));
            System.out.println(String.format("dFFOutput dFFOutput[0]:%s, %s", dFFOutput.length, dFFOutput[0].length));
            System.out.println(String.format("result.ffOutput result.ffOutput[0]:%s, %s", result.ffOutput.length, result.ffOutput[0].length));
            double[][] dW2 = MatrixUtils.matMul(MatrixUtils.transposeMatrix(result.ffOutput), dFFOutput);
            System.out.println(String.format("dW2 dW2[0]:%s, %s", dW2.length, dW2[0].length));

            // 步骤 2: 计算自注意力部分的反向传播
            // 2.1 计算 attentionWeights 的梯度
            double[][] dAttentionWeights = MatrixUtils.matMul(dFFOutput, MatrixUtils.transposeMatrix(encoder.Wv));

            // 2.2 计算注意力分数 scores 的梯度
            double[][] dScores = MatrixUtils.matMul(dAttentionWeights, MatrixUtils.transposeMatrix(encoder.Wv));
            dScores = MatrixUtils.scaleMatrix(dScores, Math.sqrt(encoder.dModel));  // 缩放

            // 2.3 计算 K 和 Q 的梯度
            double[][] dK = MatrixUtils.matMul(dScores, MatrixUtils.transposeMatrix(encoder.Wq));
            double[][] dQ = MatrixUtils.matMul(dScores, MatrixUtils.transposeMatrix(encoder.Wk));

            // 2.4 计算 Q、K、V 权重矩阵 Wq, Wk, Wv 的梯度
            double[][] dWq = MatrixUtils.matMul(MatrixUtils.transposeMatrix(input), dQ);
            double[][] dWk = MatrixUtils.matMul(MatrixUtils.transposeMatrix(input), dK);
            double[][] dWv = MatrixUtils.matMul(MatrixUtils.transposeMatrix(input), dScores);

            // 梯度裁剪（防止梯度爆炸）
            double maxNorm = 1.0;
            dW1 = MatrixUtils.clipGradients(dW1, maxNorm);
            dW2 = MatrixUtils.clipGradients(dW2, maxNorm);
            dWv = MatrixUtils.clipGradients(dWv, maxNorm);
            dWk = MatrixUtils.clipGradients(dWk, maxNorm);
            dWq = MatrixUtils.clipGradients(dWq, maxNorm);
            dWp = MatrixUtils.clipGradients(dWp, maxNorm);

            // 步骤 3: 更新权重
            // 假设我们使用学习率 lr
            double lr = 0.01;

            // 更新 W1, W2, Wq, Wk, Wv
            encoder.W1 = MatrixUtils.subtractMatrix(encoder.W1, MatrixUtils.scaleMatrix(dW1, lr));
            encoder.W2 = MatrixUtils.subtractMatrix(encoder.W2, MatrixUtils.scaleMatrix(dW2, lr));
            encoder.Wq = MatrixUtils.subtractMatrix(encoder.Wq, MatrixUtils.scaleMatrix(dWq, lr));
            encoder.Wk = MatrixUtils.subtractMatrix(encoder.Wk, MatrixUtils.scaleMatrix(dWk, lr));
            encoder.Wv = MatrixUtils.subtractMatrix(encoder.Wv, MatrixUtils.scaleMatrix(dWv, lr));
            encoder.W_proj = MatrixUtils.subtractMatrix(encoder.W_proj, MatrixUtils.scaleMatrix(dWp, lr));
        }
    }

    public static void main(String[] args) throws Exception {
        TransformerEncoder encoder = new TransformerEncoder(11, 33, 3);
        TransformerTrainer trainer = new TransformerTrainer(encoder, 0.001);

//        double[][] input = {
//            {1.0, 2.0, 3.0, 4.0},
//            {5.0, 6.0, 7.0, 8.0},
//            {9.0, 10.0, 11.0, 12.0}
//        };
//
//        double[][] target = {
//            {0.5, 1.5, 2.5, 3.5},
//            {4.5, 5.5, 6.5, 7.5},
//            {8.5, 9.5, 10.5, 11.5}
//        };
//
//        trainer.train(input, target, 1000);
//        double[][] d = encoder.forward(input).output;
//        System.out.println(Arrays.deepToString(d));

        List<QuoteVO> quoteVOList = DDPGMain.getDataList();
        List<TrainData> trainDataList = getTrainData(quoteVOList);
        for(TrainData o : trainDataList) {
            trainer.train(o.getInput(), o.getTarget(), 100);
        }

        TrainData o = trainDataList.getLast();
        double[][] d = encoder.forward(o.getInput()).outputPool;
        System.out.println("###");
        System.out.println(Arrays.deepToString(d));
        System.out.println(Arrays.deepToString(o.getTarget()));
    }

    private static List<TrainData> getTrainData(List<QuoteVO> quoteVOList) {
        int beforeN = 6;
        int afterN = 3;
        int s = beforeN -1;
        int e = quoteVOList.size() - afterN - 1;

        List<TrainData> trainDataList = new ArrayList<>();
        for(int i=s; i<e; i++) {
            List<QuoteVO> beforeList = quoteVOList.subList(i - beforeN + 1, i + 1);
            List<QuoteVO> afterList = quoteVOList.subList(i + 1, i + afterN + 1);

            double[][] input = getInput(beforeList);
            double[] target = getTarget(beforeList, afterList);

            trainDataList.add(TrainData.builder()
                .input(input)
                .target(new double[][]{target})
                .build());
        }

        return trainDataList;
    }

    @Builder
    @Data
    public static class TrainData {
        private double[][] input;
        private double[][] target;
    }

    private static double[][] getInput(List<QuoteVO> quoteVOList) {
        double[][] input = new double[quoteVOList.size()][11];

        for(int i=0; i<quoteVOList.size(); i++) {
            QuoteVO quoteVO = quoteVOList.get(i);
            double[] _input = new double[] {
                quoteVO.getOpenTime().doubleValue()
                , quoteVO.getOpen().doubleValue()
                , quoteVO.getHigh().doubleValue()
                , quoteVO.getLow().doubleValue()
                , quoteVO.getClose().doubleValue()
                , quoteVO.getVolume().doubleValue()
                , quoteVO.getCloseTime().doubleValue()
                , quoteVO.getQuoteAssetVolume().doubleValue()
                , quoteVO.getTrades().doubleValue()
                , quoteVO.getTakerBuyBaseAssetVolume().doubleValue()
                , quoteVO.getTakerBuyQuoteAssetVolume().doubleValue()
            };

            input[i] = _input;
        }

        return input;
    }

    private static double[] getTarget(List<QuoteVO> beforeList, List<QuoteVO> afterList) {
        Double vwap = getVWAP(beforeList);
        Double per = vwap * 0.01;
        Double rise = 0.0;
        Double fall = 0.0;
        Double fluctuate = 0.0;
        Double total = 0.0;
        for(QuoteVO vo : afterList) {
            if(Math.abs(vo.getClose().doubleValue() - vwap) < per) {
                fluctuate += vo.getVolume().doubleValue();
            } else {
                if(vo.getClose().doubleValue() - vwap > 0) {
                    rise += vo.getVolume().doubleValue();
                }
                if(vo.getClose().doubleValue() - vwap < 0) {
                    fall += vo.getVolume().doubleValue();
                }
            }

            total += vo.getVolume().doubleValue();
        }

        return new double[]{rise / total, fall / total, fluctuate / total};
    }

    private static Double getVWAP(List<QuoteVO> quoteVOList) {
        Double tvp = 0.0;
        Double tv = 0.0;
        for(QuoteVO vo : quoteVOList) {
            Double p = vo.getClose().doubleValue();
            Double v = vo.getVolume().doubleValue();

            if(v == 0) {
                continue;
            }

            tvp += p * v;
            tv += v;
        }

        return tvp / tv;
    }
}
