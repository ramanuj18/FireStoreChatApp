package com.example.firestorechatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firestorechatapp.fragment.GroupFragment;
import com.example.firestorechatapp.fragment.MyAccount;
import com.example.firestorechatapp.fragment.People;
import com.example.firestorechatapp.util.FirestoreUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    FrameLayout fragmentContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        replaceFragment(new People());

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        fragmentContainer=findViewById(R.id.fragment_container);

        tabLayout.addTab(tabLayout.newTab().setText("CONTACTS"));
        tabLayout.addTab(tabLayout.newTab().setText("GROUP"));
        tabLayout.addTab(tabLayout.newTab().setText("PROFILE"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            Fragment fragment;
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    fragment=new People();
                    replaceFragment(fragment);
                }else if(tab.getPosition()==1){
                    fragment=new GroupFragment();
                    replaceFragment(fragment);
                }else if(tab.getPosition()==2){
                    fragment=new MyAccount();
                    replaceFragment(fragment);
                }else {

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }
    private void replaceFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,fragment).commit();
    }

    @Override
    protected void onStop() {
        Log.d("activityOnStop","called");
        super.onStop();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.option_creategroup:
                Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MainActivity.this,NewGroupActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
