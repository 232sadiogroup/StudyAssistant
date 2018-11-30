package com.teamtwo.studyassistant.pomodoro;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.teamtwo.studyassistant.R;

import java.util.Objects;

import static com.teamtwo.studyassistant.pomodoro.utils.Constants.CHANNEL_ID;
import static com.teamtwo.studyassistant.pomodoro.utils.Constants.CHANNEL_ID_ONCOMPLETE;
import static com.teamtwo.studyassistant.pomodoro.utils.Constants.CHANNEL_NAME_ONCOMPLETE;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        createNotificationChannelComplete();
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getResources().getString(R.string.pomodoro),
                    NotificationManager.IMPORTANCE_LOW);

            NotificationManager notificationManager = Objects.requireNonNull(
                    getSystemService(NotificationManager.class));

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void createNotificationChannelComplete(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID_ONCOMPLETE,
                    CHANNEL_NAME_ONCOMPLETE,
                    NotificationManager.IMPORTANCE_HIGH);

            NotificationManager notificationManager = Objects.requireNonNull(
                    getSystemService(NotificationManager.class));

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
