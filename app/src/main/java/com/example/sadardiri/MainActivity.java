// MainActivity.java
package com.example.sadardiri;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                fragment = new DashboardFragment();
            } else if (id == R.id.nav_finance) {
                fragment = new FinanceFragment();
            } else if (id == R.id.nav_savings) {
                fragment = new SavingsFragment();
            } else if (id == R.id.nav_habits) {
                fragment = new HabitsFragment();
            } else if (id == R.id.nav_reports) {
                fragment = new ReportsFragment();
            }
            if (fragment != null) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, fragment);
                ft.commit();
            }
            return true;
        });

        // Buka Dashboard secara default
        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        }
    }
}