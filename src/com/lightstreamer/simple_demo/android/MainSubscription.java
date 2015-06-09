/*
 * Copyright 2015 Weswit Srl
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

import android.os.Handler;
import android.widget.ListView;


class MainSubscription extends SimpleSubscriptionListener {

    private static final String TAG = "MainSubscription";
   

    private ArrayList<StockForList> list;

    private Context context = new Context();
    
    public MainSubscription(ArrayList<StockForList> list) {
    	super(TAG);
        this.list = list;
    }
    
    public void changeContext(Handler handler, ListView listView) {
        this.context.handler = handler;
        this.context.listView = listView;
    }
    
    @Override
    public void onItemUpdate(ItemUpdate update) {
    	super.onItemUpdate(update);
        final StockForList toUpdate = list.get(update.getItemPos()-1);
        toUpdate.update(update,this.context);
    }
    
    public class Context {
        public Handler handler;
        public ListView listView;
    }

}