package ensembleLearning.event.graph.listener;

import com.google.common.eventbus.Subscribe;
import ensembleLearning.event.graph.GraphEvent;
import graph.DataPoint;
import graph.PriceChart;
import test.eventbus.MyEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GraphEventListener {

    @Subscribe
    public void handleEvent(GraphEvent event) {
        List<GraphEvent.GraphEventVO> voList = event.getGraphEventVOList();
        List<DataPoint> dataPoints = new ArrayList<>();
        List<DataPoint> earnPoints = new ArrayList<>();
        List<DataPoint> amountPoints = new ArrayList<>();

        for(GraphEvent.GraphEventVO vo : voList) {
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
}
