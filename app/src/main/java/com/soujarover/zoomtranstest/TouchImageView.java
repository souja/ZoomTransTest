package com.soujarover.zoomtranstest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TouchImageView extends View {

    public TouchImageView(Context context) {
        super(context);
    }

    public TouchImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TouchImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // 绘制图片的边框
//    private Paint paintEdge;
    // 绘制图片的矩阵
    private Matrix matrix = new Matrix();
    // 手指按下时图片的矩阵
    private Matrix downMatrix = new Matrix();
    // 手指移动时图片的矩阵
    private Matrix moveMatrix = new Matrix();
    // 资源图片的位图
    private Bitmap mBitmap;
    // 多点触屏时的中心点
    private PointF midPoint = new PointF();
    // 触控模式
    private int mode;
    private static final int NONE = 0; // 无模式
    private static final int TRANS = 1; // 拖拽模式
    private static final int ZOOM = 2; // 缩放模式

    private void reset() {
        matrix = new Matrix();
        downMatrix = new Matrix();
        moveMatrix = new Matrix();
    }

    public void setupImage(Bitmap bmp) {
        mBitmap = bmp;
        reset();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap == null) return;
        // 画图片
        canvas.drawBitmap(mBitmap, matrix, null);
    }

    // 手指按下屏幕的X坐标
    private float downX;
    // 手指按下屏幕的Y坐标
    private float downY;
    // 手指之间的初始距离
    private float oldDistance;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mBitmap == null) return false;

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mode = TRANS;
                downX = event.getX();
                downY = event.getY();
                downMatrix.set(matrix);
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // 多点触控
                LogUtil.e("获得多点触控事件");
                mode = ZOOM;
                oldDistance = Tool.getSpaceDistance(event);
                downMatrix.set(matrix);
                midPoint = Tool.getMidPoint(event);
                break;
            case MotionEvent.ACTION_MOVE:
                // 缩放
                if (mode == ZOOM) {
                    moveMatrix.set(downMatrix);
                    float scale = Tool.getSpaceDistance(event) / oldDistance;
                    moveMatrix.postScale(scale, scale, midPoint.x, midPoint.y);
                    matrix.set(moveMatrix);
                    invalidate();
                }
                // 平移
                else if (mode == TRANS) {
                    moveMatrix.set(downMatrix);
                    float offX = event.getX() - downX;
                    float offY = event.getY() - downY;

                    moveMatrix.postTranslate(offX, offY);
                    matrix.set(moveMatrix);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mode = NONE;
                break;
            default:
                break;
        }
        return true;
    }


//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//    }

}