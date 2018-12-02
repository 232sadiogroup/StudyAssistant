package com.teamtwo.studyassistant.pomodoro;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.teamtwo.studyassistant.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PomodoroSettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG = PomodoroSettingsActivity.class.getSimpleName();
    @BindView(R.id.work_duration_spinner)
    Spinner workDurationSpinner;
    @BindView(R.id.short_break_duration_spinner)
    Spinner shortBreakDurationSpinner;
    @BindView(R.id.long_break_duration_spinner)
    Spinner longBreakDurationSpinner;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pomodoro_activity_settings);

        ButterKnife.bind(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        initSpinner();
    }


    private void initSpinner() {
        // Create an array adapter for all three spinners using the string array
        ArrayAdapter<CharSequence> workDurationAdapter = ArrayAdapter.createFromResource(this,
                R.array.work_duration_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> shortBreakDurationAdapter = ArrayAdapter.createFromResource(this,
                R.array.short_break_duration_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> longBreakDurationAdapter = ArrayAdapter.createFromResource(this,
                R.array.long_break_duration_array, android.R.layout.simple_spinner_item);

        // Layout to use when list of choices appears
        workDurationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shortBreakDurationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        longBreakDurationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        workDurationSpinner.setAdapter(workDurationAdapter);
        shortBreakDurationSpinner.setAdapter(shortBreakDurationAdapter);
        longBreakDurationSpinner.setAdapter(longBreakDurationAdapter);

        // Set the default selection
        workDurationSpinner.setSelection(preferences.getInt(getString(R.string.work_duration_key), 1));
        shortBreakDurationSpinner.setSelection(preferences.getInt(getString(R.string.short_break_duration_key), 1));
        longBreakDurationSpinner.setSelection(preferences.getInt(getString(R.string.long_break_duration_key), 1));

        workDurationSpinner.setOnItemSelectedListener(this);
        shortBreakDurationSpinner.setOnItemSelectedListener(this);
        longBreakDurationSpinner.setOnItemSelectedListener(this);
    }

    /**
     * 保存 spinners 选择的位置到 SharedPreferences
     *
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // 初始化
        SharedPreferences.Editor editor = preferences.edit();
        switch (parent.getId()) {
            // pomodoro spinner 中选择项
            case R.id.work_duration_spinner:
                Log.v(LOG_TAG, (String) parent.getItemAtPosition(position));
                // 保存选择项当前状态
                editor.putInt(getString(R.string.work_duration_key), position);
                break;
            // SHORT_BREAK spinner 中选择项
            case R.id.short_break_duration_spinner:
                Log.v(LOG_TAG, (String) parent.getItemAtPosition(position));
                // 保存选择项当前状态
                editor.putInt(getString(R.string.short_break_duration_key), position);
                break;
            // LONG_BREAK spinner 中选择项
            case R.id.long_break_duration_spinner:
                Log.v(LOG_TAG, (String) parent.getItemAtPosition(position));
                // 保存选择项当前状态
                editor.putInt(getString(R.string.long_break_duration_key), position);
        }
        editor.apply();
    }

    /**
     * 没有在 Spinner 中选择任何东西
     *
     * @param parent
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
        //verridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}