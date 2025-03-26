package ensembleLearning.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighLowStrategyUtil {

    public static List<Double> calculatePercentiles(List<Double> closePricesList) {
        if (closePricesList == null || closePricesList.isEmpty()) {
            return List.of();
        }

        double minPrice = Collections.min(closePricesList);
        double maxPrice = Collections.max(closePricesList);
        double priceRange = maxPrice - minPrice;
        double percentileInterval = priceRange / 100; // Divide by 100 for percentiles

        List<Double> percentileValues = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            percentileValues.add(minPrice + i * percentileInterval);
        }

        return percentileValues;
    }

    public static int getPercentile(double value, List<Double> percentileValues) {
        if (percentileValues == null || percentileValues.size() != 101) {
            return -1; // Invalid percentileValues list
        }

        if (value < percentileValues.get(0)) {
            return -1; // Value is below the 0th percentile
        } else if (value > percentileValues.get(100)) {
            return -1; // Value is above the 100th percentile
        }

        for (int i = 1; i <= 100; i++) {
            if (value <= percentileValues.get(i)) {
                return i - 1; // Percentiles are 0-indexed
            }
        }

        return 100; // Should not reach here, but added for safety
    }
}
