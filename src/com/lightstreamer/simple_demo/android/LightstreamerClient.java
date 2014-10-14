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

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import com.lightstreamer.ls_client.ConnectionInfo;
import com.lightstreamer.ls_client.ConnectionListener;
import com.lightstreamer.ls_client.LSClient;
import com.lightstreamer.ls_client.PushConnException;
import com.lightstreamer.ls_client.PushServerException;
import com.lightstreamer.ls_client.PushUserException;
import com.lightstreamer.ls_client.SubscrException;
import com.lightstreamer.ls_client.SubscribedTableKey;


public class LightstreamerClient {
    
    public interface StatusChangeListener {
        public void onStatusChange(int status);
    }
    
    private static final String TAG = "LS_CONN";
    private static final String TAG_SUB = "LS_SUB";
    
    public static final int STALLED = 4;
    public static final int STREAMING = 2;
    public static final int POLLING = 3;
    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int WAITING = 5;
    
    private static String statusToString(int status){
        switch(status) {
        
            case STALLED: {
                return "STALLED";
            }
            case STREAMING: {
                return "STREAMING";
            }
            case POLLING: {
                return "POLLING";
            }
            case DISCONNECTED: {
                return "DISCONNECTED";
            }
            case CONNECTING: {
                return "CONNECTING";
            }
            case WAITING: {
                return "WAITING";
            }
            default: {
                return "Unexpected";
            }
        
        }
    }
    
    private LinkedList<Subscription> subscriptions = new LinkedList<Subscription>();

    private AtomicBoolean expectingConnected = new AtomicBoolean(false);
    
    private boolean connected = false; // do not get/set this outside the eventsThread

    final private ExecutorService eventsThread = Executors.newSingleThreadExecutor();
        //SubscriptionThread ConnectionThread ConnectionEvent
    
    final private ConnectionInfo cInfo = new ConnectionInfo();
    final private LSClient client = new LSClient();

    private ClientListener currentListener = null;
    
    private StatusChangeListener statusListener;

    private int status;

    private final AtomicInteger connId = new AtomicInteger(0);
    
    public int getStatus() {
        return status;
    }

    private void setStatus(int status, int connId) {
        if (connId != this.connId.get()) {
            //this means that this event is from an old connection
            return;
        }
        
        this.status = status;
        Log.i(TAG,connId + ": " + statusToString(this.status)); 
        if (this.statusListener != null) {
            this.statusListener.onStatusChange(this.status);
        }
    }

    public LightstreamerClient() {
         this.cInfo.adapter = "DEMO";
    }
    
    public void setServer(String pushServerUrl) {
        this.cInfo.pushServerUrl = pushServerUrl;
    }
    
    public void setListener(StatusChangeListener statusListener) {
        this.statusListener = statusListener;
    }
    
    public synchronized void start() {
        Log.d(TAG,"Connection enabled");
        if (expectingConnected.compareAndSet(false,true)) {
            this.startConnectionThread(false);
        }
    }
    
    public synchronized void stop(boolean applyPause) {
        Log.d(TAG,"Connection disabled");
        if (expectingConnected.compareAndSet(true,false)) {
            this.startConnectionThread(applyPause);
        }
    }    
    
    public synchronized void addSubscription(Subscription sub) {
        eventsThread.execute(new SubscriptionThread(sub,true));
    }
    public synchronized void removeSubscription(Subscription sub) {
        eventsThread.execute(new SubscriptionThread(sub,false));
    }
        
    private void startConnectionThread(boolean wait) {
        eventsThread.execute(new ConnectionThread(wait));
    }
    
    //ClientListener calls it through eventsThread
    private void changeStatus(int connId, boolean connected, int status) {
        if (connId != this.connId.get()) {
            //this means that this event is from an old connection
            return;
        }
        
        this.connected = connected;
        
        if (connected != expectingConnected.get()) {
            this.startConnectionThread(false);
        }
    }
    
