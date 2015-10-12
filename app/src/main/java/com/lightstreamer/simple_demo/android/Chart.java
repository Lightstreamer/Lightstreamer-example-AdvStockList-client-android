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

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.Subscription;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Chart extends SimpleSubscriptionListener {

    //to avoid sycnhronizations and concurrency issues max, min and series must be only modified by the handler thread
    double maxY = 0;
    double minY = 0;
    private Series series;

    private XYPlot dynamicPlot;
    
    DecimalFormat df = new DecimalFormat("00");
    

    private Handler handler;
    
    private final static int MAX_SERIES_SIZE = 40;

    private final static String TAG = "Chart";
    
    public Chart(XYPlot dynamicPlot,Handler handler) {
    	super("Chart");
        this.series = new Series();
        
        this.handler = handler;
        
        this.setPlot(dynamicPlot);
    }

    private void setPlot(final XYPlot dynamicPlot) {
        if (this.dynamicPlot != dynamicPlot) {
            this.dynamicPlot = dynamicPlot;
            dynamicPlot.setDomainStep(XYStepMode.SUBDIVIDE, 4);
            dynamicPlot.setRangeStep(XYStepMode.SUBDIVIDE, 5);
            dynamicPlot.getLegendWidget().setVisible(false);
            
            dynamicPlot.getBackgroundPaint().setColor(Color.BLACK);
            dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.BLACK);
            dynamicPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
            
            dynamicPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.WHITE);
            dynamicPlot.getGraphWidget().getRangeLabelPaint().setColor(Color.WHITE);

            dynamicPlot.setRangeBoundaries(minY, maxY, BoundaryMode.FIXED);
            
            dynamicPlot.setDomainValueFormat(new FormatDateLabel());
        }
    }

    public void onResume(Context context) {
        PixelUtils.init(context);
        
        int line = context.getResources().getColor(R.color.chart_line);
        LineAndPointFormatter formatter = new LineAndPointFormatter(line, line, null, null);
        this.dynamicPlot.addSeries(series, formatter);

        this.clean();
    }
    
    public void onPause() {
        this.dynamicPlot.removeSeries(series);
    }
    
    @Override
    public void onListenStart(Subscription sub) {
        super.onListenStart(sub);
        this.clean(); 
    }
    
    @Override
    public void onItemUpdate(ItemUpdate update) {
    	super.onItemUpdate(update);
    
    	String lastPrice = update.getValue("last_price");
        String time = update.getValue("time");
        this.addPoint(time, lastPrice);
    }
    
    private void addPoint(final String time, final String lastPrice) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "New point");
                series.add(time, lastPrice);
            }
        });
        this.redraw();
    }
    
    private void clean() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Reset chart");
                series.reset();
                maxY = 0;
                minY = 0;
            }
        });
        this.redraw();
    }
    
    private void redraw() {
    	handler.post(new Runnable() {
            @Override
            public void run() {
                if (dynamicPlot != null) {
                    dynamicPlot.setRangeBoundaries(minY, maxY, BoundaryMode.FIXED);
                    Log.v(TAG, "Redraw chart");
                    dynamicPlot.redraw();
                }
            }
        });
    }
    
    private void onYOverflow(double last) {
        Log.d(TAG, "Y overflow detected");
        //XXX currently never shrinks
        int shift = 1;
        if (last > maxY) {
            double newMax = maxY + shift;
          if (last > newMax) {
            newMax = last;
          }

          this.maxY = newMax;

        } else if (last < minY) {
            double newMin = minY - shift;
          if (last < newMin) {
            newMin = last;
          }
          
          this.minY = newMin;
        }
        Log.i(TAG, "New Y boundaries: " + this.minY + " -> "+ this.maxY);
    }
    
    private void onFirstPoint(double newPrice) {
        Log.d(TAG, "First point on chart");
        minY = newPrice-1;
        if (minY < 0) {
            minY = 0;
        }
        maxY = newPrice+1;
        Log.i(TAG, "New Y boundaries: " + this.minY + " -> "+ this.maxY);
    }


    private class Series implements XYSeries {


        ArrayList<Number> prices = new ArrayList<>();
        ArrayList<Number> times = new ArrayList<>();
     
        @Override
        public String getTitle() {
            return "";
        }

        public void add(String time, String lastPrice) {
            if (prices.size() >= MAX_SERIES_SIZE) {
                prices.remove(0);
                times.remove(0);
            }
            
            double newPrice = Double.parseDouble(lastPrice);
            
            if (prices.size() == 0) {
                onFirstPoint(newPrice);
            }
            
            if (newPrice < minY || newPrice > maxY) {
                onYOverflow(newPrice);
            }
            
            prices.add(newPrice);
            
            String[] pieces = time.split(":");
            int intTime = Integer.parseInt(pieces[0])*60*60 + Integer.parseInt(pieces[1])*60 + Integer.parseInt(pieces[2]);
            times.add(intTime);
            
        }

        public void reset() {
            prices.clear();
            times.clear();
        }

        @Override
        public Number getX(int index) {
            Log.v(TAG,"Extract X");
            return times.get(index);
        }

        @Override
        public Number getY(int index) {
            Log.v(TAG,"Extract Y");
            return prices.get(index);
        }

        @Override
        public int size() {
            Log.v(TAG,"Extract size");
            return prices.size();
        }
        
    }

    @SuppressWarnings("serial")
    private class FormatDateLabel extends Format {
        @Override
        public StringBuffer format(Object object, StringBuffer buffer,
                FieldPosition field) {
            Number num = (Number) object;
            
            int val = num.intValue();
            
            buffer.append(df.format((long) TimeUnit.SECONDS.toHours(val)));
            buffer.append(':');
            buffer.append(df.format((long) TimeUnit.SECONDS.toMinutes(val) % 60));
            buffer.append(':');
            buffer.append(df.format(val%60));
            
            
            return buffer;
        }

        @Override
        public Object parseObject(String string, ParsePosition position) {
            return null;
        }
    }
    
    
}
