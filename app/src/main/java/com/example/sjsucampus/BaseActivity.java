package com.example.sjsucampus;

import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by chitoo on 10/22/16.
 */

public class BaseActivity extends AppCompatActivity {
    ActionBar actionBar;

    public <T> T findViewById(@IdRes int id, Class<? extends View> clz) {
        return (T) findViewById(id);
    }

    protected void exit() {
        finish();
        if (!this.getClass().equals(MainActivity.class)) {
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);

    }


    protected void initActionbar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null && !this.getClass().equals(MainActivity.class)) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (title != null) {
            actionBar.setTitle(title);
        }
    }
}