    private class ConnectionThread implements Runnable { 
        
        boolean wait = false;
        
        public ConnectionThread(boolean wait) {
            this.wait = wait;
        }

        public void run() { //called from the eventsThread
            //expectingConnected can be changed by outside events
            
            if(this.wait) {
                //waits to see if the user/app changes its mind
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                }
            }
            
            while(connected != expectingConnected.get()) { 
                
                if (!connected) {
                    connId.incrementAndGet(); //this is the only increment
                    setStatus(CONNECTING,connId.get());

                    try {
                        currentListener = new ClientListener(connId.get());
                        client.openConnection(cInfo, currentListener);
                        Log.d(TAG,"Connecting success");
                        connected = true;
                        
                        resubscribeAll();

                    } catch (PushServerException e) {
                        Log.d(TAG,"Connection failed: " + e.getErrorCode() + ": " + e.getMessage());
                    } catch (PushUserException e) {
                        Log.d(TAG,"Connection refused: " + e.getErrorCode() + ": " + e.getMessage());
                    } catch (PushConnException e) {
                        Log.d(TAG,"Connection problems: " + e.getMessage());
                    }
                    
                    if (!connected) {
                        try {
                            setStatus(WAITING,connId.get());
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                        }
                    }
                    
                } else {
                    Log.v(TAG,"Disconnecting");
                    client.closeConnection();
                    setStatus(DISCONNECTED,connId.get());
                    currentListener = null;
                    connected = false;
                }
                
            }     
        }
    }
    
    private class ConnectionEvent implements Runnable {

        private final int connId;
        private final boolean connected;
        private final int status;

        public ConnectionEvent(int connId, boolean connected, int status) {
            this.connId = connId;
            this.connected = connected;
            this.status = status;
        }
        
        @Override
        public void run() {
            changeStatus(this.connId, this.connected, this.status);
        }
        
    }
    
    
    private class ClientListener implements ConnectionListener {

        private final int connId;
        private int lastConnectionStatus;

        public ClientListener(int connId) {
            this.connId = connId;
        }
        
        @Override
        public void onActivityWarning(boolean warn) {
            Log.d(TAG,connId + " onActivityWarning " + warn);
            if (warn) {
                setStatus(STALLED,this.connId);
                eventsThread.execute(new ConnectionEvent(this.connId,true,STALLED));
            } else {
                setStatus(this.lastConnectionStatus,this.connId);
                eventsThread.execute(new ConnectionEvent(this.connId,true,this.lastConnectionStatus));
            }
            
        }

        @Override
        public void onClose() {
            Log.d(TAG,connId + " onClose");
            setStatus(DISCONNECTED,this.connId);
            eventsThread.execute(new ConnectionEvent(this.connId,false,DISCONNECTED));
        }

        @Override
        public void onConnectionEstablished() {
            Log.d(TAG,connId + " onConnectionEstablished");
        }

        @Override
        public void onDataError(PushServerException pse) {
            Log.d(TAG,connId + " onDataError: " + pse.getErrorCode() + " -> " + pse.getMessage());
        }

        @Override
        public void onEnd(int cause) {
            Log.d(TAG,connId + " onEnd " + cause);
        }

        @Override
        public void onFailure(PushServerException pse) {
            Log.d(TAG,connId + " onFailure: " + pse.getErrorCode() + " -> " + pse.getMessage());
        }

        @Override
        public void onFailure(PushConnException pce) {
            Log.d(TAG,connId + " onFailure: " + pce.getMessage());
        }

        @Override
        public void onNewBytes(long num) {
            //Log.v(TAG,connId + " onNewBytes " + num);
        }

        @Override
        public void onSessionStarted(boolean isPolling) {
            Log.d(TAG,connId + " onSessionStarted; isPolling: " + isPolling);
            if (isPolling) {
                this.lastConnectionStatus = POLLING;
            } else {
                this.lastConnectionStatus = STREAMING;
            }
            setStatus(this.lastConnectionStatus,this.connId);
            eventsThread.execute(new ConnectionEvent(this.connId,true,this.lastConnectionStatus));
            
            
        }
        
    }
    

    
