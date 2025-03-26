package ensembleLearning.util;

import ensembleLearning.strategy.Strategy;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Util {

    // 使用 Reflections 掃描類路徑
    public static List<Strategy> loadStrategies() {
        List<Strategy> strategies = new ArrayList<>();

        // 掃描指定包下的所有類
        Reflections reflections = new Reflections("ensembleLearning.strategy"); // 替換為你的包名
        Set<Class<? extends Strategy>> strategyClasses = reflections.getSubTypesOf(Strategy.class);

        // 實例化每個類
        for (Class<? extends Strategy> clazz : strategyClasses) {
            try {
                Strategy strategy = clazz.getDeclaredConstructor().newInstance();
                strategies.add(strategy);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return strategies;
    }
}
