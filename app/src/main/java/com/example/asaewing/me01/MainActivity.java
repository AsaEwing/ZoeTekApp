package com.example.asaewing.me01;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.asaewing.me01.Others.DataManager;
import com.example.asaewing.me01.Others.HiDBHelper;
import com.example.asaewing.me01.fl.fl_01_scan;
import com.example.asaewing.me01.fl.fl_02_data;
import com.example.asaewing.me01.fl.fl_03_ZoeTek;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FrameLayout Fragment_RL;
    DataManager dataManager;
    String mTAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTAG = getClass().getSimpleName();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //用以實現單頁及Map
        Fragment_RL = (FrameLayout)findViewById(R.id.fl_c_MainFragment);
        assert Fragment_RL != null;
        Fragment_RL.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_c_MainFragment, fl_01_scan.newInstance(), "nav_01").commit();

        dataManager = new DataManager(this,mTAG);
    }

    @Override
    public void onRestart() {
        super.onRestart();

        dataManager.onRestart();
        Log.d(mTAG,"**Yes_onRestart**");
    }

    @Override
    public void onDestroy() {
        dataManager.onDestroy();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_01) {
            // Handle the camera action
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_c_MainFragment, fl_01_scan.newInstance(), "nav_01").commit();
        } else if (id == R.id.nav_02) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_c_MainFragment, fl_02_data.newInstance(), "nav_02").commit();

        } else if (id == R.id.nav_03) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_c_MainFragment, fl_03_ZoeTek.newInstance(), "nav_02").commit();

        } else if (id == R.id.nav_04) {

        } else if (id == R.id.nav_05) {

        } else if (id == R.id.nav_06) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public DataManager getDataManager(){
        return dataManager;
    }
}
