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

import java.util.HashMap;
import com.androidplot.xy.XYPlot;
import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DetailsFragment extends Fragment {
    
    private static final String TAG = "Details";
    
    public final static String[] numericFields = {"last_price", "pct_change","bid_quantity", "bid", "ask", "ask_quantity", "min", "max","open_price"};
    public final static String[] otherFields = {"stock_name", "time"};
    public final static String[] subscriptionFields = {"stock_name", "last_price", "time", "pct_change","bid_quantity", "bid", "ask", "ask_quantity", "min", "max","open_price"};

    private final SubscriptionFragment subscriptionHandling = new SubscriptionFragment();
    private Handler handler;
    HashMap<String, TextView> holder =  new HashMap<String, TextView>();
    Chart chart = new Chart();
    ToggleButton toggle;
    
    public static final String ARG_ITEM = "item";
    public static final String ARG_PN_CONTROLS = "pn_controls";
    
    int currentItem = 0;

    private Subscription currentSubscription = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        
        // If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            currentItem = savedInstanceState.getInt(ARG_ITEM);
        }
        

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.details_view, container, false);

        holder.put("stock_name",(TextView)view.findViewById(R.id.d_stock_name));
        holder.put("last_price",(TextView)view.findViewById(R.id.d_last_price));
        holder.put("time",(TextView)view.findViewById(R.id.d_time));
        holder.put("pct_change",(TextView)view.findViewById(R.id.d_pct_change));
        holder.put("bid_quantity",(TextView)view.findViewById(R.id.d_bid_quantity));
        holder.put("bid",(TextView)view.findViewById(R.id.d_bid));
        holder.put("ask",(TextView)view.findViewById(R.id.d_ask));
        holder.put("ask_quantity",(TextView)view.findViewById(R.id.d_ask_quantity));
        holder.put("min",(TextView)view.findViewById(R.id.d_min));
        holder.put("max",(TextView)view.findViewById(R.id.d_max));
        holder.put("open_price",(TextView)view.findViewById(R.id.d_open_price));
        
        final XYPlot plot = (XYPlot) view.findViewById(R.id.mySimpleXYPlot);
        chart.setPlot(plot);

        
        return view;
    }
    
    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        if (args != null) {
            updateStocksView(args.getInt(ARG_ITEM));
        } else if (currentItem != 0) {
            updateStocksView(currentItem);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        chart.onPause();
        this.subscriptionHandling.onPause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        chart.onResume(this.getActivity());
        this.subscriptionHandling.onResume();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.subscriptionHandling.onAttach(activity);
    }
    
    
    public void updateStocksView(int item) {
        if (item != currentItem || this.currentSubscription == null) {
            if (this.currentSubscription != null) {
                this.currentSubscription.removeListener(this.currentSubscription.getListeners()[0]);
            }
            
            String itemName = "item"+item;
            
            this.currentSubscription = new Subscription("MERGE",itemName,subscriptionFields);
            currentSubscription.setDataAdapter("QUOTE_ADAPTER");
            this.currentSubscription.setRequestedSnapshot("yes");
            
            Stock stock = new Stock(itemName,numericFields,otherFields);
            stock.setHolder(holder);
            stock.setChart(chart);
            
            this.currentSubscription.addListener(new StockListener(stock));
            
            
            
            this.subscriptionHandling.setSubscription(this.currentSubscription);
            
            currentItem = item;
        }
    }
    
    
    public int getCurrentStock() {
        return this.currentItem;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ARG_ITEM, currentItem);
    }
    
   
    
    private class StockListener implements SubscriptionListener {
        
        private final Stock stock;
        private Subscription sub;
        
        public StockListener(Stock stock) {
            this.stock = stock;
        }
        

        @Override
        public void onClearSnapshot(String arg0, int arg1) {
            Log.v(TAG,"Clear snapshot event");
        }

        @Override
        public void onCommandSecondLevelItemLostUpdates(int arg0, String arg1) {
            Log.wtf(TAG,"Not expecting 2nd level events");
        }

        @Override
        public void onCommandSecondLevelSubscriptionError(int arg0,
                String arg1, String arg2) {
            Log.wtf(TAG,"Not expecting 2nd level events");
        }

        @Override
        public void onEndOfSnapshot(String arg0, int itemName) {
             Log.v(TAG,"Snapshot end for " + itemName);
        }

        @Override
        public void onItemLostUpdates(String arg0, int arg1, int arg2) {
            Log.wtf(TAG,"Not expecting lost updates");
        }

        @Override
        public void onItemUpdate(ItemUpdate update) {
            Log.v(TAG,"Update for " + update.getItemName());
            this.stock.update(update,sub,handler);
        }

        @Override
        public void onListenEnd(Subscription arg0) {
        }

        @Override
        public void onListenStart(Subscription sub) {
            this.sub = sub;
        }

        @Override
        public void onSubscription() {
            Log.v(TAG,"Subscribed");
        }

        @Override
        public void onSubscriptionError(int code, String message) {
            Log.v(TAG,"Subscription error " + code+": " + message);
        }

        @Override
        public void onUnsubscription() {
             Log.v(TAG,"Unsubscribed");
        }
        
    }

    

}
