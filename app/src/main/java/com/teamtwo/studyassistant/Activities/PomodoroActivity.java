package com.teamtwo.studyassistant.Activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.teamtwo.studyassistant.R;
import com.teamtwo.studyassistant.pomodoro.CountDownTimerService;
import com.teamtwo.studyassistant.pomodoro.PomodoroSettingsActivity;
import com.teamtwo.studyassistant.pomodoro.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;


import static com.teamtwo.studyassistant.pomodoro.utils.Constants.CHANNEL_ID_ONCOMPLETE;
import static com.teamtwo.studyassistant.pomodoro.utils.Constants.LONG_BREAK;
import static com.teamtwo.studyassistant.pomodoro.utils.Constants.POMODORO;
import static com.teamtwo.studyassistant.pomodoro.utils.Constants.SHORT_BREAK;
import static com.teamtwo.studyassistant.pomodoro.utils.Constants.TASK_INFORMATION_NOTIFICATION_ID;


public class PomodoroActivity extends AppCompatActivity implements View.OnClickListener {

    private static final long TIME_INTERVAL = 1000; // Time Interval is 1 second

    BroadcastReceiver stoppedIntentReceiver;
    BroadcastReceiver countDownReceiver;
    @BindView(R.id.settings_imageview_main)
    ImageView settingsImageView;
    @BindView(R.id.timer_button_main)
    ToggleButton timerButton;
    @BindView(R.id.countdown_textview_main)
    TextView countDownTextView;
    @BindView(R.id.session_completed_value_textview_main)
    TextView workSessionCompletedTextView;
    @BindView(R.id.finish_imageview_main)
    ImageView finishImageView;

