package com.lightstreamer.simple_demo.android;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.Subscription;

public interface LightstreamerClientProxy {
    boolean start(boolean userCall);
    void stop(boolean userCall);
    void addSubscription(Subscription sub);
    void removeSubscription(Subscription sub);
    void addListener(ClientListener listener);
    void removeListener(ClientListener listener);
    String getStatus();
}
