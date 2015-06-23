package com.lightstreamer.simple_demo.android;

import android.util.Log;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;

public class SimpleSubscriptionListener implements SubscriptionListener {
    
    private String TAG;

    public SimpleSubscriptionListener(String tag) {
        this.TAG = tag;
    }

     @Override
    public void onClearSnapshot(String arg0, int arg1) {
        Log.i(TAG,"clear snapshot call"); //the default stocklist demo adapter does not send this event
    }

    @Override
    public void onCommandSecondLevelItemLostUpdates(int arg0, String arg1) {
        Log.wtf(TAG,"Not expecting 2nd level events");
    }

    @Override
    public void onCommandSecondLevelSubscriptionError(int arg0, String arg1,
            String arg2) {
        Log.wtf(TAG,"Not expecting 2nd level events");
    }

    @Override
    public void onEndOfSnapshot(String itemName, int arg1) {
        Log.v(TAG,"Snapshot end for " + itemName);
    }

    @Override
    public void onItemLostUpdates(String arg0, int arg1, int arg2) {
         Log.wtf(TAG,"Not expecting lost updates");
    }

    @Override
    public void onItemUpdate(ItemUpdate update) {
        Log.v(TAG,"Update for " + update.getItemName());
    }

    @Override
    public void onListenEnd(Subscription arg0) {
         Log.d(TAG,"Start listening");
    }

    @Override
    public void onListenStart(Subscription arg0) {
        Log.d(TAG,"Stop listening");
    }

    @Override
    public void onSubscription() {
        Log.v(TAG,"Subscribed");
    }

    @Override
    public void onSubscriptionError(int code, String message) {
        Log.e(TAG,"Subscription error " + code + ": " + message);
    }

    @Override
    public void onUnsubscription() {
        Log.v(TAG,"Unsubscribed");
    }

}
