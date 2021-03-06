/*
 * Copyright (c) Lightstreamer Srl
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
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lightstreamer.client.Subscription;

import java.util.ArrayList;

public class StocksFragment extends ListFragment {
    
    onStockSelectedListener listener;
    
    public interface onStockSelectedListener {
        /** Called by HeadlinesFragment when a list item is selected */
        public void onStockSelected(int item);
    }
    
    final static String[] items = {"item1", "item2", "item3",
            "item4", "item5", "item6", "item7", "item8", "item9", "item10",
            "item11", "item12", "item13", "item14", "item15", "item16", 
            "item17", "item18", "item19", "item20" };
   
    
    public final static String[] subscriptionFields = {"stock_name", "last_price", "timestamp"};
    
    private Handler handler;
    LightstreamerClientProxy lsClient;
    
    private static ArrayList<StockForList> list;
    
    static {
        list = new ArrayList<StockForList>(items.length);
        for (int i = 0; i < items.length; i++) {
            list.add(new StockForList(items[i],i));
        }
    }
   
    private Subscription mainSubscription = new Subscription("MERGE",StocksFragment.items,StocksFragment.subscriptionFields);
    private MainSubscription mainSubscriptionListener = new MainSubscription(list);
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {

        return inflater.inflate(R.layout.list_view, container, false);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        handler = new Handler();

        setListAdapter(new StocksAdapter(getActivity(), R.layout.row_layout, list));
        
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        //there's always only one StocksFragment at a time
        mainSubscriptionListener.changeContext(handler, getListView()); 
        
        if (getFragmentManager().findFragmentById(R.id.details_fragment) != null) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
        
    }

    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < list.size(); i++) {
            StockForList item = list.get(i);
            item.clean();
        }
    }
    
    
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


        lsClient = StockListDemoApplication.client;
        mainSubscription.setDataAdapter("QUOTE_ADAPTER");
        mainSubscription.setRequestedMaxFrequency("1");
        mainSubscription.setRequestedSnapshot("yes");
        mainSubscription.addListener(mainSubscriptionListener);
        lsClient.addSubscription(mainSubscription);
      
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            listener = (onStockSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        lsClient.removeSubscription(mainSubscription);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Notify the parent activity of selected item
        listener.onStockSelected(position+1);
        
        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);
    }
    
    

}
