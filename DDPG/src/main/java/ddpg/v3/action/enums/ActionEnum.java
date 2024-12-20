package ddpg.v3.action.enums;

public enum ActionEnum {
    BUY(0),
    SELL(1),
    HOLD(2),
    ;

    private int value;

    ActionEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
