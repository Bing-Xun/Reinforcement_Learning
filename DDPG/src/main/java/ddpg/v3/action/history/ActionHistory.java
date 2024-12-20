package ddpg.v3.action.history;

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

    public static class History {
        private double[] state;
        private double[] action;
        private BigDecimal price;
        private Double position;

        public double[] getAction() {
            return action;
        }

        public void setAction(double[] action) {
            this.action = action;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Double getPosition() {
            return position;
        }

        public void setPosition(Double position) {
            this.position = position;
        }

        public double[] getState() {
            return state;
        }

        public void setState(double[] state) {
            this.state = state;
        }
    }
}
