package com.teamtwo.studyassistant.pomodoro.utils;

import android.content.Context;
import android.content.SharedPreferences;


import com.teamtwo.studyassistant.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;



import static com.teamtwo.studyassistant.pomodoro.utils.Constants.LONG_BREAK;
import static com.teamtwo.studyassistant.pomodoro.utils.Constants.POMODORO;
import static com.teamtwo.studyassistant.pomodoro.utils.Constants.SHORT_BREAK;


public class Utils {

    /**
     * * WorkSessionCount 加一并写到 SharedPreferences
     *
     * @return WorkSessionCount 更改后的值
     */
    public static int updateWorkSessionCount(SharedPreferences preferences, Context context) {

        int oldWorkSessionCount = preferences.getInt(context.getString(R.string.work_session_count_key), 0);

        int newWorkSessionCount = ++oldWorkSessionCount;
        preferences
                .edit()
                .putInt(context.getString(R.string.work_session_count_key), newWorkSessionCount)
                .apply();

        return newWorkSessionCount;
    }

    /**
     * 返回用户应该进行休息的类型（大休息 or 短休息）
     *
     */
    public static int getTypeOfBreak(SharedPreferences preferences, Context context) {
        int currentWorkSessionCount = preferences.getInt(context.getString(R.string.work_session_count_key), 0);
        if (currentWorkSessionCount % 4 == 0)
            return LONG_BREAK;
        return SHORT_BREAK;
    }

    public static void updateCurrentlyRunningServiceType(SharedPreferences preferences, Context context, int currentlyRunningServiceType) {
        preferences
                .edit()
                .putInt(context.getString(R.string.currently_running_service_type_key), currentlyRunningServiceType)
                .apply();
    }

    public static int retrieveCurrentlyRunningServiceType(SharedPreferences preferences, Context context) {
        return preferences.getInt(context.getString(R.string.currently_running_service_type_key), 0);
    }

    /**
     * 获取当前 POMODORO, SHORT_BREAK and LONG_BREAK 的值.
     * 返回形式为毫秒
     *
     */
    public static long getCurrentDurationPreferenceOf(SharedPreferences preferences, Context context, int currentlyRunningServiceType) {
        if (currentlyRunningServiceType == POMODORO) {
            int currentWorkDurationPreference = preferences.getInt(context.getString(R.string.work_duration_key), 1);

            switch (currentWorkDurationPreference) {
                case 0:
                    //return 20 * 60000; // 20 minutes
                    return 10000; // 10s
                case 1:
                    return 25 * 60000; // 25 minutes
                case 2:
                    return 30 * 60000; // 30 minutes
                case 3:
                    return 40 * 60000; // 40 minutes
                case 4:
                    return 55 * 60000; // 55 minutes
            }
        } else if (currentlyRunningServiceType == SHORT_BREAK) {
            int currentShortBreakDurationPreference = preferences.getInt(context.getString(R.string.short_break_duration_key), 1);

            switch (currentShortBreakDurationPreference) {
                case 0:
                    return 3 * 60000; // 3 minutes
                    //return 10000; // 10s
                case 1:
                    return 5 * 60000; // 5 minutes
                case 2:
                    return 10 * 60000; // 10 minutes
                case 3:
                    return 15 * 60000; // 15 minutes
            }
        } else if (currentlyRunningServiceType == LONG_BREAK) {
            int currentLongBreakDurationPreference = preferences.getInt(context.getString(R.string.long_break_duration_key), 1);

            switch (currentLongBreakDurationPreference) {
                case 0:
                    return 10 * 60000; // 10 minutes
                case 1:
                    return 15 * 60000; // 15 minutes
                case 2:
                    return 20 * 60000; // 20 minutes
                case 3:
                    return 25 * 60000; // 25 minutes
            }
        }
        return 0;
    }

    public static String getCurrentDurationPreferenceStringFor(long duration) {
        // https://stackoverflow.com/a/41589025/8411356
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration) % 60,
                TimeUnit.MILLISECONDS.toSeconds(duration) % 60);
    }
}