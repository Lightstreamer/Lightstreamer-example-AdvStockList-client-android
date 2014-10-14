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

import java.text.DecimalFormat;

import android.view.View;
import android.widget.ListView;

import com.lightstreamer.simple_demo.android.MainSubscription.Context;
import com.lightstreamer.simple_demo.android.StocksAdapter.RowHolder;
import com.lightstreamer.ls_client.UpdateInfo;

public class StockForList {
    
    private String stockName = "N/A";
    private String lastPrice = "N/A";
    private double lastPriceNum;
    private String time = "N/A";
    
    private int stockNameColor = R.color.background;
    private int lastPriceColor = R.color.background;
    private int timeColor = R.color.background; 
    
    private int pos;
    private TurnOffRunnable turningOff;
    
    private DecimalFormat format = new DecimalFormat("#.00");

    
    public StockForList(String item, int pos) {
        this.pos = pos;
    }
    
    public void update(UpdateInfo newData, final Context context) {
        boolean isSnapshot = newData.isSnapshot();
        if (newData.isValueChanged("stock_name")) {
            stockName = newData.getNewValue("stock_name");
            stockNameColor = isSnapshot ? R.color.snapshot_highlight : R.color.higher_highlight;
        }
        if (newData.isValueChanged("time")) {
            time = newData.getNewValue("time");
            timeColor = isSnapshot ? R.color.snapshot_highlight : R.color.higher_highlight;
        }
        if (newData.isValueChanged("last_price")) {
            double newPrice = Double.parseDouble(newData.getNewValue("last_price"));
            lastPrice = format.format(newPrice);
            
            if (isSnapshot) {
                lastPriceColor = R.color.snapshot_highlight;
            } else {
                lastPriceColor = newPrice < lastPriceNum ? R.color.lower_highlight : R.color.higher_highlight;
                lastPriceNum = newPrice;
            }
        }
        
        if (this.turningOff != null) {
            this.turningOff.disable();
        }
        
        
        
        context.handler.post(new Runnable() {

            @Override
            public void run() {
                RowHolder holder = extractHolder(context.listView);
                if (holder != null) {
                    fill(holder);
                }
            }
            
        });
        
        this.turningOff = new TurnOffRunnable(context);
        context.handler.postDelayed(this.turningOff,600);
    }
    

    public void fill(RowHolder holder) {
        holder.stock_name.setText(stockName);
        holder.last_price.setText(lastPrice);
        holder.time.setText(time);
        
        this.fillColor(holder);
    }
    
    public void fillColor(RowHolder holder) {
        holder.stock_name.setBackgroundResource(stockNameColor);
        holder.last_price.setBackgroundResource(lastPriceColor);
        holder.time.setBackgroundResource(timeColor);
    }

    RowHolder extractHolder(ListView listView) {
        View row = listView.getChildAt(pos - listView.getFirstVisiblePosition());
        if(row == null) {
            return null;
        }
        return (RowHolder) row.getTag();
    }
    
    
    private class TurnOffRunnable implements Runnable {

        private boolean valid = true;
        private Context context;
        
        public TurnOffRunnable(Context context) {
            this.context = context;
        }
        
        public synchronized void disable() {
            valid = false;
        }

        @Override
        public synchronized void run() {
            if (!valid) {
                return;
            }
            stockNameColor = R.color.transparent;
            lastPriceColor = R.color.transparent;
            timeColor = R.color.transparent;
            
            
            RowHolder holder = extractHolder(context.listView);
            if(holder != null) {
                fillColor(holder);
            }
        }
        
        
        
    }
    
}
