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


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lightstreamer.client.ClientListener;


public class StockListDemo extends AppCompatActivity implements
    StocksFragment.onStockSelectedListener {

    private static final String TAG = "StockListDemo";

    private ClientListener currentListener = new LSClientListener();
    private boolean pnEnabled = false;
    private boolean isConnectionExpected = false;
    
    private GestureDetectorCompat mDetector;

    LightstreamerClientProxy client;
    
    
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = StockListDemoApplication.client;

        GestureControls gs = new GestureControls();
        mDetector = new GestureDetectorCompat(this,gs);
        mDetector.setOnDoubleTapListener(gs);

        this.handler = new Handler();
        
        getSupportActionBar().setTitle(R.string.lightstreamer);
        
        setContentView(R.layout.stocks);

        if (findViewById(R.id.fragment_container) != null) {
            
            //single fragment view (phone)

            if (savedInstanceState != null) {
                return;
            }

            StocksFragment firstFragment = new StocksFragment();

            firstFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        } 

        
        
    }

    private int getIntentItem() {
        int openItem = 0;
        Intent launchIntent = getIntent();
        if (launchIntent != null) {
            Bundle extras = launchIntent.getExtras();
            if (extras != null) {
                openItem = extras.getInt("itemNum");
            }
        }
        return openItem;
    }
    
    @Override 
    public void onNewIntent(Intent intent) {
        Log.d(TAG, "New intent received");
        setIntent(intent);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        client.removeListener(currentListener);
        client.stop(false);
    }
    
    @Override 
    public void onResume() {
        super.onResume();

        client.addListener(this.currentListener);
        isConnectionExpected = client.start(false);

        int openItem = getIntentItem();
        if (openItem == 0 && findViewById(R.id.fragment_container) == null) {
            //tablet, always start with an open stock
            DetailsFragment df = getDetailsFragment();
            if (df != null) {
                openItem = df.getCurrentStock();
            }
            
            if (openItem == 0) {
                openItem = 2;
            }
        }
        
        if (openItem != 0) {
            onStockSelected(openItem);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.v(TAG,"Switch button: " + isConnectionExpected);
        menu.findItem(R.id.start).setVisible(!isConnectionExpected);
        menu.findItem(R.id.stop).setVisible(isConnectionExpected);
        
        return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.stop) {
            Log.i(TAG,"Stop");
            supportInvalidateOptionsMenu();
            client.stop(true);
            isConnectionExpected = false;
            return true;
        } else if (itemId == R.id.start) {
            Log.i(TAG,"Start");
            supportInvalidateOptionsMenu();
            client.start(true);
            isConnectionExpected = true;
            return true;
        } else if (itemId == R.id.about) {
            new AboutDialog().show(getSupportFragmentManager(), null);
            
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    
    private DetailsFragment getDetailsFragment() {
        DetailsFragment detailsFrag = (DetailsFragment)
                getSupportFragmentManager().findFragmentById(R.id.details_fragment);
        
        
        if (detailsFrag == null) {
            //phones
            detailsFrag = (DetailsFragment)getSupportFragmentManager().findFragmentByTag("DETAILS_FRAGMENT");
        } // else tablets
        
        return detailsFrag;
    }
    
    @Override
    public void onStockSelected(int item) {
        Log.v(TAG, "Stock detail selected");

        DetailsFragment detailsFrag = getDetailsFragment();
        
        if (detailsFrag != null) {
            //tablets
            detailsFrag.updateStocksView(item);

        } else {
            DetailsFragment newFragment = new DetailsFragment();
            Bundle args = new Bundle();
            args.putInt(DetailsFragment.ARG_ITEM, item);
            args.putBoolean(DetailsFragment.ARG_PN_CONTROLS, pnEnabled);
            newFragment.setArguments(args);
            
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);

            transaction.replace(R.id.fragment_container, newFragment,"DETAILS_FRAGMENT");
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public class LSClientListener implements ClientListener {

        @Override
        public void onListenEnd(com.lightstreamer.client.LightstreamerClient arg0) {
        }

        @Override
        public void onListenStart(com.lightstreamer.client.LightstreamerClient client) {
            this.onStatusChange(client.getStatus());
            
        }

        @Override
        public void onPropertyChange(String arg0) {
        }

        @Override
        public void onServerError(int code, String message) {
            Log.e(TAG,"Error "+code+": "+message);
        }

        @Override
        public void onStatusChange(String status) {
            handler.post(new StatusChange(status));
        }
        
    }
    
    
    private class StatusChange implements Runnable {

        private String status;

        public StatusChange(String status) {
            this.status = status;
        }
        
        private void applyStatus(int statusId, int textId) {
            ImageView statusIcon = (ImageView) findViewById(R.id.status_image);
            TextView textStatus = (TextView) findViewById(R.id.text_status);
            
            
            statusIcon.setContentDescription(getResources().getString(textId));
            statusIcon.setImageResource(statusId);
            textStatus.setText(getResources().getString(textId));
            
            
        }

        @Override
        public void run() {
            
            
            switch(status) {
            
                case "CONNECTING":
                    applyStatus(R.drawable.status_disconnected,R.string.status_connecting);
                    break;

                case "CONNECTED:STREAM-SENSING":
                    applyStatus(R.drawable.status_connected_polling,R.string.status_connecting);
                    break;

                case "DISCONNECTED":
                    applyStatus(R.drawable.status_disconnected,R.string.status_disconnected);
                    break;
                case "DISCONNECTED:WILL-RETRY":
                    applyStatus(R.drawable.status_disconnected,R.string.status_waiting);
                    break;
                
                case "CONNECTED:HTTP-STREAMING":
                    applyStatus(R.drawable.status_connected_streaming,R.string.status_streaming);
                    break;
                case "CONNECTED:WS-STREAMING":
                    applyStatus(R.drawable.status_connected_streaming,R.string.status_ws_streaming);
                    break;
                
                case "CONNECTED:HTTP-POLLING":
                    applyStatus(R.drawable.status_connected_polling,R.string.status_polling);
                    break;
                case "CONNECTED:WS-POLLING":
                    applyStatus(R.drawable.status_connected_polling,R.string.status_ws_polling);
                    break;
                    
                case "DISCONNECTED:TRYING-RECOVERY":
                    applyStatus(R.drawable.status_stalled,R.string.status_stalled);
                    break;
                case "STALLED":
                    applyStatus(R.drawable.status_stalled,R.string.status_stalled);
                    break;
                
                default: 
                    Log.wtf(TAG, "Received unexpected connection status: " + status);
                    return;
                
            }
        }
        
    }
    
    @Override 
    public boolean onTouchEvent(MotionEvent event){ 
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }
    
    public static class AboutDialog extends DialogFragment {
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.dialog_about, null)).setPositiveButton("OK", null);
            return builder.create();
        }
        
    }

 //we simply use this class to listen for double taps in which case we reveal/hide
    //a textual version of the connection status
    private class GestureControls extends 
        GestureDetector.SimpleOnGestureListener implements  
        GestureDetector.OnDoubleTapListener  {
            
        @Override
        public boolean onDown(MotionEvent event) { 
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            
            //toggleContainer.setVisibility(show ? View.VISIBLE : View.GONE);
            TextView textStatus = (TextView) findViewById(R.id.text_status);
            textStatus.setVisibility(textStatus.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            
            return true;
        }
    
      
    }
    
}
