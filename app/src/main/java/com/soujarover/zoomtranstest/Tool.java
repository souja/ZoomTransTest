package com.soujarover.zoomtranstest;

import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * Created by Souja on 2018/3/16 0016.
 */

public class Tool {
    /**
     * 获取手指间的距离
     *
     * @param event
     * @return
     */
    public static float getSpaceDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 获取手势中心点
     *
     * @param event
     */
    public static PointF getMidPoint(MotionEvent event) {
        PointF point = new PointF();
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
        return point;
    }
}
