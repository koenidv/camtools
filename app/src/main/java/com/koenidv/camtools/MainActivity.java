package com.koenidv.camtools;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private AHBottomNavigation mNavigation;

    protected ActionBar mActionBar;

    private boolean noUpdate = false;

    @SuppressWarnings("RedundantCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") final SharedPreferences.Editor prefsEdit = prefs.edit();

        FirebaseApp.initializeApp(getApplicationContext());
        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetch(mFirebaseRemoteConfig.getLong("config_cache"))
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        }
                    }
                });

        if (prefs.getBoolean("system_darkmode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        mNavigation = findViewById(R.id.navigation);
        mActionBar = getSupportActionBar();


        //ViewPager - Pages
        //
        mViewPager = findViewById(R.id.viewPager);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);


        //Navigation - Bottom bar
        AHBottomNavigationItem nav_sky = new AHBottomNavigationItem(R.string.title_sky_short, R.drawable.ic_sun, R.color.tab_sky);
        AHBottomNavigationItem nav_exposure = new AHBottomNavigationItem(R.string.title_exposure_short, R.drawable.ic_density, R.color.tab_exposure);
        AHBottomNavigationItem nav_focus = new AHBottomNavigationItem(R.string.title_focus_short, R.drawable.ic_focus, R.color.tab_focus);

        mNavigation.addItem(nav_sky);
        mNavigation.addItem(nav_exposure);
        mNavigation.addItem(nav_focus);
        // mNavigation.addItem(nav_settings);

        mNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mNavigation.setAccentColor(getResources().getColor(R.color.colorAccent));
        mNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        mNavigation.setElevation(10f);

        mViewPager.setCurrentItem(prefs.getInt("lastTab", 0));
        mNavigation.setCurrentItem(prefs.getInt("lastTab", 0));

        mNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener()

        {
            @SuppressWarnings("RedundantCast")
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (!noUpdate) {
                    mViewPager.setCurrentItem(position);
                }
                return true;
            }
        });


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()

        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                prefsEdit.putInt("lastTab", position).apply();

                Runnable r = new Runnable() {
                    @SuppressWarnings("RedundantCast")
                    @Override
                    public void run() {
                        if (mNavigation.getCurrentItem() != position) {
                            noUpdate = true;
                            mNavigation.setCurrentItem(position);
                            noUpdate = false;
                            invalidateOptionsMenu();
                        }
                    }
                };

                Handler mHandler = new Handler();
                mHandler.postDelayed(r, 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //                   // getItem is called to instantiate the fragment for the given page.
            // default           // Return a PlaceholderFragment (defined as a static inner class below).
            //                   return PlaceholderFragment.newInstance(position + 1);
            //TODO: Customizability
            switch (position) {
                case 0:
                    return new SkyFragment();
                case 1:
                    return new ExposureFragment();
                case 2:
                    return new FocusFragment();
                default:
                    return new SkyFragment();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_sky_long);
                case 1:
                    return getString(R.string.title_exposure_long);
                case 2:
                    return getString(R.string.title_focus_long);
                case 3:
                    return getString(R.string.title_settings_long);
            }
            return null;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                break;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_help:
                //startActivity(new Intent(MainActivity.this, FragmentActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
