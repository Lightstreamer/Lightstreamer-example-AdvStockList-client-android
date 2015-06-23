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


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StocksAdapter extends ArrayAdapter<StockForList> {

    private Activity activity; 
    
    
    public StocksAdapter(Activity activity, int layout, ArrayList<StockForList> list) {
        super(activity,layout,list);
        this.activity = activity;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RowHolder holder;
        
        if(row == null) {
            LayoutInflater inflater = this.activity.getLayoutInflater();
            row = inflater.inflate(R.layout.row_layout, parent, false);
            
            holder = new RowHolder();
            holder.stock_name = (TextView)row.findViewById(R.id.stock_name);
            holder.last_price = (TextView)row.findViewById(R.id.last_price);
            holder.time = (TextView)row.findViewById(R.id.time);
            
            row.setTag(holder);
            
        } else {
            holder = (RowHolder)row.getTag();
        }
        
        StockForList stock = getItem(position);
        stock.fill(holder);
        
        return row;
    }
    
    public class RowHolder {
        TextView stock_name;
        TextView last_price;
        TextView time;
    }
    
}
