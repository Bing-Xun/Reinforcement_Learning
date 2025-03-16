package ddpg.action.actor;


import binace.vo.QuoteVO;
import ddpg.trend.DDPGMain;
import lstm.SimpleLSTM;
import transformer.TransformerEncoder;
import transformer.TransformerTrainer;

import java.util.Arrays;
import java.util.List;

public class Actor {
    private double learningRate = 0.01;  // 学习率
    private TransformerEncoder encoder;
    private TransformerTrainer trainer;
    private int epochs = 10;

    public Actor(TransformerEncoder encoder) {
        this.encoder = encoder;
        this.trainer = new TransformerTrainer(encoder, 0.01);
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    // Actor 預測趨勢概率的範圍 [-1, 1]
    public double[][] predict(double[][] state) {
        return encoder.forward(state).outputPool;
    }

    public void train(double[][] state, double[][] lossGrad) {
        TransformerEncoder.TransformerForwardResult result = encoder.forward(state);
        trainer.train(state, result,  lossGrad, epochs);
    }
}
