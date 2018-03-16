package com.soujarover.zoomtranstest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class MovingDotsView extends View {

    public MovingDotsView(Context context) {
        super(context);
        init();
    }

    public MovingDotsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovingDotsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MovingDotsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public interface MovingDotsListener {
        void handleOnAction(MotionEvent event);
    }

    private MovingDotsListener mListener;

    public void setMovingDotsListener(MovingDotsListener listener) {
        mListener = listener;
    }

    private Paint mPaint;

    private void init() {
        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
    }

    private final int minDis = 80;//距离绘制的点在80以内才算操作那个点
    private static final int NONE = 0;
    private static final int TRANS = 1;
    private static final int ZOOM = 2;
    private int mode;
    // 手指之间的初始距离
    private float oldDistance;
    // 多点触屏时的中心点
    private PointF curMidPoint;

    private boolean bTransMode = false;
    private float curOffX, curOffY;

    private boolean bZoomMode = false;
    private float curScale = 1;

    // 手指按下屏幕的X坐标
    private float downX;
    // 手指按下屏幕的Y坐标
    private float downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mPoints == null) return false;

        //获取到手指处的横坐标和纵坐标

        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mListener != null) mListener.handleOnAction(event);
                mode = TRANS;
                //计算按下的点到描绘的点的距离，判断是否是在操作描绘的点
                calcDistanceOfAllPoints(new PointF(event.getX(), event.getY()));
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // 多点触控
                LogUtil.e("多点触控，return false");
                if (mListener != null) mListener.handleOnAction(event);
                mode = ZOOM;
                oldDistance = Tool.getSpaceDistance(event);
                curMidPoint = Tool.getMidPoint(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    if (mListener != null) mListener.handleOnAction(event);
                    curScale = Tool.getSpaceDistance(event) / oldDistance;
                    bZoomMode = true;
                    invalidate();
                } else if (mode == TRANS) {
                    if (handlePointIndex != -1) {//移动某一个绘制的点
                        mPoints.get(handlePointIndex).x = event.getX();
                        mPoints.get(handlePointIndex).y = event.getY();
                        invalidate();
                    } else {//移动画布
                        if (mListener != null) mListener.handleOnAction(event);
                        curOffX = event.getX() - downX;
                        curOffY = event.getY() - downY;
                        if (curOffX > 0f || curOffY > 0f) {
                            LogUtil.e("移动画布：x=" + curOffX + ",y=" + curOffY);
                            bTransMode = true;
                            invalidate();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (mListener != null) mListener.handleOnAction(event);
                mode = NONE;
                break;
            default:
                break;
        }
        return true;
    }

    private ArrayMap<Integer, Double> distanceMap;
    private int handlePointIndex = -1;//正在操作的点的index

    private void calcDistanceOfAllPoints(PointF touchPoint) {
        if (distanceMap == null) distanceMap = new ArrayMap<>();
        for (int i = 0; i < mPoints.size(); i++) {
            double distance = getDistance(touchPoint, mPoints.get(i));
            LogUtil.e("i=" + i + ",dis=" + distance);
            distanceMap.put(i, distance);
        }
        double minDistance = 0;
        for (Integer index : distanceMap.keySet()) {
            if (index == 0) {
                if (distanceMap.get(index) < distanceMap.get(index + 1)) {
                    minDistance = distanceMap.get(index);
                    handlePointIndex = index;
                } else {
                    minDistance = distanceMap.get(index + 1);
                    handlePointIndex = index + 1;
                }
            } else {
                if (distanceMap.get(index) < minDistance) {
                    minDistance = distanceMap.get(index);
                    handlePointIndex = index;
                }
                minDistance = Math.min(minDistance, distanceMap.get(index));
            }
        }
        LogUtil.e("minDistance=" + minDistance + ",handlePointIndex=" + handlePointIndex);
        if (minDistance > minDis) {
            LogUtil.e("but 没有操作点");
            handlePointIndex = -1;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPoints == null) return;
        if (bTransMode) {
            canvas.translate(curOffX, curOffY);
            bTransMode = false;
        }
        if (bZoomMode) {
            canvas.scale(curScale, curScale, curMidPoint.x, curMidPoint.y);
            bZoomMode = false;
        }
        for (int i = 0; i < mPoints.size(); i++) {
            PointF point = mPoints.get(i);
            canvas.drawCircle(point.x, point.y, 8, mPaint);
        }
    }

    private ArrayList<PointF> mPoints;

    public void setUpDots(ArrayList<PointF> dots) {
        mPoints = dots;
        invalidate();
    }

    public ArrayList<PointF> getPoints() {
        return mPoints;
    }


    public double getDistance(PointF p1, PointF p2) {
        double _x = Math.abs(p1.x - p2.x);
        double _y = Math.abs(p1.y - p2.y);
        return Math.sqrt(_x * _x + _y * _y);
    }
}