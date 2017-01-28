package com.tr.nsergey.uchetKomplektacii;

import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class FiveFlingListener extends GestureDetector.SimpleOnGestureListener{
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private boolean gestureInProgress = false;
    private static int numSwipes = 0;
    private FiveFlingObserver fiveFlingObserver;
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2,
                           float velocityX, float velocityY) {
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                }
                result = true;
            }
            else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    onSwipeBottom();
                } else {
                    onSwipeTop();
                }
            }
            result = true;

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }
    public void subscribe(FiveFlingObserver fiveFlingObserver){
        this.fiveFlingObserver = fiveFlingObserver;
    }
    public void onSwipeRight() {
        if(numSwipes > 3){
            numSwipes = 0;
            fiveFlingObserver.onFiveFlings();
            return;
        }
        if(!gestureInProgress){
            gestureInProgress = true;
            new Handler().postDelayed(() -> {
                gestureInProgress = false;
                numSwipes = 0;
            }, 5000);
        }
        numSwipes++;
    }

    public void onSwipeLeft() {
        numSwipes = 0;
    }

    public void onSwipeTop() {
        numSwipes = 0;
    }

    public void onSwipeBottom() {
        numSwipes = 0;
    }
    public interface FiveFlingObserver{
        void onFiveFlings();
    }
}