    private int currentlyRunningServiceType; // 服务的类型分为 porodoro, short break 和 long break
    private long workDuration;
    private String workDurationString;
    private long shortBreakDuration;
    private String shortBreakDurationString;
    private long longBreakDuration;
    private String longBreakDurationString;
    private SharedPreferences preferences;
    private int workSessionCount = 0;
    private AlertDialog alertDialog;
    private boolean isAppVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pomodoro_activity_main);

        isAppVisible = true;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ButterKnife.bind(this);
        settingsImageView.setOnClickListener(this);
        timerButton.setOnClickListener(this);
        finishImageView.setOnClickListener(this);

        timerButton.setChecked(isServiceRunning(CountDownTimerService.class));

        // 接收倒计时停止的 broadcast
        stoppedIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                timerButton.setChecked(false);

                if (intent.getExtras() != null) {
                    workSessionCount = intent.getExtras().getInt("workSessionCount");
                    workSessionCompletedTextView.setText(String.valueOf(workSessionCount));
                }

                currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences, getApplicationContext());

                changeToggleButtonStateText(currentlyRunningServiceType);
                unregisterLocalBroadcastReceivers();
                alertDialog = createPomodoroCompletionAlertDialog();
                displayPomodoroCompletionAlertDialog();
                displayTaskInformationNotification();
            }
        };

        countDownReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras() != null)
                    countDownTextView.setText(intent.getExtras().getString("countDown"));
            }
        };

        // 从 sharedpreferences get Pomorodo SHORT_BREAK,LONG_BREAK 的时间
        workDuration = Utils.getCurrentDurationPreferenceOf(preferences, this, POMODORO);
        shortBreakDuration = Utils.getCurrentDurationPreferenceOf(preferences, this, SHORT_BREAK);
        longBreakDuration = Utils.getCurrentDurationPreferenceOf(preferences, this, LONG_BREAK);

        // 将时间换算成毫秒
        workDurationString = Utils.getCurrentDurationPreferenceStringFor(workDuration);
        shortBreakDurationString = Utils.getCurrentDurationPreferenceStringFor(shortBreakDuration);
        longBreakDurationString = Utils.getCurrentDurationPreferenceStringFor(longBreakDuration);

        currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences, this);
        changeToggleButtonStateText(currentlyRunningServiceType);

        // 设置当前完成的 WorkSession 的数量
        workSessionCount = preferences.getInt(getString(R.string.work_session_count_key), 0);
        workSessionCompletedTextView.setText(String.valueOf(workSessionCount));

        alertDialog = createPomodoroCompletionAlertDialog();
        displayPomodoroCompletionAlertDialog();


    }

    @Override
    protected void onStart() {
        isAppVisible = true;
        currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences, this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        isAppVisible = true;
        registerLocalBroadcastReceivers();

        alertDialog = createPomodoroCompletionAlertDialog();
        displayPomodoroCompletionAlertDialog();
        super.onResume();
    }

    @Override
    protected void onPause() {
        isAppVisible = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        isAppVisible = false;
        if (!isServiceRunning(CountDownTimerService.class)) {
            unregisterLocalBroadcastReceivers();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        isAppVisible = false;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        registerLocalBroadcastReceivers();

        // 从 SharedPreferences 获得 currentRunningServiceType
        currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences, this);

        switch (v.getId()) {

            // 点击设置按钮
            case R.id.settings_imageview_main:
                // 打开 PomodoroSettingsActivity
                Intent intent = new Intent(PomodoroActivity.this, PomodoroSettingsActivity.class);
                startActivity(intent);
                break;



            case R.id.timer_button_main:
                if (currentlyRunningServiceType == POMODORO) {
                    if (timerButton.isChecked()) {
                        startTimer(workDuration);
                    } else {
                        // 点击停止番茄则停止服务并重置 toggleButton 状态
                        switchToPomodoro();
                    }
                } else if (currentlyRunningServiceType == SHORT_BREAK) {
                    if (timerButton.isChecked()) {
                        startTimer(shortBreakDuration);
                    } else {
                        // 点击跳过休息则停止服务并重置 toggleButton 状态
                        switchToPomodoro();
                    }
                } else if (currentlyRunningServiceType == LONG_BREAK) {
                    if (timerButton.isChecked()) {
                        startTimer(longBreakDuration);
                    } else {
                        // 点击跳过大休息则停止服务并重置 toggleButton 状态
                        switchToPomodoro();
                    }
                }
                break;

            case R.id.finish_imageview_main:
                if (timerButton.isChecked()) {
                    // 完成按钮->停止服务并将 currentlyRunningServiceType 设置为 SHORT_BREAK 或 LONG_BREAK
                    if (currentlyRunningServiceType == POMODORO) {

                        // 更新完成番茄数并显示
                        int newWorkSessionCount = Utils.updateWorkSessionCount(preferences, this);
                        workSessionCompletedTextView.setText(String.valueOf(newWorkSessionCount));

                        currentlyRunningServiceType = Utils.getTypeOfBreak(preferences, this);
                        Utils.updateCurrentlyRunningServiceType(preferences, this, currentlyRunningServiceType);

                        long duration = Utils.getCurrentDurationPreferenceOf(preferences, this, currentlyRunningServiceType);
                        stopTimer(Utils.getCurrentDurationPreferenceStringFor(duration));
                        changeToggleButtonStateText(currentlyRunningServiceType);
                        unregisterLocalBroadcastReceivers();
                        alertDialog = createPomodoroCompletionAlertDialog();
                        displayPomodoroCompletionAlertDialog();
                        displayTaskInformationNotification();
                    }
                }
                break;
        }
    }


    /**
     * 根据 duration 的值启动相应 service 和 CountDownTimer
     *
     * @param duration 为倒计时时间
     * */
    private void startTimer(long duration) {
        Intent serviceIntent = new Intent(this, CountDownTimerService.class);
        serviceIntent.putExtra("time_period", duration);
        serviceIntent.putExtra("time_interval", TIME_INTERVAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(serviceIntent);
        else
            startService(serviceIntent);
    }

    /**
     * 停止服务并重置 CountDownTimer
     *
     * @param duration 为倒计时时长
     */
    private void stopTimer(String duration) {
        Intent serviceIntent = new Intent(getApplicationContext(), CountDownTimerService.class);
        stopService(serviceIntent);
        countDownTextView.setText(duration);
    }

    /**
     * 根据 currentRunningServiceType 更改「开始番茄钟」按钮文字并重置 CountDownTimer
     *
     * @param currentlyRunningServiceType 包含 POMODORO, SHORT_BREAK 或 LONG_BREAK 类型.
     */
    private void changeToggleButtonStateText(int currentlyRunningServiceType) {
        timerButton.setChecked(isServiceRunning(CountDownTimerService.class));
        if (currentlyRunningServiceType == POMODORO) {
            timerButton.setTextOn(getString(R.string.cancel_pomodoro));
            timerButton.setTextOff(getString(R.string.start_pomodoro));
            countDownTextView.setText(workDurationString);
        } else if (currentlyRunningServiceType == SHORT_BREAK) {
            timerButton.setTextOn(getString(R.string.skip_short_break));
            timerButton.setTextOff(getString(R.string.start_short_break));
            countDownTextView.setText(shortBreakDurationString);
        } else if (currentlyRunningServiceType == LONG_BREAK) {
            timerButton.setTextOn(getString(R.string.skip_long_break));
            timerButton.setTextOff(getString(R.string.start_long_break));
            countDownTextView.setText(longBreakDurationString);
        }

        timerButton.setChecked(timerButton.isChecked());
    }

    /**
     * Register LocalBroadcastReceivers.
     */
    private void registerLocalBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver((stoppedIntentReceiver),
                new IntentFilter(CountDownTimerService.STOP_ACTION_BROADCAST));
        LocalBroadcastManager.getInstance(this).registerReceiver((countDownReceiver),
                new IntentFilter(CountDownTimerService.COUNTDOWN_BROADCAST));
    }

    /**
     * Unregister LocalBroadcastReceivers.
     */
    private void unregisterLocalBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stoppedIntentReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(countDownReceiver);
    }

    /**
     * 短/长休息后换到工作状态
     */
    private void switchToPomodoro() {
        stopTimer(workDurationString);

        // Clearing any previous notifications.
        NotificationManagerCompat
                .from(getApplicationContext())
                .cancel(TASK_INFORMATION_NOTIFICATION_ID);

        if (alertDialog != null)
            alertDialog.cancel();
        currentlyRunningServiceType = POMODORO;
        Utils.updateCurrentlyRunningServiceType(preferences, this, currentlyRunningServiceType);
        currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences, this);
        changeToggleButtonStateText(currentlyRunningServiceType);
        unregisterLocalBroadcastReceivers();
    }

    /**
     * 检查服务是否运行
     *
     * @param serviceClass Service 类名
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置一个番茄完成之后的对话框
     *
     * @return alert-dialog
     */
    private AlertDialog createPomodoroCompletionAlertDialog() {
        if (alertDialog != null)
            alertDialog.cancel();

        View alertDialogLayout = View.inflate(getApplicationContext(), R.layout.pomodoro_layout_alert_dialog, null);
        final Button startBreakLargeButton = alertDialogLayout.findViewById(R.id.start_break);
        final Button startOtherBreakMediumButton = alertDialogLayout.findViewById(R.id.start_other_break);
        Button skipBreakSmallButton = alertDialogLayout.findViewById(R.id.skip_break);

        if (currentlyRunningServiceType == SHORT_BREAK) {
            startBreakLargeButton.setText(R.string.start_short_break);
            startOtherBreakMediumButton.setText(R.string.start_long_break);
        } else if (currentlyRunningServiceType == LONG_BREAK) {
            startBreakLargeButton.setText(R.string.start_long_break);
            startOtherBreakMediumButton.setText(R.string.start_short_break);
        }

        startBreakLargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentButtonText = startBreakLargeButton.getText().toString();
                startBreakFromAlertDialog(currentButtonText);
            }
        });

        startOtherBreakMediumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentButtonText = startOtherBreakMediumButton.getText().toString();
                startBreakFromAlertDialog(currentButtonText);
            }
        });

        skipBreakSmallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToPomodoro();
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(alertDialogLayout);
        alertDialogBuilder.setCancelable(false);
        return alertDialogBuilder.create();
    }


    private void displayPomodoroCompletionAlertDialog() {
        if (currentlyRunningServiceType != POMODORO && isAppVisible && !alertDialog.isShowing() && !isServiceRunning(CountDownTimerService.class)) {
            alertDialog.show();
        }
    }


    private void startBreakFromAlertDialog(String currentButtonText) {
        long breakDuration = 0;
        if (currentButtonText.equals(getString(R.string.start_long_break))) {
            Utils.updateCurrentlyRunningServiceType(preferences, getApplicationContext(), LONG_BREAK);
            breakDuration = longBreakDuration;
        } else if (currentButtonText.equals(getString(R.string.start_short_break))) {
            Utils.updateCurrentlyRunningServiceType(preferences, getApplicationContext(), SHORT_BREAK);
            breakDuration = shortBreakDuration;
        }

        currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences, getApplicationContext());
        if (alertDialog != null)
            alertDialog.cancel();
        registerLocalBroadcastReceivers();
        changeToggleButtonStateText(currentlyRunningServiceType);
        startTimer(breakDuration);
        timerButton.setChecked(isServiceRunning(CountDownTimerService.class));
    }

    /**
     * 任何一个倒计时完成则显示通知
     *
     * @return notification.
     */
    private NotificationCompat.Builder createTaskInformationNotification() {
        Intent notificationIntent = new Intent(this, PomodoroActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        String notificationContentText;

        if (currentlyRunningServiceType == POMODORO)
            notificationContentText = getString(R.string.start_pomodoro);
        else
            notificationContentText = getString(R.string.pomodoro_completion_alert_message);

        return new NotificationCompat.Builder(this, CHANNEL_ID_ONCOMPLETE)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("Pomodoro Countdown Timer")
                .setContentIntent(pendingIntent)
                .setContentText(notificationContentText)
                .setAutoCancel(true);
    }

    /**
     * foreground service 完成则显示通知
     */
    private void displayTaskInformationNotification() {
        Notification notification = createTaskInformationNotification().build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat
                .from(this);

        // Clearing any previous notifications.
        notificationManagerCompat
                .cancel(TASK_INFORMATION_NOTIFICATION_ID);

        // Displays a notification.
        if (!isServiceRunning(CountDownTimerService.class)) {
            notificationManagerCompat
                    .notify(TASK_INFORMATION_NOTIFICATION_ID, notification);
        }
    }


}
