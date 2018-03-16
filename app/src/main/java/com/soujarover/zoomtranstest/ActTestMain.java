package com.soujarover.zoomtranstest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Souja on 2018/3/16 0016.
 */

public class ActTestMain extends AppCompatActivity {


    @BindView(R.id.tiv_test)
    TouchImageView mTivTest;
    @BindView(R.id.mdv_test)
    MovingDotsView mMdvTest;

    private Bitmap bmpBot;
    private ArrayList<PointF> originPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test);
        ButterKnife.bind(this);

        bmpBot = BitmapFactory.decodeResource(getResources(), R.drawable.aaa);

        mTivTest.setupImage(bmpBot);

        mMdvTest.setMovingDotsListener(new MovingDotsView.MovingDotsListener() {
            @Override
            public void handleOnAction(MotionEvent event) {
                mTivTest.onTouchEvent(event);
            }
        });
        originPoints = new ArrayList<>();

        ArrayList<PointF> testPoints = new ArrayList<>();
        float x = 50, y = 50;
        for (int i = 0; i < 4; i++) {
            testPoints.add(new PointF(x, y));
            x += x;
            y += y;
        }
        originPoints.addAll(testPoints);
        mMdvTest.setUpDots(testPoints);

        findViewById(R.id.btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<PointF> pointFS = new ArrayList<>();
                pointFS.addAll(originPoints);
                mMdvTest.setUpDots(pointFS);
                mTivTest.setupImage(bmpBot);
            }
        });
    }
}
