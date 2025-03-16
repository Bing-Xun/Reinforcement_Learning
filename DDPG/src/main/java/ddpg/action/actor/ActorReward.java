package ddpg.action.actor;

import binace.vo.QuoteVO;
import ddpg.action.DDPGMain;
import ddpg.v3.position.Position;
import ddpg.v3.util.Utils;
import lombok.Data;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


@Data
public class ActorReward {

    /**
     *
     * @param state
     * @param nextState
     * @param quoteVOList, 計算獎勵用, 為後面幾天資訊, 趨勢算法
     * @return
     */
    public static ActorRewardVO getRewards(double[][] state, int action, double volume, List<QuoteVO> quoteVOList) {
        ActorRewardVO actorRewardVO = new ActorRewardVO();
        actorRewardVO.setState(state);
        actorRewardVO.setReward(0.0);

        DDPGMain.TrainData d1 = new DDPGMain.TrainData(state[0]);
        DDPGMain.TrainData d2 = new DDPGMain.TrainData(state[state.length-1]);

        double price = d1.getClose().doubleValue();
        double positionPrice = d2.getPositionPrice().doubleValue();
//        double vwap = getVWAP(state);
        boolean isBuyPer = ((positionPrice - price) / positionPrice) >= DDPGMain.trainPer;
        boolean isSellPer = ((price - positionPrice) / positionPrice) >= DDPGMain.trainPer;
        double diffV = Math.abs(getPriceDiff(price, quoteVOList) * volume);
        double reward = 0.0;

        // buy
        if(action == 0) {
            if(isBuyPer) {
                reward = diffV;
            } else {
                if(positionPrice < price) {
                    reward = -diffV;
                }
            }
        }

        // sell
        if(action == 1) {
            if(isSellPer) {
                reward = diffV;
            } else {
                if(positionPrice > price) {
                    reward = -diffV;
                }
            }
        }

        // hold
        if(action == 2) {
            if(isBuyPer && isSellPer) {
                reward = diffV;
            } else {
                reward = -diffV;
            }
        }

        actorRewardVO.setReward(reward * volume);
        return actorRewardVO;
    }

    private static double[] getAction(List<Double[]> beforeList, List<QuoteVO> afterList) {
        Double vwap = getVWAP(beforeList);
        Double per = vwap * DDPGMain.trainPer;
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

    private static Double getVWAP(double[][] d) {
        List<Double[]> list = new ArrayList<>();
        for(int i=0; i<d.length; i++) {
            DDPGMain.TrainData o = new DDPGMain.TrainData(d[i]);
            list.add(new Double[]{o.getClose().doubleValue(), o.getVolume().doubleValue()});
        }

        return getVWAP(list);
    }

    private static Double getVWAP(List<Double[]> list) {
        Double tvp = 0.0;
        Double tv = 0.0;

        for(Double[] d : list) {
            Double p = d[0];
            Double v = d[1];

            if(v == 0) {
                continue;
            }

            tvp += p * v;
            tv += v;
        }

        return tvp / tv;
    }

    private static double getPriceDiff(double price, List<QuoteVO> quoteVOList) {
        double volume = 0.0;
        double v = 0.0;

        for(QuoteVO vo : quoteVOList) {
            v += (vo.getClose().doubleValue() - price) * vo.getVolume().doubleValue();
            volume += vo.getVolume().doubleValue();
        }

        return v / volume;
    }

}
