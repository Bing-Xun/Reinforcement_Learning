package ddpg.v3.reward;

import ddpg.v2.util.Utils;
import ddpg.v3.action.enums.ActionEnum;
import ddpg.v3.action.history.ActionHistory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

            if(Utils.getMaxIndex(history.getAction()) == ActionEnum.HOLD.getValue()) {
                Reward reward = new Reward();
                reward.reward = ((basePrice.subtract(history.getPrice())).divide(diffPrice, 2, RoundingMode.DOWN)).doubleValue();
                reward.state = history.getState();
                reward.nextState = nextHistory.getState();
                rewardList.add(reward);
            }

            if(Utils.getMaxIndex(history.getAction()) == ActionEnum.SELL.getValue()) {
                Reward reward = new Reward();
                reward.reward = ((basePrice.subtract(history.getPrice())).divide(diffPrice, 2, RoundingMode.DOWN)).doubleValue();
                reward.state = history.getState();
                reward.nextState = nextHistory.getState();
                rewardList.add(reward);
            }

            if(Utils.getMaxIndex(history.getAction()) == ActionEnum.BUY.getValue()) {
                Reward reward = new Reward();
                reward.reward = ((basePrice.subtract(history.getPrice()))
                    .multiply(BigDecimal.valueOf(history.getPosition() / buyPosition))
                    .divide(diffPrice, 2, RoundingMode.DOWN)).doubleValue();
                reward.state = history.getState();
                reward.nextState = nextHistory.getState();
                rewardList.add(reward);
            }
        }

        return rewardList;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public double[] getState() {
        return state;
    }

    public void setState(double[] state) {
        this.state = state;
    }

    public double[] getNextState() {
        return nextState;
    }

    public void setNextState(double[] nextState) {
        this.nextState = nextState;
    }
}
