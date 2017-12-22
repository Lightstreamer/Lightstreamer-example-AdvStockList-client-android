# Lightstreamer - Stock-List Demo - Android Client


This project contains a full example of an Android application that employs the [Lightstreamer Android Client library](http://www.lightstreamer.com/docs/client_android_api/index.html)
version 1 to subscribe to real-time updates.

An extended version, including support for GCM push notifications is also available: [Stock-List Demo with GCM Push Notifications - Android Client](https://github.com/Lightstreamer/Lightstreamer-example-AdvStockList-client-android/tree/for-client-1)


## Live Demo

[![screenshot](screen_android_large.png)](https://play.google.com/store/apps/details?id=com.lightstreamer.simple_demo.android)
 
[![tablet screenshot](screen_android_tablet.png)](https://play.google.com/store/apps/details?id=com.lightstreamer.simple_demo.android)

![QR](qrcode.png)

###[![](http://demos.lightstreamer.com/site/img/play.png) View live demo](https://play.google.com/store/apps/details?id=com.lightstreamer.simple_demo.android)
(To install the app from the Google Play Store, you can also use the above QR code)


## Details

This is a Java-for-Android version of the [Lightstreamer - Basic Stock-List Demo - HTML client](https://github.com/Lightstreamer/Lightstreamer-example-StockList-client-javascript#basic-stock-list-demo---html-client).<br>
This app uses the <b>Android Client API for Lightstreamer</b> version 1 to handle the communications with Lightstreamer Server. A simple user interface is implemented to display the real-time data received from Lightstreamer Server.<br>

Touch a row opens a new panel with the detailed information, updated in real-time, of the stock.

### Dig the Code

* `StockListDemo.java` is the entry point and only `Activity` of the application. It contains two `Fragment`s, the status of
the application, and acts as a proxy to the LightstreamerClient instance. The two `Fragment` are both visible if the application
runs on tablet; on the contrary, only one `Fragment` is visible and are exchanged based on the user interaction
* `LightstreamerClient.java` handles the connection to the Lightstreamer server and the Subscription/Unsubscription requests
issued by the various part of the application.
* `SubscriptionFragment.java` represents a `Fragment` containing a subscription that is started/stopped based on the lifecycle of 
the `Fragment`. Please note that this class does not actually extend `Fragment`.
* `StocksFragment.java` and `DetailsFragment.java` are the classes representing the two fragments of the application. 
* `Chart.java` wraps the AndroidPlot APIs to plot the real-time chart on the details fragment.


Check out the sources for further explanations.
  
*NOTE: Not all the functionalities of the Lightstreamer Android Java client are exposed by the classes listed above. You can easily expand those functionalities using the [Android Client API](http://www.lightstreamer.com/docs/client_android_api/index.html) as a reference. If in trouble check out the [specific Lightstreamer forum](http://forums.lightstreamer.com/forumdisplay.php?33-Android-Client-API).*

## Install

If you want to install a version of this demo pointing to your local Lightstreamer Server and running into 
an [Android Virtual Device](http://developer.android.com/tools/devices/emulator.html), follow these steps:

* Note that, as prerequisite, the [Lightstreamer - Stock-List Demo - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-Stocklist-adapter-java) 
has to be deployed on your local Lightstreamer Server instance. Please check out that project and follow the installation 
instructions provided with it. 
* Launch Lightstreamer Server.
* Download the `deploy.zip` file, which you can find in the latest [deploy release](https://github.com/Lightstreamer/Lightstreamer-example-StockList-client-android/releases) 
of this project for client version 1.x and extract the `Android_StockListDemo.apk` file.
* Locate or install the [Android SDK](http://developer.android.com/sdk/index.html)
* Install the `Android_StockListDemo.apk` in your emulator:
  * Execute the emulator (Android SDK/SDK Manager->Tools->Manage AVDs...->New then Start)
  * Open the console and move to the platform-tools folder of SDK directory.
  * Then type the following command:
    ```
    adb install [.apk path]
    ```
* Look up the demo in your virtual device and launch it.

*Note that the demo targets the Lightstreamer server @ http://10.0.2.2:8080 because 10.0.2.2 is the special alias to your host loopback interface.*

## Build

To build your own version of the demo, please consider that this example is comprised of the following folders:
* `/src` Contains the sources to build the Java-for-Android application.
* `/res` Contains the images and other resourced needed to the demo. 
* `/lib` Drop here the `ls-android-client.jar` from the Lighstreamer SDK for Android Clients and
`androidplot-core.jar` from the Androidplot library, to be used for the build process.

The demo has also references the [v7 Support Library](https://developer.android.com/tools/support-library/setup.html).
  
### Getting Started

You can import the sources on a new project on [Eclipse](http://www.eclipse.org/) (provided you installed the necessary
[ADT plugin](http://developer.android.com/sdk/eclipse-adt.html)) or on [Android Studio](https://developer.android.com/sdk/installing/studio.html).
In the former case, you'll need to separately download the [Android SDK](http://developer.android.com/sdk/).

Once the project has been imported, the `android-support-v7-appcompat` dependency has to be satisfied-
Please follow the related guide: [v7 Support Library](https://developer.android.com/tools/support-library/setup.html).

### Compile and Run

To run the demo, a suitable emulated or real device is required. To run the demo, you'll need at least android 2.3. 
To receive push notification you'll need a Google account configured on the system. In case the emulator is used a "Google APIs" 
OS image has to be used.

* On eclipse, right-click on the project in the Package Explorer and click Run As -> Android Application, then follow the instructions.
* On Android Studio, select Run from the menu and choose "Run", then follow the instructions

### Deploy
  
You may run the demo against your local server or using our online server at http://push.lightstreamer.com:80. The server to which the demo will connect to is configured in the `res/values/strings.xml` file.
In the former case, the example requires that the [QUOTE_ADAPTER](https://github.com/Lightstreamer/Lightstreamer-example-Stocklist-adapter-java) has to be deployed in your local Lightstreamer server instance;
the [LiteralBasedProvider](https://github.com/Lightstreamer/Lightstreamer-example-ReusableMetadata-adapter-java) is also needed, but it is already provided by Lightstreamer server.


## See Also

### Lightstreamer Adapters Needed by This Demo Client

* [Lightstreamer - Stock-List Demo - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-Stocklist-adapter-java)
* [Lightstreamer - Reusable Metadata Adapters - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-ReusableMetadata-adapter-java)

### Related Projects

* [Lightstreamer - Stock-List Demos - HTML Clients](https://github.com/Lightstreamer/Lightstreamer-example-Stocklist-client-javascript)
* [Lightstreamer - Stock-List Demo with GCM Push Notifications - Android Client](https://github.com/Lightstreamer/Lightstreamer-example-MPNStockList-client-android/tree/for-client-1)

## Lightstreamer Compatibility Notes

* Compatible with Lightstreamer Android Client API version 1.x.
* For Lightstreamer Allegro (+ Android Client API support), Presto, Vivace.
