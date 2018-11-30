package com.teamtwo.studyassistant.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import com.teamtwo.studyassistant.Adapters.FragmentsTabAdapter;
import com.teamtwo.studyassistant.Fragments.FridayFragment;
import com.teamtwo.studyassistant.Fragments.MondayFragment;
import com.teamtwo.studyassistant.Fragments.SaturdayFragment;
import com.teamtwo.studyassistant.Fragments.SundayFragment;
import com.teamtwo.studyassistant.Fragments.ThursdayFragment;
import com.teamtwo.studyassistant.Fragments.TuesdayFragment;
import com.teamtwo.studyassistant.Fragments.WednesdayFragment;
import com.teamtwo.studyassistant.R;
import com.teamtwo.studyassistant.Utils.AlertDialogsHelper;


import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentsTabAdapter adapter;
    private ViewPager viewPager;
    private boolean switchSevenDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAll();
    }

    private void initAll() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setupFragments();
        setupCustomDialog();
        setupSevenDaysPref();

        if(switchSevenDays) changeFragments(true);
    }


    //获取当前星期数
    private int getDayOfWeek(){
        Calendar now = Calendar.getInstance();
        //一周第一天是否为星期天
        boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
        //获取周几
        int weekDay = now.get(Calendar.DAY_OF_WEEK);
        //若一周第一天为星期天，则-1
        if(isFirstSunday){
            weekDay = weekDay - 1;
            if(weekDay == 0){
                weekDay = 7;
            }
        }
        return weekDay;
    }

    //设置fragments
    private void setupFragments() {
        adapter = new FragmentsTabAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        adapter.addFragment(new MondayFragment(), getResources().getString(R.string.monday));
        adapter.addFragment(new TuesdayFragment(), getResources().getString(R.string.tuesday));
        adapter.addFragment(new WednesdayFragment(), getResources().getString(R.string.wednesday));
        adapter.addFragment(new ThursdayFragment(), getResources().getString(R.string.thursday));
        adapter.addFragment(new FridayFragment(), getResources().getString(R.string.friday));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(getDayOfWeek()-1, true);//设置当前星期
        tabLayout.setupWithViewPager(viewPager);
    }

    //设置为一周七天后重新设置fragments
    private void changeFragments(boolean isChecked) {
        if(isChecked) {
            TabLayout tabLayout = findViewById(R.id.tabLayout);
            adapter.addFragment(new SaturdayFragment(), getResources().getString(R.string.saturday));
            adapter.addFragment(new SundayFragment(), getResources().getString(R.string.sunday));
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(getDayOfWeek()-1, true);//设置当前星期
            tabLayout.setupWithViewPager(viewPager);
        } else {
            if(adapter.getFragmentList().size() > 5) {
                adapter.removeFragment(new SaturdayFragment(), 5);
                adapter.removeFragment(new SundayFragment(), 5);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupCustomDialog() {
        final View alertLayout = getLayoutInflater().inflate(R.layout.dialog_add_subject, null);
        AlertDialogsHelper.getAddSubjectDialog(MainActivity.this, alertLayout, adapter, viewPager);
    }

    private void setupSevenDaysPref() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        switchSevenDays = sharedPref.getBoolean(SettingsActivity.KEY_SEVEN_DAYS_SETTING, false);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.teachers:
                Intent teacher = new Intent(MainActivity.this, TeachersActivity.class);
                startActivity(teacher);
                return true;
            case R.id.homework:
                Intent homework = new Intent(MainActivity.this, HomeworksActivity.class);
                startActivity(homework);
                return true;
            case R.id.settings:
                Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.pomodoro:
                Intent pomodoro = new Intent(MainActivity.this, PomodoroActivity.class);
                startActivity(pomodoro);
                return true;
            default:
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
        }
    }


}
