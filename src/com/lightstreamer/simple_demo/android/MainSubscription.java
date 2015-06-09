/*
 * Copyright 2014 Weswit Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lightstreamer.simple_demo.android;

import java.util.ArrayList;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;

import android.os.Handler;
import android.util.Log;
import android.widget.ListView;


class MainSubscription implements SubscriptionListener {

    private static final String TAG = "MainSubscription";
   

    private ArrayList<StockForList> list;

    private Context context = new Context();
    
    public MainSubscription(ArrayList<StockForList> list) {
        this.list = list;
    }
    
    public void changeContext(Handler handler, ListView listView) {
        this.context.handler = handler;
        this.context.listView = listView;
    }
    
    public class Context {
        public Handler handler;
        public ListView listView;
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
        final StockForList toUpdate = list.get(update.getItemPos()-1);
        toUpdate.update(update,this.context);
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