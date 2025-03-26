package test.eventbus;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class Main {
    public static void main(String[] args) {
        // 1. 創建 EventBus 實例
        EventBus eventBus = new EventBus();

        // 2. 創建事件監聽器實例
        MyEventListener listener = new MyEventListener();

        // 3. 註冊監聽器到 EventBus
        eventBus.register(listener);

        // 4. 創建事件實例
        MyEvent myEvent = new MyEvent("Hello from Guava EventBus!");
        String anotherEvent = "This is a String event.";
        Integer yetAnotherEvent = 123;

        // 5. 發布事件
        System.out.println("發布 MyEvent...");
        eventBus.post(myEvent);

        System.out.println("\n發布 String 事件...");
        eventBus.post(anotherEvent);

        System.out.println("\n發布 Integer 事件...");
        eventBus.post(yetAnotherEvent);

        // 你也可以註冊其他的監聽器
        AnotherListener anotherListener = new AnotherListener();
        eventBus.register(anotherListener);
        System.out.println("\n再次發布 MyEvent (這次也會被 AnotherListener 處理)...");
        eventBus.post(myEvent);
    }
}

class AnotherListener {
    @Subscribe
    public void onMyEvent(MyEvent event) {
        System.out.println("AnotherListener 接收到 MyEvent: " + event.getMessage().toLowerCase());
    }
}
