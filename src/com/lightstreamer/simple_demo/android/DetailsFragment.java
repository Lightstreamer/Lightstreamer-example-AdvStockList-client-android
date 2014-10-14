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
import java.util.concurrent.atomic.AtomicBoolean;

import com.androidplot.xy.XYPlot;
import com.lightstreamer.ls_client.ExtendedTableInfo;
import com.lightstreamer.ls_client.HandyTableListener;
import com.lightstreamer.ls_client.SubscrException;
import com.lightstreamer.ls_client.SubscribedTableKey;
import com.lightstreamer.ls_client.UpdateInfo;

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

    private ItemSubscription currentSubscription = null;
    
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
                this.currentSubscription.disable();
            }
            this.currentSubscription = new ItemSubscription("item"+item);
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
    
    
    private class ItemSubscription implements Subscription {
    
        private final Stock stock;
        private ExtendedTableInfo tableInfo;
        private SubscribedTableKey key;
        private StockListener listener;
        
        public ItemSubscription(String item) {
            this.stock = new Stock(item,numericFields,otherFields);
            stock.setHolder(holder);
            stock.setChart(chart);
                     
            this.listener = new StockListener(stock);
            
            try {
                this.tableInfo = new ExtendedTableInfo(new String[] {item}, "MERGE", subscriptionFields , true);
                this.tableInfo.setDataAdapter("QUOTE_ADAPTER");
            } catch (SubscrException e) {
                Log.wtf(TAG, "I'm pretty sure MERGE is compatible with the snapshot request!");
            }
        }

        public void disable() {
            this.listener.disable();
        }

        @Override
        public HandyTableListener getTableListener() {
            return this.listener;
        }

        @Override
        public SubscribedTableKey getTableKey() {
            return  this.key;
        }

        @Override
        public ExtendedTableInfo getTableInfo() {
            return this.tableInfo;
        }

        @Override
        public void setTableKey(SubscribedTableKey key) {
            this.key = key;
        }

    }
    
    private class StockListener implements HandyTableListener {
        
        private AtomicBoolean disabled = new AtomicBoolean(false);
        private final Stock stock;
        
        public StockListener(Stock stock) {
            this.stock = stock;
        }
        
        public void disable() {
            disabled.set(true);
        }
        
        @Override
        public void onRawUpdatesLost(int arg0, String arg1, int arg2) {
            Log.wtf(TAG,"Not expecting lost updates");
        }

        @Override
        public void onSnapshotEnd(int itemPos, String itemName) {
            Log.v(TAG,"Snapshot end for " + itemName);
        }

        @Override
        public void onUnsubscr(int itemPos, String itemName) {
            Log.v(TAG,"Unsubscribed " + itemName);
        }

        @Override
        public void onUnsubscrAll() {
            Log.v(TAG,"Unsubscribed all");
        }

        @Override
        public void onUpdate(int itemPos, String itemName, UpdateInfo newData) {
            if (disabled.get()) {
                return;
            }
            Log.v(TAG,"Update for " + itemName);
            this.stock.update(newData,handler);
        }
        
    }

    

}