//Subscription handling    
    
    
    private void resubscribeAll() {
        if (subscriptions.size() == 0) {
            return;
        }
        
        Log.i(TAG_SUB,"Resubscribing " + subscriptions.size() + " subscriptions");
        
        ExecutorService tmpExecutor = Executors.newFixedThreadPool(subscriptions.size());
        
        //start batch
        try {
            client.batchRequests(subscriptions.size());

        } catch (SubscrException e) {
            //connection is closed, exit
            return;
        }
        
        for (Subscription sub : subscriptions) {
            tmpExecutor.execute(new BatchSubscriptionThread(sub));
        }
        
        
        //close batch
        client.closeBatch(); //should be useless
        
        tmpExecutor.shutdown();
        try {
            tmpExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
    }
    
    
    
    private class SubscriptionThread implements Runnable {

        Subscription sub;
        private boolean add;
        
        public SubscriptionThread(Subscription sub, boolean add) {
            this.sub = sub;
            this.add = add;
        }
        
        @Override
        public void run() {
            
            if (subscriptions.contains(sub)) {
                if (this.add) {
                    //already contained, exit now
                    Log.d(TAG_SUB,"Can't add subscription: Subscription already in: " + sub);
                    return;
                }
                
                Log.i(TAG_SUB,"Removing subscription " + sub);
                subscriptions.remove(sub);
               
            } else {
                if (!this.add) {
                    //already removed, exit now
                    Log.d(TAG_SUB,"Can't remove subscription: Subscription not in: " + sub);
                    return;
                }
                Log.i(TAG_SUB,"Adding subscription " + sub);
                subscriptions.add(sub);

            }
            
            
            if (connected && expectingConnected.get()) {
                if (this.add) {
                    doSubscription(sub);
                } else {
                    doUnsubscription(sub);
                }
            }
            
        }
        
    }
    
    private class BatchSubscriptionThread implements Runnable {
        
        Subscription sub;
        
        public BatchSubscriptionThread(Subscription sub) {
            this.sub = sub;
        }
        
        @Override
        public void run() {
            doSubscription(sub);
        }
        
    }
    
    private void doSubscription(Subscription sub) {
        
        Log.d(TAG_SUB,"Subscribing " + sub);
        
        try {
            SubscribedTableKey key = client.subscribeTable(sub.getTableInfo(), sub.getTableListener(), false);
            sub.setTableKey(key);
        } catch (SubscrException e) {
            Log.d(TAG_SUB,"Connection was closed: " + e.getMessage());
        } catch (PushServerException e) {
            Log.d(TAG_SUB,"Subscription failed: " + e.getErrorCode() + ": " + e.getMessage());
        } catch (PushUserException e) {
            Log.d(TAG_SUB,"Subscription refused: " + e.getErrorCode() + ": " + e.getMessage());
        } catch (PushConnException e) {
            Log.d(TAG_SUB,"Connection problems: " + e.getMessage());
        }
    }
    private void doUnsubscription(Subscription sub) {
        
        Log.d(TAG_SUB,"Unsubscribing " + sub);
        
        try {
            client.unsubscribeTable(sub.getTableKey());
        } catch (SubscrException e) {
            Log.d(TAG_SUB,"Connection was closed: " + e.getMessage());
        } catch (PushServerException e) {
            Log.wtf(TAG_SUB,"Unsubscription failed: " + e.getErrorCode() + ": " + e.getMessage());
        } catch (PushConnException e) {
            Log.d(TAG_SUB,"Unubscription failed: " + e.getMessage());
        }
    }

    public interface LightstreamerClientProxy {
        public void start();
        public void stop(boolean applyPause);
        public void addSubscription(Subscription sub);
        public void removeSubscription(Subscription sub);
   }
    
    
}
