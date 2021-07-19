package io.github.trojan_gfw.igniter.tool;

import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

/***************
 ***** Created by fan on 2020/6/27.
 ***************/

public abstract class NoFastDoubleClickListener implements Toolbar.OnMenuItemClickListener {

    public static final int MIN_CLICK_DELAY_TIME = 2000;
    private long lastClickTime = 0;

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            noFastDoubleClick(item);
        }
        return true;
    }

    public abstract void noFastDoubleClick(MenuItem item);
}