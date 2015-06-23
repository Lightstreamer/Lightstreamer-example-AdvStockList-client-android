package com.lightstreamer.simple_demo.android;

import com.lightstreamer.client.Subscription;

public interface LightstreamerClientProxy {
	public void start();
    public void stop(boolean applyPause);
    public void addSubscription(Subscription sub);
    public void removeSubscription(Subscription sub);
}
