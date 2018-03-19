package com.example.herma.lab2;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by herma on 19.03.2018.
 */

public class BackgroundUpdate extends IntentService
{
    public BackgroundUpdate(String name) {
        super(name);
    }

    public BackgroundUpdate() {
        super("DefaultName");
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
    }

    public Context context = null;
    public Handler handler = null;
    public Runnable runnable = null;
    int interval = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        int intervalChoice = sharedPreferences.getInt(getString(R.string.text_preference_interval), 0);

        switch (intervalChoice)
        {
            case 0:
            {
                interval = 600000;      // 10 minutes in milliseconds
                //interval = 10000;       //  10 seconds, for testing;
                break;
            }
            case 1:
            {
                interval = 3600000;     //  1 hour in milliseconds
                break;
            }
            case 2:
            {
                interval = 3600000 * 24;    //  24 hours in milliseconds
                break;
            }
        }

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                update();
                handler.postDelayed(runnable, interval);
            }
        };
        new Thread(runnable).start();
    }

    public void update()
    {
        MainActivity.updateLists(context);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //  Literally do nothing
    }
}
