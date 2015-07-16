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


import android.app.Application;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;

public class StockListDemoApplication extends Application {

    public static LightstreamerClientProxy client = null;

    @Override
    public void onCreate() {
        super.onCreate();

        client = new ClientProxy(); //expose the instance
    }


    private class ClientProxy implements LightstreamerClientProxy {
        private boolean connectionWish = false;
        private boolean userWantsConnection = true;
        private LightstreamerClient lsClient = new LightstreamerClient(null, "DEMO");

        public ClientProxy() {
            lsClient.connectionDetails.setServerAddress(getResources().getString(R.string.host));
            lsClient.connect();
        }

        @Override
        public boolean start(boolean userCall) {
            synchronized (lsClient) {
                if (!userCall) {
                    if (!userWantsConnection) {
                        return false;
                    }
                } else {
                    userWantsConnection = true;
                }

                connectionWish = true;
                lsClient.connect();
                return true;
            }
        }

        @Override
        public void stop(boolean userCall) {
            synchronized (lsClient) {
                connectionWish = false;

                if (userCall) {
                    userWantsConnection = false;
                    lsClient.disconnect();
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                            }
                            synchronized (lsClient) {
                                if (!connectionWish) {
                                    lsClient.disconnect();
                                }
                            }
                        }
                    }.start();
                }
            }
        }

        @Override
        public void addSubscription(Subscription sub) {
            lsClient.subscribe(sub);
        }

        @Override
        public void removeSubscription(Subscription sub) {
            lsClient.unsubscribe(sub);
        }


        @Override
        public void addListener(ClientListener listener) {
            lsClient.addListener(listener);
        }

        @Override
        public void removeListener(ClientListener listener) {
            lsClient.addListener(listener);
        }

        public String getStatus() {
            return lsClient.getStatus();
        }
    }
}
