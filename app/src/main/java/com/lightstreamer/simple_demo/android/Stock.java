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

import android.os.Handler;
import android.widget.TextView;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.Subscription;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class Stock extends SimpleSubscriptionListener {
    private final String[] fields;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    //var fieldsList = ["last_price", "time", "pct_change", "bid_quantity", "bid", "ask", "ask_quantity", "min", "max", "ref_price", "open_price", "stock_name", 
    
    private HashMap<String,TextView> holder = null;
    private HashMap<String,UpdateRunnable> turnOffRunnables = new HashMap<String,UpdateRunnable>();

    Set<String> numericField;
    
    private Handler handler;
    volatile private Subscription sub;

    
    public Stock(Set<String> numericFields, String[] fields, Handler handler, HashMap<String,TextView> holder) {
        super("Stock");

        this.fields = fields;
        this.numericField = numericFields;

        this.handler = handler;
        this.holder = holder;
    }

    public void setSubscription(Subscription sub) {
        this.sub = sub;
    }
    
    @Override
    public void onListenStart() {
    	super.onListenStart();

    	handler.post(new ResetRunnable());
    }
    
    @Override
    public void onListenEnd() {
    	super.onListenEnd();
    	this.sub = null;
    }
    
    
    @Override
    public void onItemUpdate(ItemUpdate update) {
    	super.onItemUpdate(update);
    	this.updateView(update);
    }
    
    private void updateView(ItemUpdate newData) {
        boolean snapshot = newData.isSnapshot();
        String itemName = newData.getItemName();
        
        Iterator<Entry<String, String>> changedFields = newData.getChangedFields().entrySet().iterator();
        while(changedFields.hasNext()) {
            
            Entry<String, String> updatedField = changedFields.next();
            String value = updatedField.getValue();
            String fieldName = updatedField.getKey();
            TextView field = holder.get(fieldName);
            
            if (field != null) {
                if (fieldName.equals("timestamp")) {
                    Date then = new Date(Long.parseLong(value));
                    value = dateFormat.format(then);
                }
                
                double upDown = 0.0;
                int color;
                if (!snapshot ) {
                    // update cell color 
                    if (numericField.contains(fieldName)) {
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
    
    private class ResetRunnable implements Runnable {
    	
    	public synchronized void run() {
    		resetHolder(holder, fields);
    	}
    	
    	private void resetHolder(HashMap<String,TextView> holder, String[] fields) {  
            for (int i=0; i<fields.length; i++) {
                
                TextView field = holder.get(fields[i]);
                if (field != null) {
                    field.setText("N/A");
                }
                
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
