package transformer;

import java.util.Arrays;

public class PositionalEncoding {
    public static double[][] getPositionalEncoding(int seqLen, int dModel) {
        double[][] pe = new double[seqLen][dModel];
        double divTerm;

        for (int pos = 0; pos < seqLen; pos++) {
            for (int i = 0; i < dModel / 2; i++) {
                divTerm = Math.pow(10000, (2.0 * i) / dModel);
                pe[pos][2 * i] = Math.sin(pos / divTerm);
                pe[pos][2 * i + 1] = Math.cos(pos / divTerm);
            }
        }
        return pe;
    }

    public static void main(String[] args) {
        int seqLen = 30; // 序列长度
        int dModel = 4;    // 维度
        double[][] encoding = getPositionalEncoding(seqLen, dModel);

        // 打印部分编码结果
        for (int i = 0; i < 10; i++) {
            System.out.println(Arrays.toString(encoding[i]));
        }
    }
}
