package com.teamtwo.studyassistant.pomodoro;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.teamtwo.studyassistant.Activities.PomodoroActivity;
import com.teamtwo.studyassistant.R;
import com.teamtwo.studyassistant.pomodoro.utils.Utils;

import static com.teamtwo.studyassistant.pomodoro.utils.Constants.CHANNEL_ID;
import static com.teamtwo.studyassistant.pomodoro.utils.Constants.POMODORO;
import static com.teamtwo.studyassistant.pomodoro.utils.Constants.TASK_INFORMATION_NOTIFICATION_ID;


public class CountDownTimerService extends Service {
    public static final int ID = 1;
    public static final String COUNTDOWN_BROADCAST = "com.teamtwo.countdown";
    public static final String STOP_ACTION_BROADCAST = "com.teamtwo.stop.action";
    LocalBroadcastManager broadcaster;
    private CountDownTimer countDownTimer;
    private SharedPreferences preferences;
    private int newWorkSessionCount;
    private int currentlyRunningServiceType;

    public CountDownTimerService() {
    }

    @Override
    public void onCreate() {

        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //将通信通道返回给服务。
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long TIME_PERIOD = intent.getLongExtra("time_period", 0);
        long TIME_INTERVAL = intent.getLongExtra("time_interval", 0);

        Intent notificationIntent = new Intent(this, PomodoroActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("Pomodoro Countdown Timer")
                //.setColor(getResources().getColor(R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setContentText("Countdown timer is running")
//                .addAction(R.drawable.complete,"Complete",completeActionPendingIntent).setColor(Color.GREEN)
//                .addAction(R.drawable.cancel,"Cancel",cancelActionPendingIntent).setColor(Color.RED)
                .build();

        // 清除之前的所有通知。
        NotificationManagerCompat
                .from(this)
                .cancel(TASK_INFORMATION_NOTIFICATION_ID);

        startForeground(ID, notification);
        countDownTimerBuilder(TIME_PERIOD, TIME_INTERVAL).start();
        return START_REDELIVER_INTENT;
    }

    /**
     * @return 每秒更新一次时间
     */
    private CountDownTimer countDownTimerBuilder(long TIME_PERIOD, long TIME_INTERVAL) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences, getApplicationContext());
        countDownTimer = new CountDownTimer(TIME_PERIOD, TIME_INTERVAL) {
            @Override
            public void onTick(long timeInMilliSeconds) {
//                soundPool.play(tickID, 0.5f, 0.5f, 1, 0, 1f);

                String countDown = Utils.getCurrentDurationPreferenceStringFor(timeInMilliSeconds);

                broadcaster.sendBroadcast(
                        new Intent(COUNTDOWN_BROADCAST)
                                .putExtra("countDown", countDown));
            }

            @Override
            public void onFinish() {
                // 更新并检索WorkSessionCount的新值。
                if (currentlyRunningServiceType == POMODORO) {
                    newWorkSessionCount = Utils.updateWorkSessionCount(preferences, getApplicationContext());
                    // 获取下一步的休息类型，更新 service
                    currentlyRunningServiceType = Utils.getTypeOfBreak(preferences, getApplicationContext());
                } else {
                    // 如果currentRunningServiceType的最后一个值为SHORT_BREAK或LONG_BREAK，则将其设置回POMODORO
                    currentlyRunningServiceType = POMODORO;
                }

                newWorkSessionCount = preferences.getInt(getString(R.string.work_session_count_key), 0);
                // 如果currentRunningServiceType的最后一个值为SHORT_BREAK或LONG_BREAK，则将其设置回POMODORO
                Utils.updateCurrentlyRunningServiceType(preferences, getApplicationContext(), currentlyRunningServiceType);
                stopSelf();
                stoppedBroadcastIntent();
            }
        };
        return countDownTimer;
    }

    // 停止广播intent
    protected void stoppedBroadcastIntent() {
        broadcaster.sendBroadcast(
                new Intent(STOP_ACTION_BROADCAST)
                        .putExtra("workSessionCount", newWorkSessionCount));
    }
}
