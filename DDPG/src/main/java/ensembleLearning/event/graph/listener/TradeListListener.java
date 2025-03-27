package ensembleLearning.event.graph.listener;

import com.google.common.eventbus.Subscribe;
import ensembleLearning.event.graph.TradeListEvent;
import graph.DataPoint;
import graph.PriceChart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TradeListListener {

    @Subscribe
    public void handleEvent(TradeListEvent event) {
        plotGraph(event.getGraphEventVOList());
        showTickAmount(event.getGraphEventVOList());
    }

    private void plotGraph(List<TradeListEvent.TradeVO> voList) {
        List<DataPoint> dataPoints = new ArrayList<>();
        List<DataPoint> earnPoints = new ArrayList<>();
        List<DataPoint> amountPoints = new ArrayList<>();

        for(TradeListEvent.TradeVO vo : voList) {
            String action = vo.getAction();
            BigDecimal price = vo.getPrice();

            DataPoint dataPoint = new DataPoint(
                vo.getPrice()
                , new BigDecimal(vo.getPositionCnt())
                , vo.getCloseTime()
                , action.equals("HOLD") ? "" : action + ":" + vo.getCloseTime());
//                , "");
            dataPoints.add(dataPoint);

            double b = vo.getPositionCnt() * price.doubleValue();
            DataPoint earnPoint = new DataPoint(
                new BigDecimal(b).add(vo.getAmount())
                , new BigDecimal(vo.getPositionCnt())
                , vo.getCloseTime()
//                , action.equals("HOLD") ? "" : actionVO.getAction() + ":" + actionVO.getCloseTime());
                , "");
            earnPoints.add(earnPoint);

            DataPoint amountPoint = new DataPoint(
                vo.getAmount()
                , new BigDecimal(vo.getPositionCnt())
                , vo.getCloseTime()
//                , action.equals("HOLD") ? "" : actionVO.getAction() + ":" + actionVO.getCloseTime());
                , "");
            amountPoints.add(amountPoint);
        }

        PriceChart.plot(dataPoints);
        PriceChart.plot(earnPoints);
        PriceChart.plot(amountPoints);
    }

    private void showTickAmount(List<TradeListEvent.TradeVO> voList) {
        for(TradeListEvent.TradeVO vo : voList) {
            double b = vo.getPositionCnt() * vo.getPrice().doubleValue();
            if(Set.of("BUY", "SELL").contains(vo.getAction())) {
                System.out.println(vo.getAmount().add(new BigDecimal(b)) + ":" + vo.getAmount());
            }
        }
    }
}
