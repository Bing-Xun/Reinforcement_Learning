package test.eventbus;

import com.google.common.eventbus.Subscribe;

public class MyEventListener {
    @Subscribe
    public void handleMyEvent(MyEvent event) {
        System.out.println("監聽器 1 接收到 MyEvent: " + event.getMessage());
    }

    @Subscribe
    public void anotherEventHandler(MyEvent event) {
        System.out.println("監聽器 2 也接收到 MyEvent: " + event.getMessage().toUpperCase());
    }

    @Subscribe
    public void handleGenericEvent(Object event) {
        System.out.println("監聽器 3 接收到任意事件: " + event.getClass().getSimpleName());
    }
}