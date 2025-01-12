package ddpg.v3.action.history;

import ddpg.v3.action.enums.ActionEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ActionHistory {

    private List<History> historyList = new ArrayList<>();

    public List<History> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<History> historyList) {
        this.historyList = historyList;
    }

    @Data
    public static class History {
        private double[] state;
        private BigDecimal amount;
        private Action action;
        private Position position;
    }

    @Data
    public static class Action {
        private double[] action;
        private ActionEnum actionEnum;
        private BigDecimal price;
        private double volume;
    }

    @Data
    public static class Position {
        private BigDecimal price;
        private Double cnt;
    }
}
