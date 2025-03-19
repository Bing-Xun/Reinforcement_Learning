package graph;

import java.math.BigDecimal;

public class DataPoint {
    private BigDecimal price;
    private BigDecimal volume;
    private long timestamp;
    private String tag;

    public DataPoint(BigDecimal price, BigDecimal volume, long timestamp, String tag) {
        this.price = price;
        this.volume = volume;
        this.timestamp = timestamp;
        this.tag = tag;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTag() {
        return tag;
    }
}