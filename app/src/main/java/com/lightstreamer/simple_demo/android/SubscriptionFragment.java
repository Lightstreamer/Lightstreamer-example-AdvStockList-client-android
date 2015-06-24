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


import android.app.Activity;
import android.util.Log;

import com.lightstreamer.client.Subscription;

/**
 * We may subscribe/unsubscribe during onAttach/onDetach events (to keep the subscription alive as much as possible even
 * if the fragment is not visible) or during onResume/onPause so that the subscription is only alive if the fragment is visible.
 * This implementation follows the latter approach while the StocksFragment uses the former. 
 *
 */


public class SubscriptionFragment /*extends Fragment*/ {

    private static final String TAG = "SubscriptionFragment";
    
    private LightstreamerClientProxy lsClient;
    private Subscription subscription;
    private boolean subscribed = false;
    private boolean running = false;
    
    protected synchronized void setSubscription(Subscription subscription) {
        if (this.subscription != null && subscribed) {
            Log.d(TAG,"Replacing subscription");
            this.lsClient.removeSubscription(this.subscription);
        }
        Log.d(TAG,"New subscription " + subscription);
        this.subscription = subscription;
        
        if (running) {
            this.lsClient.addSubscription(this.subscription);
        }
    }
    
    public synchronized void onResume() {
        //subscribe
        if (this.lsClient != null && this.subscription != null) {
            this.lsClient.addSubscription(this.subscription);
            subscribed = true;
        }
        running = true;
    }
    
    
    public synchronized void onPause() {
        //unsubscribe
        if (this.lsClient != null && this.subscription != null) {
            this.lsClient.removeSubscription(this.subscription);
            subscribed = false;
        }
        running = false;
    }
    
    public synchronized void onAttach(Activity activity) {
        lsClient = StockListDemoApplication.client;
    }
}
