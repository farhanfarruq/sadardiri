package com.example.sadardiri.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sadardiri.R;
import com.example.sadardiri.fragment.DashboardFragment;
import com.example.sadardiri.fragment.FinanceFragment;
import com.example.sadardiri.fragment.HabitsFragment;
import com.example.sadardiri.fragment.ReportsFragment;
import com.example.sadardiri.fragment.SavingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Tema
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isNightMode = prefs.getBoolean("night_mode", false);
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        MainPagerAdapter pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        viewPager.setPageTransformer((page, position) -> {
            float MIN_SCALE = 0.85f;
            float MIN_ALPHA = 0.5f;
            if (position < -1) {
                page.setAlpha(0f);
            } else if (position <= 1) {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else {
                page.setAlpha(0f);
            }
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) viewPager.setCurrentItem(0);
            else if (id == R.id.nav_finance) viewPager.setCurrentItem(1);
            else if (id == R.id.nav_savings) viewPager.setCurrentItem(2);
            else if (id == R.id.nav_habits) viewPager.setCurrentItem(3);
            else if (id == R.id.nav_reports) viewPager.setCurrentItem(4);
            return true;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0: bottomNavigation.getMenu().findItem(R.id.nav_dashboard).setChecked(true); break;
                    case 1: bottomNavigation.getMenu().findItem(R.id.nav_finance).setChecked(true); break;
                    case 2: bottomNavigation.getMenu().findItem(R.id.nav_savings).setChecked(true); break;
                    case 3: bottomNavigation.getMenu().findItem(R.id.nav_habits).setChecked(true); break;
                    case 4: bottomNavigation.getMenu().findItem(R.id.nav_reports).setChecked(true); break;
                }
            }
        });

        startEntryAnimation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Kalau belum login, balik ke LoginActivity
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void startEntryAnimation() {
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        slideUp.setStartOffset(200);
        bottomNavigation.startAnimation(slideUp);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in);
        viewPager.startAnimation(fadeIn);
    }

    private static class MainPagerAdapter extends FragmentStateAdapter {
        public MainPagerAdapter(FragmentActivity fa) { super(fa); }
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new DashboardFragment();
                case 1: return new FinanceFragment();
                case 2: return new SavingsFragment();
                case 3: return new HabitsFragment();
                case 4: return new ReportsFragment();
                default: return new DashboardFragment();
            }
        }
        @Override
        public int getItemCount() { return 5; }
    }
}
