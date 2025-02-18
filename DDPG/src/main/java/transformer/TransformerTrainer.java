package transformer;

import binace.vo.QuoteVO;
import ddpg.trend.DDPGMain;
import ddpg.v3.util.Utils;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            TransformerEncoder.TransformerForwardResult result = encoder.forward(input);

            // 计算投影层的梯度
            double[][] lossGrad = MatrixUtils.subtractMatrix(result.outputPool, target); // (batch_size, 3), 1, 3
            double[][] d = MatrixUtils.unpoolGrad(lossGrad, 1, 6); // 6, 3
            double[][] gradW_proj = MatrixUtils.matMul(d, MatrixUtils.transposeMatrix(encoder.W_proj)); // 11, 3
            double[][] dWp = MatrixUtils.matMul(MatrixUtils.transposeMatrix(result.attentionOutput), d);

            // 步骤 1: 计算前馈网络部分的反向传播
            // 1.1 计算第一个权重矩阵 W1 的梯度
            double[][] dAttentionOutput = MatrixUtils.matMul(gradW_proj, MatrixUtils.transposeMatrix(encoder.W2));
            double[][] dW1 = MatrixUtils.matMul(MatrixUtils.transposeMatrix(result.attentionOutput), dAttentionOutput);

            // 1.2 计算第二个权重矩阵 W2 的梯度
            double[][] dFFOutput = MatrixUtils.matMul(dAttentionOutput, MatrixUtils.transposeMatrix(encoder.W1));
            double[][] dW2 = MatrixUtils.matMul(MatrixUtils.transposeMatrix(result.ffOutput), dFFOutput);

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
            // 更新 W1, W2, Wq, Wk, Wv
            encoder.W1 = MatrixUtils.subtractMatrix(encoder.W1, MatrixUtils.scaleMatrix(dW1, learningRate));
            encoder.W2 = MatrixUtils.subtractMatrix(encoder.W2, MatrixUtils.scaleMatrix(dW2, learningRate));
            encoder.Wq = MatrixUtils.subtractMatrix(encoder.Wq, MatrixUtils.scaleMatrix(dWq, learningRate));
            encoder.Wk = MatrixUtils.subtractMatrix(encoder.Wk, MatrixUtils.scaleMatrix(dWk, learningRate));
            encoder.Wv = MatrixUtils.subtractMatrix(encoder.Wv, MatrixUtils.scaleMatrix(dWv, learningRate));
            encoder.W_proj = MatrixUtils.subtractMatrix(encoder.W_proj, MatrixUtils.scaleMatrix(dWp, learningRate));
        }
    }

    public static void main(String[] args) throws Exception {
        TransformerEncoder encoder = new TransformerEncoder(11, 33, 3);
        TransformerTrainer trainer = new TransformerTrainer(encoder, 0.01);

        List<QuoteVO> quoteVOList = DDPGMain.getDataList();
        List<TrainData> trainDataList = getTrainData(quoteVOList);
        for(TrainData o : trainDataList) {
            trainer.train(o.getInput(), o.getTarget(), 15);
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
        Double per = vwap * 0.003;
//        System.out.println("per:"+per);
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
