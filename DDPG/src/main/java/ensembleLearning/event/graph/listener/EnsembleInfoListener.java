package ensembleLearning.event.graph.listener;

import com.google.common.eventbus.Subscribe;
import ensembleLearning.event.graph.EnsembleInfoEvent;

public class EnsembleInfoListener {

    @Subscribe
    public void handleEvent(EnsembleInfoEvent event) {
        System.out.println("holdCnt:"+event.getHoldCnt());
        System.out.println("sellCnt:"+event.getSellCnt());
        System.out.println("buyCnt:"+event.getBuyCnt());
        System.out.println("earnAmount:"+event.getEarnAmount());
        System.out.println("minAmount:"+event.getMinAmount());
        System.out.println("maxAmount:"+event.getMaxAmount());
    }
}
