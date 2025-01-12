package ddpg.v3.action.critic;

import ddpg.v2.util.Utils;
import ddpg.v3.action.enums.ActionEnum;
import ddpg.v3.action.history.ActionHistory;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Reward {

    private double reward;
    private double[] state;
    private double[] nextState;

    /**
     * 最後一筆應為sell再跑
     * @param actionHistory
     */
    public static List<Reward> getRewards(ActionHistory actionHistory) {
        List<ActionHistory.History> historyList = actionHistory.getHistoryList();
        List<ActionHistory.History> buyHistoryList = new ArrayList<>();
        List<ActionHistory.History> sellHistoryList = new ArrayList<>();
        Double buyPosition = 0.0;
        Double sellPosition = 0.0;

        for(ActionHistory.History history : historyList) {
            if(Utils.getMaxIndex(history.getAction()) == ActionEnum.BUY.getValue()) {
                buyPosition += history.getPosition();
                buyHistoryList.add(history);
            }
            if(Utils.getMaxIndex(history.getAction()) == ActionEnum.SELL.getValue()) {
                sellPosition += history.getPosition();
                sellHistoryList.add(history);
            }
        }

        if(buyPosition == 0.0) {
            if(historyList.size() < 2) {
                ActionHistory.History history = historyList.get(historyList.size() - 1);
                Reward reward = new Reward();
                reward.setState(history.getState());
                reward.setNextState(history.getState());
                reward.setReward(-1);
                return List.of(reward);
            }

            ActionHistory.History history = historyList.get(historyList.size() - 2);
            ActionHistory.History nextHistory = historyList.get(historyList.size() - 1);
            Reward reward = new Reward();
            reward.setState(history.getState());
            reward.setNextState(nextHistory.getState());
            reward.setReward(-1);
            return List.of(reward);
        }

        BigDecimal basePriceNumerator = BigDecimal.ZERO;
        BigDecimal basePriceDenominator = BigDecimal.ZERO;
        for(ActionHistory.History history : buyHistoryList) {
            basePriceNumerator = basePriceNumerator.add(history.getPrice().multiply(BigDecimal.valueOf(history.getPosition())));
            basePriceDenominator = basePriceDenominator.add(BigDecimal.valueOf(history.getPosition()));
        }
        BigDecimal basePrice = basePriceNumerator.divide(basePriceDenominator, 8 , RoundingMode.DOWN);
        Double finalBuyPosition = buyPosition;
        Double finalSellPosition = sellPosition;
        BigDecimal diffPrice = historyList.stream()
            .map(o -> {
                if(Utils.getMaxIndex(o.getAction()) == ActionEnum.BUY.getValue()) {
                    return o.getPrice().multiply(BigDecimal.valueOf(o.getPosition()))
                        .divide(BigDecimal.valueOf(finalBuyPosition), 8, RoundingMode.DOWN)
                        .multiply(BigDecimal.valueOf(finalSellPosition));
                }

                return o.getPrice().subtract(basePrice).abs();
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(historyList.size()), 8, RoundingMode.DOWN);

        List<Reward> rewardList = new ArrayList<>();
        for(int i=0; i<historyList.size()-1; i++) {
            ActionHistory.History history = historyList.get(i);
            ActionHistory.History nextHistory = historyList.get(i+1); // 因為這邊要拿到next, 所以 size-1

//            System.out.println("history.getAction():" + Utils.getMaxIndex(Arrays.copyOf(history.getAction(), history.getAction().length - 1)));
            if(Utils.getMaxIndex(Arrays.copyOf(history.getAction(), history.getAction().length - 1)) == ActionEnum.HOLD.getValue()) {
                Reward reward = new Reward();
                reward.state = history.getState();
                reward.nextState = nextHistory.getState();

                reward.reward = 0.0;
                if(history.getVolume() != 0.0) {
                    reward.reward = -10;
                }
                if(reward.reward == 0.0) {
                    reward.reward = ((basePrice.subtract(history.getPrice())).divide(diffPrice, 2, RoundingMode.DOWN)).doubleValue();
                }

                rewardList.add(reward);
//                System.out.println("reward:"+reward.reward);
            }

            if(Utils.getMaxIndex(Arrays.copyOf(history.getAction(), history.getAction().length - 1)) == ActionEnum.SELL.getValue()) {
                Reward reward = new Reward();
                reward.state = history.getState();
                reward.nextState = nextHistory.getState();

                reward.reward = 0.0;
                if(history.getPosition() == 0.0) {
                    reward.reward = -10;
                }
                if(history.getPosition() < history.getVolume()) {
                    reward.reward = -10;
                }
                if(reward.reward == 0.0) {
                    reward.reward = ((basePrice.subtract(history.getPrice())).divide(diffPrice, 2, RoundingMode.DOWN)).doubleValue();
                }

                rewardList.add(reward);
//                System.out.println("reward:"+reward.reward);
            }

            if(Utils.getMaxIndex(Arrays.copyOf(history.getAction(), history.getAction().length - 1)) == ActionEnum.BUY.getValue()) {
                Reward reward = new Reward();
                reward.state = history.getState();
                reward.nextState = nextHistory.getState();

                reward.reward = 0.0;
                if(history.getVolume() == 0.0) {
                    reward.reward = -10;
                }
                if(history.getVolume() * history.getPrice().doubleValue() > history.getAmount().doubleValue()) {
                    reward.reward = -10;
                    System.out.println("### aaa");
                }
                if(reward.reward == 0.0) {
                    reward.reward = ((basePrice.subtract(history.getPrice()))
                            .multiply(BigDecimal.valueOf(history.getPosition() / buyPosition))
                            .divide(diffPrice, 2, RoundingMode.DOWN)).doubleValue();
                }

                rewardList.add(reward);
            }
        }

        return rewardList;
    }
}
