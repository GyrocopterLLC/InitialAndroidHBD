package com.example.david.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class ThrottleView extends View {
    private static final int DEFAULT_GRADIENT_START = 75;
    private static final int DEFAULT_MIN_COLOR = Color.argb(255, 0, 255, 0);
    private static final int DEFAULT_MAX_COLOR = Color.argb(255,255,0,0);
    private static final int DEFAULT_MARKER_COLOR = Color.argb(255,255,255,255);
    private static final int DEFAULT_WIDTH = 30;
    private static final int DEFAULT_HEIGHT = 100;
    private static final int DEFAULT_MARGIN = 32;

    private int mMinColor = DEFAULT_MIN_COLOR;
    private int mMaxColor = DEFAULT_MAX_COLOR;
    private int mMarkerColor = DEFAULT_MARKER_COLOR;
    private int mGradientStart = DEFAULT_GRADIENT_START;
    private int mWidth = DEFAULT_WIDTH;
    private int mHeight = DEFAULT_HEIGHT;
    private int mThrottlePosition = 0;

    private Paint mBarPaint;
    private Path mMarkerCPath;
    private Path mMarkerLPath;
    private Path mMarkerRPath;
    private Paint mMarkerPaint;
    private Rect mThrottleBar;
    private float[] mGradientPoints = new float[5];
    private int[] mColorPoints = new int[5];

    public ThrottleView(Context context) {
        super(context);
    }

    public ThrottleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.ThrottleView,
                0, 0);
        try{
            mMinColor = a.getColor(R.styleable.ThrottleView_min_color, DEFAULT_MIN_COLOR);
            mMaxColor = a.getColor(R.styleable.ThrottleView_max_color, DEFAULT_MAX_COLOR);
            mMarkerColor = a.getColor(R.styleable.ThrottleView_marker_color, DEFAULT_MARKER_COLOR);
            mGradientStart = a.getInt(R.styleable.ThrottleView_gradient_start, DEFAULT_GRADIENT_START);
        } finally{
            a.recycle();
        }

//        mBarPaint.setStyle(Paint.Style.FILL);

        mMarkerPaint = new Paint();
        mMarkerPaint.setColor(mMarkerColor);
        mMarkerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mMarkerPaint.setStrokeWidth(5f);

        mMarkerLPath = new Path();
        mMarkerRPath = new Path();
        mMarkerCPath = new Path();
        mBarPaint = new Paint();
        mThrottleBar = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(mThrottleBar,mBarPaint);
        mMarkerCPath.reset();
        mMarkerCPath.moveTo(DEFAULT_MARGIN/2,mHeight*(100-mThrottlePosition)/100);
        mMarkerCPath.lineTo(mWidth-DEFAULT_MARGIN/2, mHeight*(100-mThrottlePosition)/100);
        canvas.drawPath(mMarkerCPath,mMarkerPaint);
        mMarkerLPath.reset();
        mMarkerLPath.moveTo(DEFAULT_MARGIN/2,mHeight*(100-mThrottlePosition)/100);
        mMarkerLPath.lineTo(0, mHeight*(100-mThrottlePosition)/100-DEFAULT_MARGIN/4);
        mMarkerLPath.lineTo(0,mHeight*(100-mThrottlePosition)/100+DEFAULT_MARGIN/4);
        mMarkerLPath.lineTo(DEFAULT_MARGIN/2, mHeight*(100-mThrottlePosition)/100);
        canvas.drawPath(mMarkerLPath,mMarkerPaint);
        mMarkerRPath.reset();
        mMarkerRPath.moveTo(mWidth-DEFAULT_MARGIN/2,mHeight*(100-mThrottlePosition)/100);
        mMarkerRPath.lineTo(mWidth-1, mHeight*(100-mThrottlePosition)/100 - DEFAULT_MARGIN/4);
        mMarkerRPath.lineTo(mWidth-1, mHeight*(100-mThrottlePosition)/100 + DEFAULT_MARGIN/4);
        mMarkerRPath.lineTo(mWidth-DEFAULT_MARGIN/2, mHeight*(100-mThrottlePosition)/100);
        canvas.drawPath(mMarkerRPath,mMarkerPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int preferredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int preferredHeight = MeasureSpec.getSize(heightMeasureSpec);

        switch(widthMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                mWidth = preferredWidth;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                mWidth = DEFAULT_WIDTH;
                break;
        }
        switch(heightMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                mHeight = preferredHeight;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                mHeight = DEFAULT_HEIGHT;
                break;
        }
        setMeasuredDimension(mWidth, mHeight);

        mGradientPoints[0] = 0.01f;
        if(mGradientStart < 90) mGradientPoints[1] = (float)(100-(mGradientStart+10))/100.0f;
        else mGradientPoints[1] = 0.01f;
        mGradientPoints[2] = (float)(100-mGradientStart)/100.0f;
        if(mGradientStart > 10) mGradientPoints[3] = (float)(100-(mGradientStart-10))/100.0f;
        else mGradientPoints[3] = 0.99f;
        mGradientPoints[4] = 0.99f;

        mColorPoints[0] = mMaxColor;
        mColorPoints[1] = mMaxColor;
        mColorPoints[2] = mMinColor;
        mColorPoints[3] = mMinColor;
        mColorPoints[4] = mMinColor;

        mBarPaint.setShader(new LinearGradient(0,0, 0,mHeight,
                mColorPoints,mGradientPoints,
                Shader.TileMode.CLAMP));

        mThrottleBar.set(DEFAULT_MARGIN/2,0,
                mWidth-DEFAULT_MARGIN/2,mHeight);

    }

    public void setThrottlePosition(int pos) {
        if(pos >= 0 && pos <= 100) {
            mThrottlePosition = pos;
            this.invalidate();
        }
    }

    public int getThrottlePosition() {
        return mThrottlePosition;
    }
}
