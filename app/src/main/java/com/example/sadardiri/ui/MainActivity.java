package com.example.sadardiri.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // 1. Pasang Adapter ke ViewPager2
        MainPagerAdapter pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Matikan swipe gesture jika tidak diinginkan (Optional, hapus baris ini kalau mau swipe)
        // viewPager.setUserInputEnabled(false);

        // 2. Saat Menu Diklik -> Pindah Halaman ViewPager
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                viewPager.setCurrentItem(0);
            } else if (id == R.id.nav_finance) {
                viewPager.setCurrentItem(1);
            } else if (id == R.id.nav_savings) {
                viewPager.setCurrentItem(2);
            } else if (id == R.id.nav_habits) {
                viewPager.setCurrentItem(3);
            } else if (id == R.id.nav_reports) {
                viewPager.setCurrentItem(4);
            }
            return true;
        });

        // 3. Saat Layar Di-Swipe -> Update Menu Bottom Nav
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigation.getMenu().findItem(R.id.nav_dashboard).setChecked(true);
                        break;
                    case 1:
                        bottomNavigation.getMenu().findItem(R.id.nav_finance).setChecked(true);
                        break;
                    case 2:
                        bottomNavigation.getMenu().findItem(R.id.nav_savings).setChecked(true);
                        break;
                    case 3:
                        bottomNavigation.getMenu().findItem(R.id.nav_habits).setChecked(true);
                        break;
                    case 4:
                        bottomNavigation.getMenu().findItem(R.id.nav_reports).setChecked(true);
                        break;
                }
            }
        });
    }

    // ADAPTER: Mengatur urutan fragment untuk Swipe
    private static class MainPagerAdapter extends FragmentStateAdapter {
        public MainPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

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
        public int getItemCount() {
            return 5; // Jumlah Menu
        }
    }
}