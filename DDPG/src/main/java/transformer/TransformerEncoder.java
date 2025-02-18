package transformer;

public class TransformerEncoder {
    public final int dModel;
    public final int dHidden;
    public final int output_dim;
    public double[][] Wq, Wk, Wv, W1, W2;
    public double[][] W_proj; // output_dim 可能是 3


    public TransformerEncoder(int dModel, int dHidden, int output_dim) {
        this.dModel = dModel;
        this.dHidden = dHidden;
        this.output_dim = output_dim;
        this.Wq = MatrixUtils.heInit(dModel, dModel);
        this.Wk = MatrixUtils.heInit(dModel, dModel);
        this.Wv = MatrixUtils.heInit(dModel, dModel);
        this.W1 = MatrixUtils.heInit(dModel, dHidden);
        this.W2 = MatrixUtils.heInit(dHidden, dModel);

        this.W_proj = MatrixUtils.randomMatrix(dModel, output_dim); // output_dim 可能是 3
    }

    public TransformerForwardResult forward(double[][] input) {
        /**
         * 步骤 2：计算 Q、K、V
         */
        double[][] Q = MatrixUtils.matMul(input, Wq);
        double[][] K = MatrixUtils.matMul(input, Wk);
        double[][] V = MatrixUtils.matMul(input, Wv);

        double[][] scores = MatrixUtils.matMul(Q, MatrixUtils.transposeMatrix(K));
//        System.out.println(String.format("scores scores[0]:%s, %s", scores.length, scores[0].length));
        MatrixUtils.scaleMatrix(scores, Math.sqrt(dModel));

        /**
         * 步骤 3：计算自注意力（Scaled Dot-Product Attention）
         */
        double[][] attentionWeights = MatrixUtils.softmax(scores);
        double[][] attentionOutput = MatrixUtils.matMul(attentionWeights, V);

        // GELU 替换 ReLU
        double[][] ffOutput = MatrixUtils.gelu(MatrixUtils.matMul(attentionOutput, W1));
        double[][] ffOutput1 = MatrixUtils.matMul(ffOutput, W2);

        double[][] Output = MatrixUtils.layerNorm(MatrixUtils.addMatrix(attentionOutput, ffOutput1));
        double[][] projectedOutput = MatrixUtils.matMul(Output, W_proj);
        double[][] projectedOutputPool = MatrixUtils.poolProject(projectedOutput);

        // 创建 TransformerForwardResult 来存储所有数据
        return new TransformerForwardResult(
            projectedOutputPool,
            attentionOutput,
            ffOutput,
            Q,
            K,
            V
        );
    }

    public class TransformerForwardResult {
        public double[][] outputPool;
        public double[][] attentionOutput;
        public double[][] ffOutput;
        public double[][] Q;
        public double[][] K;
        public double[][] V;

        public TransformerForwardResult(double[][] outputPool, double[][] attentionOutput, double[][] ffOutput, double[][] Q, double[][] K, double[][] V) {
            this.outputPool = outputPool;
            this.attentionOutput = attentionOutput;
            this.ffOutput = ffOutput;
            this.Q = Q;
            this.K = K;
            this.V = V;
        }
    }

    /**
     * 1️⃣	输入数据（input）	(3, 4)
     * 2️⃣	计算 Q, K, V	(3, 4)
     * 3️⃣	自注意力计算	(3, 4)
     * 4️⃣	多头注意力计算	(3, 4)
     * 5️⃣	残差连接 + 归一化	(3, 4)
     * 6️⃣	前馈网络（FFN）	(3, 4)
     * 7️⃣	再次残差连接 + 归一化	(3, 4)
     * ✅	最终输出（output）	(3, 4)
     *
     * 这段代码展示了 Transformer 编码器 的完整前向传播：
     *
     * 输入数据（3×4 矩阵） → 计算 Q、K、V
     * 计算自注意力 → 多头注意力
     * 残差连接 + 归一化 → 前馈网络（FFN）
     * 再次残差连接 + 归一化 → 最终输出
     * 最终 output 仍然是一个 3×4 矩阵，但包含了 Transformer 编码后的深层特征信息！ 🚀
     */
    public static void main(String[] args) {
        /**
         * 4 代表输入数据的 特征维度（dModel），即每个数据点有 4 个特征。
         * 8 代表 注意力头数（numHeads），即 Multi-Head Attention 的数量。
         */
        TransformerEncoder encoder = new TransformerEncoder(4, 8, 3);

        /**
         * input 是一个 3×4 的矩阵，表示 3 个输入数据，每个数据有 4 个特征。
         * 例如，第 1 行 {1.0, 2.0, 3.0, 4.0} 是第 1 个数据点的特征。
         */
        double[][] input = {
            {1.0, 2.0, 3.0, 4.0},
            {5.0, 6.0, 7.0, 8.0},
            {9.0, 10.0, 11.0, 12.0}
        };

//        double[][] output = encoder.forward(input).output;
//        MatrixUtils.printMatrix(output);
    }
}

