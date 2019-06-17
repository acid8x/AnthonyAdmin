package com.lepetitmaraicher.anthonyadmin;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public abstract class TouchTimer implements View.OnTouchListener {

    private boolean ready = false;
    private long touchStart = 0;

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.touchStart = System.currentTimeMillis();
                ready = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (ready && System.currentTimeMillis() - touchStart >= 1500) {
                            onTouchEnded(true);
                            ready = false;
                        }
                    }
                }, 1500);
                return true;

            case MotionEvent.ACTION_UP:
                ready = false;
                return true;

            default:
                return false;
        }
    }

    protected abstract void onTouchEnded(boolean touch);
}