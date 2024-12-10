package ddpg.v3.volume.actor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Random;

public class VolumeActor {
    private double[] weights; // 狀態和方向概率的權重
    private double epsilon = 0.1;  // ε-greedy 探索率
    static Random random = new Random();

    public VolumeActor(int inputSize) {
        this.weights = new double[inputSize];
        // 初始化權重
        for (int i = 0; i < inputSize; i++) {
            weights[i] = random.nextDouble() * 0.1;
        }
    }

    public static double getMaxPosition(BigDecimal amount, BigDecimal price, double positionCnt) {
        return Math.max(amount.divide(price, 10, RoundingMode.HALF_UP).doubleValue(), positionCnt);
    }

    public double predict(double[] state, double[] actionProbs, double maxPosition) {
        if(maxPosition <= 0) {
            return 0.0;
        }

        if (random.nextDouble() < epsilon) {
            return random.nextDouble(0.0, maxPosition);
        }

        double[] inputs = new double[state.length + actionProbs.length];
        System.arraycopy(state, 0, inputs, 0, state.length);
        System.arraycopy(actionProbs, 0, inputs, state.length, actionProbs.length);

        double volume = 0.0;
        for (int i = 0; i < inputs.length; i++) {
            volume += inputs[i] * weights[i];
        }

        volume = Math.min(volume, maxPosition);
        volume = Math.max(volume, 0.0); // 確保交易量非負
        return volume;
    }

    public void updateWeights(double[] state, double[] actionProbs, double tdError, double learningRate) {
        // 合併狀態和方向概率作為輸入
        double[] inputs = new double[state.length + actionProbs.length];
        System.arraycopy(state, 0, inputs, 0, state.length);
        System.arraycopy(actionProbs, 0, inputs, state.length, actionProbs.length);

        // 更新權重
        for (int i = 0; i < weights.length; i++) {
            // 策略梯度更新
            weights[i] += learningRate * tdError * inputs[i];
        }
    }

    public double[] getWeights() {
        System.out.println(Arrays.toString(weights));
        return weights;
    }
}
