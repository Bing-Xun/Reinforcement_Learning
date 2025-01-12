import binace.BinanceAPI;
import binace.vo.QuoteVO;
import ddpg.v3.SimplifiedDDPG;
import ddpg.v3.position.Position;
import ddpg.v3.util.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static List<QuoteVO> quoteVOList = new ArrayList<>();
    private static Position position;

    public static void main(String[] args) throws InterruptedException {
        schedule();

        SimplifiedDDPG.train();

        position = new Position();
        position.setAmount(BigDecimal.valueOf(1000.0)); // 初始化資金

//        while(true) {
//            List<Double> priceList = quoteVOList.stream().map(o -> {
//                return Double.valueOf(o.getOpen().doubleValue());
//            }).toList();
//            List<Double> volList = quoteVOList.stream().map(o -> {
//                return Double.valueOf(o.getVolume().doubleValue());
//            }).toList();
//
//            double[][] states = SimplifiedDDPG.getIndicatorsList(priceList, volList, priceList.size());
//            List<Double> list = new ArrayList<>();
//            list.addAll(Utils.toDoubleList(states[0]));
//            list.addAll(List.of(position.getAmount().doubleValue(), position.getPositionCnt(), position.getPrice().doubleValue()));
//            double[] state = list.stream().mapToDouble(Double::doubleValue).toArray();
//
//            Object[] o = SimplifiedDDPG.predict(state);
////            System.out.println("o:" + o[0] + " " + o[1]);
//
//            position.modifyPosition(BigDecimal.valueOf(state[0]), (Double) o[1], (Integer) o[0]);
//
//            System.out.println("==========");
//            System.out.println("action:"+o[0]);
//            System.out.println("volume:"+o[1]);
//
//            System.out.println("getAmount:"+position.getAmount());
//            System.out.println("getPositionCnt:"+position.getPositionCnt());
//            System.out.println("getPrice:"+position.getPrice());
//
//            TimeUnit.SECONDS.sleep(30);
//        }
    }

    public static void schedule() {
        // 创建一个调度线程池，包含2个线程
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                quoteVOList = BinanceAPI.getQuote("BTCUSDT", "1m");
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

}
