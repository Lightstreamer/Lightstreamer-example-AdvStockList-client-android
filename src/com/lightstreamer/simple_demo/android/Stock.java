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
import java.util.Iterator;
import java.util.Map.Entry;

import android.os.Handler;
import android.widget.TextView;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.Subscription;

public class Stock {

    //var fieldsList = ["last_price", "time", "pct_change", "bid_quantity", "bid", "ask", "ask_quantity", "min", "max", "ref_price", "open_price", "stock_name", 
    
    private HashMap<String,TextView> holder = null;
    private HashMap<String,UpdateRunnable> turnOffRunnables = new HashMap<String,UpdateRunnable>();

    private String[] numericFields;
    private String[] otherFields;
    private Chart chart;
    
    private double lastPrice; //might improve by saving all the field values

    
    public Stock(String item, String[] numericFields, String[] otherFields) {
        this.numericFields = numericFields;
        this.otherFields = otherFields;
    }
    
    public void setHolder(HashMap<String,TextView> holder) { //UI thread
        this.holder = holder;
        
        this.resetHolder(holder, numericFields);
        this.resetHolder(holder, otherFields);

    }
    
    private void resetHolder(HashMap<String,TextView> holder, String[] fields) {  //UI thread
        for (int i=0; i<fields.length; i++) {
            
            TextView field = holder.get(fields[i]);
            if (field != null) {
                field.setText("N/A");
            }
            
        }
    }
    
    public void setChart(Chart chart) { //UI thread
        this.chart = chart;
        this.chart.clean();
    }
    
    public double getLastPrice() {
        return this.lastPrice;
    }
    
    public void update(ItemUpdate newData, Subscription sub, Handler handler) {
        this.updateView(newData, sub, handler, numericFields, true);
        this.updateView(newData, sub, handler, otherFields, false);
        
        //save lastPrice
        String value = newData.getValue("last_price");
        try {
            this.lastPrice = Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            //unexpected o_O
        }
        
        
        chart.addPoint(newData);
    }
    
    private void updateView(ItemUpdate newData, Subscription sub, Handler handler, String[] fields, boolean numeric) {
        boolean snapshot = newData.isSnapshot();
        String itemName = newData.getItemName();
        
        Iterator<Entry<String, String>> changedFields = newData.getChangedFieldsIterator();
        while(changedFields.hasNext()) {
            
            Entry<String, String> updatedField = changedFields.next();
            String value = updatedField.getValue();
            String fieldName = updatedField.getKey();
            TextView field = holder.get(fieldName);
            
            if (field != null) {
                
                double upDown = 0.0;
                int color;
                if (!snapshot ) {
                    // update cell color 
                    if (numeric) {
                        String oldValue = sub.getValue(itemName,fieldName); //get the current value so that we can compare it with the new ones.
                        try {
                            double valueNum = Double.parseDouble(value);
                            double oldValueNum = Double.parseDouble(oldValue);
                            upDown = valueNum - oldValueNum;
                        } catch (NumberFormatException nfe) {
                            //unexpected o_O
                        }
                    }
                    
                    if (upDown < 0) {
                        color = R.color.lower_highlight; 
                    } else {
                        color = R.color.higher_highlight; 
                    }
                   
                } else {
                    color = R.color.snapshot_highlight;
                }
                
               
                
                UpdateRunnable turnOff = turnOffRunnables.get(fieldName);
                if (turnOff != null) {
                    turnOff.invalidate();
                }
                turnOff = new UpdateRunnable(field,null,R.color.transparent);
                this.turnOffRunnables.put(fieldName, turnOff);
                
                handler.post(new UpdateRunnable(field,value,color));
                handler.postDelayed(turnOff, 600);
            }
            
        }
        
    }
    
    
    
    private class UpdateRunnable implements Runnable {
        private int background;
        private TextView view;
        private String text;
        private boolean valid = true;

        UpdateRunnable(TextView view, String text, int background) {
            this.view = view;
            this.text = text;
            this.background = background;
        }

        public synchronized void run() {
            if (this.valid) {
                if (this.text != null) {
                    view.setText(text);
                }
                view.setBackgroundResource(background);
                view.invalidate();
            }
        }

        public synchronized void invalidate() {
            this.valid = false;
        }
    }



    
    
    
}
