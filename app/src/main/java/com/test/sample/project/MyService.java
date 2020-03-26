package com.test.sample.project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A Forground service with notification which runs a TimerTask or Handler every 100 milliseconds
 */
public class MyService extends Service {

    private final String TAG = "MyService";

    private final String CHANNEL_ID = "My_Channel";

    private final int NOTIFICATION_ID = 6123;

    private final int DELAY = 100;

    private HandlerThread myHandlerThread;
    private Handler myBackgroundHandler;

    private long timePrevious = 0;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationOreo();
        } else {
            notificationPreOreo();
        }


        //test on this
        runHandler();

        //or test on this
//        runTimerTask();

    }


    /**
     * it will run TimerTask after every 100 milliseconds (ms)
     */
    private void runTimerTask() {
        Timer task = new Timer();
        task.schedule(timerTask, DELAY, DELAY);
    }

    /**
     * it will run Handler after every 100 milliseconds (ms)
     */
    private void runHandler() {
        myHandlerThread = new HandlerThread("MyThread");
        myHandlerThread.start();
        myBackgroundHandler = new Handler(myHandlerThread.getLooper());

        myBackgroundHandler.post(runnable);


    }

    /**
     * Huawei randomly stops this runnable for 3064 milliseconds when we open another app
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            logDelay();

            myBackgroundHandler.postDelayed(this, DELAY);

        }
    };



    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

            logDelay();
        }
    };

    /**
     * this Run Method should be called after every 100 milliseconds
     * To check when it is called just copy the Log TAG in the Logcat which is "MyService"
     */
    private void logDelay() {

        //checking here if it is called within 100 ms
        long current = System.currentTimeMillis();
        long dif = current - timePrevious;
        Log.d(TAG, "run after: " + dif);

        //here i am checking if this method is called after 120 milliseconds then definitely Huawei has stopped it calling previously and that's the problem
        if (dif > 120) {
            Log.e(TAG, "Something is wrong with this delay: " + dif);
        }

        timePrevious = current;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void notificationOreo() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Our Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(notificationChannel);


        Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(null)
                .setSubText("Service is running")
                .setSmallIcon(R.drawable.service_icon)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void notificationPreOreo() {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.service_icon)
                .setContentTitle("Service is running")
                .setContentText("Yes running");

        startForeground(NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (myBackgroundHandler != null && myHandlerThread != null) {
            myBackgroundHandler.removeCallbacksAndMessages(null);
            myBackgroundHandler.getLooper().quit();
            myHandlerThread.quit();
        }


        Log.d(TAG, "onDestroy: ");
        stopForeground(true);
    }
}
