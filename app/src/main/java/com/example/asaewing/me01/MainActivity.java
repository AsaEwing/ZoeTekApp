package com.example.asaewing.me01;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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
import com.example.asaewing.me01.fl.fl_05_HRV;

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

        String sTag = "fl_05_HRV";
        Fragment fragment = fl_05_HRV.newInstance();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_c_MainFragment, fragment, sTag).commit();

        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle(sTag);

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
        String sTag = null;

        if (id == R.id.nav_01) {
            sTag = "fl_01_scan";

        } else if (id == R.id.nav_02) {
            sTag = "fl_02_data";

        } else if (id == R.id.nav_03) {
            sTag = "fl_03_ZoeTek";

        } else if (id == R.id.nav_04) {
            sTag = "fl_05_HRV";

        } else if (id == R.id.nav_05) {

        } else if (id == R.id.nav_06) {

        }

        setFragment(sTag);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setFragment(String sTag){
        Fragment fragment = null;
        if (sTag == null) return;

        switch (sTag){
            case "fl_01_scan":
                fragment = fl_01_scan.newInstance();
                break;
            case "fl_02_data":
                fragment = fl_02_data.newInstance();
                break;
            case "fl_03_ZoeTek":
                fragment = fl_03_ZoeTek.newInstance();
                break;
            case "fl_05_HRV":
                fragment = fl_05_HRV.newInstance();
                break;
        }

        if (fragment == null) return;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_c_MainFragment, fragment, sTag).commit();

        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle(sTag);

    }

    public DataManager getDataManager(){
        return dataManager;
    }
}
