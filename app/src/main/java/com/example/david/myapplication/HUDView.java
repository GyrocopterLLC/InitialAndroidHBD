package com.example.david.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

public class HUDView extends View {

    private static final String TAG = GaugeView.class.getSimpleName();
    private static final int preferredWidth = 725;
    private static final int preferredHeight = 525;
    private static final int NUM_LED_BARS = 12;

    // "Constants" for limiting the gauges
    private float MIN_BATTERY_VOLTS = 48.0f; // 16s battery pack at 3.0V
    private float MAX_BATTERY_VOLTS = 67.2f; // 16s battery pack at 4.2V
    private float MAX_POWER = 1000.0f;
    private float MAX_SPEED = 50.0f;
    private float SPEEDO_START_ANGLE = 135.0f;
    private float SPEEDO_FULL_ANGLE = 270.0f;

    // Current values
    private float mSpeed = 0.0f;
    private float mPower = 0f;
    private float mVolts = 0f;
    private float mThrottle = 0.f;

    // Paints, for drawing on a Canvas
    private class Paints {
        Paint SpeedoOn;
        Paint SpeedoOff;
        Paint SpeedoWiper;
        Paint SpeedoBigText;
        Paint SpeedoSmallText;
        Paint SpeedoUnitsText;
        Paint SpeedoTicksText;

        Paint ThrottleOn;
        Paint ThrottleOff;
        Paint ThrottleText;
        Paint ThrottleFrame;

        Paint BattPwrLo_On;
        Paint BattPwrLo_Off;
        Paint BattPwrMid_On;
        Paint BattPwrMid_Off;
        Paint BattPwrHi_On;
        Paint BattPwrHi_Off;
        Paint BattPwrText;
    }
    private Paints mpaints = new Paints();

    // Paths, which are colored by Paints
    private class Paths {
        Path SpeedoOn;
        Path SpeedoOff;
        Path ThrottleLeftFrame;
        Path ThrottleRightFrame;
        Path ThrottleOn;
        Path ThrottleOff;
    };
    private Paths mpaths = new Paths();

    // Drawing points
    private class SpeedoPoints {
        RectF circle;
        PointF center;
        float radius;
        float speedTextOffset;
//        float textbottom;
//        float textcenter;
        PointF[] ticks;
    };
    private SpeedoPoints msppts = new SpeedoPoints();

    private class ThrottlePoints {
        PointF ul;
        PointF ur;
        PointF ll;
        PointF lr;
    };
    private ThrottlePoints mthrpts = new ThrottlePoints();

    private class LEDBarPoints {
        float barspacing;
        float leftwall;
        float rightwall;
        PointF[] barpts;
    }
    private LEDBarPoints mbarpts = new LEDBarPoints();

    public HUDView(Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public HUDView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.HUDView,
                0, 0);
        try{
            MIN_BATTERY_VOLTS = a.getFloat(R.styleable.HUDView_min_battery, MIN_BATTERY_VOLTS);
            MAX_BATTERY_VOLTS = a.getFloat(R.styleable.HUDView_max_battery, MAX_BATTERY_VOLTS);
            MAX_POWER = a.getFloat(R.styleable.HUDView_max_power, MAX_POWER);
            MAX_SPEED = a.getFloat(R.styleable.HUDView_max_speed, MAX_SPEED);

        } finally{
            a.recycle();
        }
        initDrawingTools();
    }

    public void setCurrentData(float speed, float throttle, float battery_volts, float total_power) {
        mSpeed = speed;
        mThrottle = throttle;
        mVolts = battery_volts;
        mPower = total_power;
        this.invalidate();
    }

    private Paint createPaint(Paint.Style style, @ColorRes int color_id, float strokeWidth,
                             float shadowRadius, @ColorRes int shadow_color_id) {
        Paint newpaint = new Paint();
        newpaint.setStyle(style);
        newpaint.setColor(ResourcesCompat.getColor(
                getResources(),
                color_id,
                null));
        newpaint.setStrokeWidth(strokeWidth);
        newpaint.setAntiAlias(true);
        if(shadowRadius > 0.0f) {
            newpaint.setShadowLayer(shadowRadius, 0f, 0f, ResourcesCompat.getColor(
                    getResources(),
                    shadow_color_id,
                    null));
        }

        return newpaint;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initDrawingTools() {
        // Create the paints for speedometer, power and battery gauges, throttle
        mpaints.SpeedoOn = createPaint(Paint.Style.STROKE,
                R.color.colorPrimary,
                60f,
                5f,
                R.color.colorPrimaryDark);

        mpaths.SpeedoOn = new Path();
        mpaths.SpeedoOff = new Path();

        mpaints.SpeedoOff = createPaint(Paint.Style.STROKE,
                R.color.colorInactive,
                60f,
                5f,
                R.color.colorPrimaryDark);

        mpaints.SpeedoWiper = createPaint(Paint.Style.STROKE,
                R.color.wiper,
                10f,
                10f,
                R.color.colorPrimaryDark);

        msppts.circle = new RectF();
        msppts.center = new PointF();

        mpaints.SpeedoBigText = createPaint(Paint.Style.FILL_AND_STROKE,
                R.color.colorPrimaryDark,
                1f,
                0f,
                0);

        mpaints.SpeedoSmallText = createPaint(Paint.Style.FILL_AND_STROKE,
                R.color.colorPrimaryDark,
                1f,
                0f,
                0);

        mpaints.SpeedoUnitsText = createPaint(Paint.Style.FILL_AND_STROKE,
                R.color.colorPrimaryDark,
                1f,
                0f,
                0);
        mpaints.SpeedoUnitsText.setTextScaleX(0.5f);

        mpaints.SpeedoTicksText = createPaint(Paint.Style.FILL_AND_STROKE,
                R.color.colorPrimaryDark,
                1f,
                0f,
                0);

        int numticks = (int)MAX_SPEED / 5;
        if((int)MAX_SPEED % 5 == 0) numticks = numticks+1;
        msppts.ticks = new PointF[numticks];

        mthrpts.ul = new PointF();
        mthrpts.ur = new PointF();
        mthrpts.ll = new PointF();
        mthrpts.lr = new PointF();
        mpaths.ThrottleLeftFrame = new Path();
        mpaths.ThrottleRightFrame = new Path();
        mpaths.ThrottleOn = new Path();
        mpaths.ThrottleOff = new Path();

        mpaints.ThrottleOn = createPaint(Paint.Style.FILL,
                R.color.colorPrimary,
                1f,
                5f,
                R.color.colorPrimaryDark);

        mpaints.ThrottleOff = createPaint(Paint.Style.FILL,
                R.color.colorInactive,
                1f,
                5f,
                R.color.colorPrimaryDark);

        mpaints.ThrottleText = createPaint(Paint.Style.FILL_AND_STROKE,
                R.color.colorPrimaryDark,
                1f,
                0f,
                0);

        mpaints.ThrottleFrame = createPaint(Paint.Style.FILL,
                R.color.throttleFrame,
                1f,
                5,
                R.color.colorPrimaryDark);

        mpaints.BattPwrHi_On = createPaint(Paint.Style.STROKE,
                R.color.battPwrHi,
                1.0f,
                5.0f,
                R.color.battPwrHi);
        mpaints.BattPwrHi_Off = createPaint(Paint.Style.STROKE,
                R.color.battPwrHiOff,
                1.0f,
                5.0f,
                R.color.colorPrimaryDark);

        mpaints.BattPwrMid_On = createPaint(Paint.Style.STROKE,
                R.color.battPwrMid,
                1.0f,
                5.0f,
                R.color.battPwrMid);
        mpaints.BattPwrMid_Off = createPaint(Paint.Style.STROKE,
                R.color.battPwrMidOff,
                1.0f,
                5.0f,
                R.color.colorPrimaryDark);

        mpaints.BattPwrLo_On = createPaint(Paint.Style.STROKE,
                R.color.battPwrLo,
                1.0f,
                5.0f,
                R.color.battPwrLo);
        mpaints.BattPwrLo_Off = createPaint(Paint.Style.STROKE,
                R.color.battPwrLoOff,
                1.0f,
                5.0f,
                R.color.colorPrimaryDark);

        mpaints.BattPwrText = createPaint(Paint.Style.FILL_AND_STROKE,
                R.color.colorPrimaryDark,
                1.0f,
                0f,
                0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int chosenHeight;
        int chosenWidth;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if(widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            chosenWidth = widthSize;
        } else {
            chosenWidth = preferredWidth;
        }

        if(heightMode == MeasureSpec.EXACTLY|| heightMode == MeasureSpec.AT_MOST) {
            chosenHeight = heightSize;
        } else {
            chosenHeight = preferredHeight;
        }

        setMeasuredDimension(chosenWidth, chosenHeight);
        calculatePoints(chosenWidth, chosenHeight);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {

        calculatePoints(width, height);
    }

    private void calculatePoints(int width, int height) {
        // Determine start / stop points for all the drawings

        // Speedometer drawing math
        int speedoWidth;
//        if(width < height) {
            speedoWidth = 60*width/100;
//        } else {
//            speedoWidth = 60*height/100;
//        }
        // Centered left-to-right
        msppts.circle.left = (width - speedoWidth) / 2;
        msppts.circle.right = width - msppts.circle.left;
        // Bring it down from the top enough to see the full speedometer arc
        msppts.circle.top = mpaints.SpeedoOn.getStrokeWidth();
        // Force a square
        msppts.circle.bottom = msppts.circle.right
                - msppts.circle.left + msppts.circle.top;

        msppts.radius = (msppts.circle.right - msppts.circle.left) / 2;
        msppts.center.x = (msppts.circle.left + msppts.circle.right) / 2;
        msppts.center.y = (msppts.circle.top + msppts.circle.bottom) / 2;
        msppts.speedTextOffset = speedoWidth/10;

//        msppts.textbottom = (float)(Math.sin(Math.PI*(135.0f+180.0f)/180.0f)
//                * msppts.radius) + msppts.center.y;

//        msppts.textcenter = (msppts.circle.top + msppts.textbottom) / 2;


        mpaints.SpeedoBigText.setTextSize(speedoWidth/3);
        mpaints.SpeedoSmallText.setTextSize(speedoWidth/5);
        mpaints.SpeedoUnitsText.setTextSize(speedoWidth/8);
        mpaints.SpeedoTicksText.setTextSize(speedoWidth/15);

        float tempx, tempy;
        for(int i = 0; i < msppts.ticks.length; i++) {
            tempx = (float)(Math.cos((Math.PI/180.0f) * (SPEEDO_START_ANGLE  + (SPEEDO_FULL_ANGLE*(5f*i)/MAX_SPEED)))
                    * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f + 6*speedoWidth/100)))
                    + msppts.center.x;
            tempy = (float)(Math.sin((Math.PI/180.0f) * (SPEEDO_START_ANGLE  + (SPEEDO_FULL_ANGLE*(5f*i)/MAX_SPEED)))
                    * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f + 6*speedoWidth/100)))
                    + msppts.center.y;
            msppts.ticks[i] = new PointF(tempx, tempy);
        }

        // Throttle drawing math
        mpaints.ThrottleText.setTextSize(speedoWidth / 15);
        mthrpts.ll.x = (float)(Math.cos(Math.PI*(SPEEDO_START_ANGLE)/180.0f))
                * (msppts.radius + (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.x
                + 16f;
        mthrpts.ll.y = (float)(Math.sin(Math.PI*(SPEEDO_START_ANGLE)/180.0f))
                * (msppts.radius + (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.y;

        mthrpts.ul.x = (float)(Math.cos(Math.PI*(SPEEDO_START_ANGLE)/180.0f))
                * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.x
                + 16f;
        mthrpts.ul.y = (float)(Math.sin(Math.PI*(SPEEDO_START_ANGLE)/180.0f))
                * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.y;

        mthrpts.lr.x = (float)(Math.cos(Math.PI*(SPEEDO_START_ANGLE+SPEEDO_FULL_ANGLE)/180.0f))
                * (msppts.radius + (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.x
                - 16f;
        mthrpts.lr.y = (float)(Math.sin(Math.PI*(SPEEDO_START_ANGLE+SPEEDO_FULL_ANGLE)/180.0f))
                * (msppts.radius + (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.y;

        mthrpts.ur.x = (float)(Math.cos(Math.PI*(SPEEDO_START_ANGLE+SPEEDO_FULL_ANGLE)/180.0f))
                * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.x
                - 16f;
        mthrpts.ur.y = (float)(Math.sin(Math.PI*(SPEEDO_START_ANGLE+SPEEDO_FULL_ANGLE)/180.0f))
                * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.y;

        // Predraw the frame triangles on left and right
        mpaths.ThrottleLeftFrame.reset();
        mpaths.ThrottleLeftFrame.moveTo(mthrpts.ll.x, mthrpts.ll.y);
        mpaths.ThrottleLeftFrame.lineTo(mthrpts.ul.x, mthrpts.ul.y);
        mpaths.ThrottleLeftFrame.lineTo(mthrpts.ul.x, mthrpts.ll.y);
        mpaths.ThrottleLeftFrame.close();
        mpaths.ThrottleRightFrame.reset();
        mpaths.ThrottleRightFrame.moveTo(mthrpts.lr.x, mthrpts.lr.y);
        mpaths.ThrottleRightFrame.lineTo(mthrpts.ur.x, mthrpts.ur.y);
        mpaths.ThrottleRightFrame.lineTo(mthrpts.ur.x, mthrpts.lr.y);
        mpaths.ThrottleRightFrame.close();

        // Battery and power LED bars drawing math
        mpaints.BattPwrText.setTextSize(speedoWidth/6);
        // Total Y height is 10 bars plus the word "Batt" or "Pwr", equal to the height
        // of 80% of the speedometer
        // Battery voltage and power readouts appear almost above the speedo

        float speedoHeight =  mthrpts.ll.y - msppts.circle.top; // Throttle bar is at the bottom
                                                                // of the speedometer
        Rect bounds = new Rect();
        mpaints.BattPwrText.getTextBounds("Batt",0,4,bounds);
        // Figure out how big the bars are. Using text on top and text on bottom (2*bounds.height),
        // and spanning from the bottom to the top of the speedometer. A little spacing (8pxl)
        // between the bottom text and bars, and another spacing (8 more pxl) between top text
        // and bars.
        mbarpts.barspacing = ((speedoHeight+(mpaints.SpeedoOn.getStrokeWidth()/2f) - 2*bounds.height() - 16f) / NUM_LED_BARS);
        mpaints.BattPwrHi_On.setStrokeWidth(mbarpts.barspacing - 4f); // Get the blank space between, too
        mpaints.BattPwrMid_On.setStrokeWidth(mbarpts.barspacing - 4f);
        mpaints.BattPwrLo_On.setStrokeWidth(mbarpts.barspacing - 4f);
        mpaints.BattPwrHi_Off.setStrokeWidth(mbarpts.barspacing/2f - 4f);
        mpaints.BattPwrMid_Off.setStrokeWidth(mbarpts.barspacing/2f - 4f);
        mpaints.BattPwrLo_Off.setStrokeWidth(mbarpts.barspacing/2f - 4f);

        mbarpts.leftwall = 16f;
        mbarpts.rightwall = width - mbarpts.leftwall;
        mbarpts.barpts = new PointF[NUM_LED_BARS];
        for(int i = 0; i < NUM_LED_BARS; i++) {
            mbarpts.barpts[i] = new PointF();
            mbarpts.barpts[i].y = mthrpts.ll.y - bounds.height() - mbarpts.barspacing/2 - 8f
                    - i*mbarpts.barspacing;
            mbarpts.barpts[i].x = (float) (Math.pow(msppts.radius+mpaints.SpeedoOn.getStrokeWidth(),2)
                                - Math.pow(mbarpts.barpts[i].y - msppts.center.y,2));
            if(mbarpts.barpts[i].x < 0f) {
                if(i > 0)
                    mbarpts.barpts[i].x = mbarpts.barpts[i-1].x;
                else
                    mbarpts.barpts[i].x = msppts.center.x - mthrpts.ll.x;
            }
            else
            {
                mbarpts.barpts[i].x = (float)Math.sqrt(mbarpts.barpts[i].x);
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSpeedo(canvas);
        drawThrottle(canvas);
        drawBattery(canvas);
        drawPower(canvas);
    }

    private void drawSpeedo(Canvas canvas) {
        if(mSpeed > MAX_SPEED) mSpeed = MAX_SPEED;
        if(mSpeed < 0f) mSpeed = 0f;
        float speedoSweepAngle = (360.0f-90.0f)*mSpeed/MAX_SPEED;

        // Draw the background color
        canvas.drawColor(ResourcesCompat.getColor(getResources(),
                R.color.app_background,
                null));

        // Draw the speedometer arc
        mpaths.SpeedoOn.rewind();
        mpaths.SpeedoOn.addArc(msppts.circle,
                SPEEDO_START_ANGLE,// Start angle = start of speed (clockwise drawing)
                speedoSweepAngle// Sweep angle
        );

        mpaths.SpeedoOff.rewind();
        mpaths.SpeedoOff.addArc(msppts.circle,
                SPEEDO_START_ANGLE+speedoSweepAngle,
                SPEEDO_FULL_ANGLE-speedoSweepAngle);

        canvas.drawPath(mpaths.SpeedoOn,mpaints.SpeedoOn);
        canvas.drawPath(mpaths.SpeedoOff,mpaints.SpeedoOff);

        // Add the wiper
        float speedoThick = mpaints.SpeedoOn.getStrokeWidth();
        float speedoAngle = SPEEDO_START_ANGLE+speedoSweepAngle;
        // Draw from inside to outside of line thickness
        float wiperStartX = (float)(Math.cos(Math.PI*speedoAngle/180.0f)
                * (msppts.radius - (speedoThick/2.0f + 5f))) + msppts.center.x;
        float wiperStartY = (float)(Math.sin(Math.PI*speedoAngle/180.0f)
                * (msppts.radius-(speedoThick/2.0f + 5f))) + msppts.center.y;
        float wiperStopX = (float)(Math.cos(Math.PI*speedoAngle/180.0f)
                * (msppts.radius+(speedoThick/2.0f + 5f))) + msppts.center.x;
        float wiperStopY = (float)(Math.sin(Math.PI*speedoAngle/180.0f)
                * (msppts.radius+(speedoThick/2.0f + 5f))) + msppts.center.y;
        canvas.drawLine(wiperStartX,wiperStartY,wiperStopX,wiperStopY,mpaints.SpeedoWiper);

        // Place the ticks

        Rect tickbounds = new Rect();
        String tickstring;
        for(int i = 0; i < msppts.ticks.length; i ++) {
            tickstring = Integer.toString(5*i);
            mpaints.SpeedoTicksText.getTextBounds(tickstring, 0, tickstring.length(), tickbounds);
            canvas.drawText(tickstring,
                    msppts.ticks[i].x - tickbounds.width()/2,
                    msppts.ticks[i].y + tickbounds.height()/2,
                    mpaints.SpeedoTicksText);
        }

        // Print the speed in the middle
        String big_speed = String.format("%d.",(int)mSpeed);
        String small_speed = String.format("%d",(int)(10f*(mSpeed - (int)mSpeed)));
        Rect bigbounds = new Rect();
        Rect smallbounds = new Rect();
        mpaints.SpeedoBigText.getTextBounds(big_speed, 0, big_speed.length(), bigbounds);

        // Speed is printed in the middle of the speedometer circle, but shifted slightly left
        // from dead center.
        canvas.drawText(big_speed, (msppts.center.x + msppts.speedTextOffset)-(bigbounds.width()),
                msppts.center.y + (bigbounds.height()/2),
                mpaints.SpeedoBigText);

        mpaints.SpeedoUnitsText.getTextBounds("MPH", 0, 3, smallbounds);
        canvas.drawText(small_speed, msppts.center.x  + msppts.speedTextOffset,
                msppts.center.y + (bigbounds.height()/2) - smallbounds.height() - 8,
                mpaints.SpeedoSmallText);
        canvas.drawText("MPH", msppts.center.x + 24 + msppts.speedTextOffset,
                msppts.center.y + (bigbounds.height()/2),
                mpaints.SpeedoUnitsText);
    }

    private void drawThrottle(Canvas canvas) {
        if(mThrottle > 1.0f) mThrottle = 1.0f;
        if(mThrottle < 0.0f) mThrottle = 0.0f;


        // Determine where to stop the "on" portion of the throttle bar
        float on_end_x = (mthrpts.ur.x - mthrpts.ul.x) // width of bar
            * mThrottle // scaled to throttle
            + mthrpts.ul.x; // Offset from the left

        mpaths.ThrottleOn.rewind();
        mpaths.ThrottleOn.moveTo(mthrpts.ul.x, mthrpts.ul.y);
        mpaths.ThrottleOn.lineTo(mthrpts.ul.x, mthrpts.ll.y);
        mpaths.ThrottleOn.lineTo(on_end_x, mthrpts.ll.y); // stop position
        mpaths.ThrottleOn.lineTo(on_end_x, mthrpts.ul.y);
        mpaths.ThrottleOn.close();
        canvas.drawPath(mpaths.ThrottleOn, mpaints.ThrottleOn);

        mpaths.ThrottleOff.rewind();
        mpaths.ThrottleOff.moveTo(mthrpts.ur.x, mthrpts.ur.y);
        mpaths.ThrottleOff.lineTo(mthrpts.ur.x, mthrpts.lr.y);
        mpaths.ThrottleOff.lineTo(on_end_x, mthrpts.lr.y); // stop position
        mpaths.ThrottleOff.lineTo(on_end_x, mthrpts.ur.y);
        mpaths.ThrottleOff.close();
        canvas.drawPath(mpaths.ThrottleOff, mpaints.ThrottleOff);

        // Draw triangle bits on sides of throttle bar at bottom of speedometer circle
        // Paths are precomputed in calculatePoints function
        canvas.drawPath(mpaths.ThrottleLeftFrame, mpaints.ThrottleFrame);
        canvas.drawPath(mpaths.ThrottleRightFrame, mpaints.ThrottleFrame);

        // Draw wiper

        canvas.drawLine(on_end_x,mthrpts.ul.y - 4f,
                on_end_x,mthrpts.ll.y + 4f,
                mpaints.SpeedoWiper);

        // Print labels
        Rect bounds = new Rect();
        mpaints.ThrottleText.getTextBounds("Thr",0,3,bounds);
        canvas.drawText("Thr", mthrpts.ll.x, mthrpts.ll.y + bounds.height() + 16f,
                mpaints.ThrottleText);
        String thr_pct = String.format("%2d%%",(int)(mThrottle*100f));
        mpaints.ThrottleText.getTextBounds(thr_pct,0,thr_pct.length(),bounds);
        canvas.drawText(thr_pct, mthrpts.lr.x - bounds.width(),
                mthrpts.lr.y + bounds.height() + 16f, mpaints.ThrottleText);
    }

    private void drawBattery(Canvas canvas) {
        if(mVolts < 0.0f) mVolts = 0.0f;
        if(mVolts > MAX_BATTERY_VOLTS) mVolts = MAX_BATTERY_VOLTS;

        canvas.drawText("Batt", mbarpts.leftwall, mthrpts.ll.y,mpaints.BattPwrText);
        Rect bounds = new Rect();
        String batt_val = String.format("%04.1f V", mVolts);
        mpaints.BattPwrText.getTextBounds(batt_val, 0, batt_val.length(), bounds);
        canvas.drawText(batt_val, mbarpts.leftwall,
                msppts.circle.top - mpaints.SpeedoOn.getStrokeWidth()/2+bounds.height(),
                mpaints.BattPwrText);

        Paint tempPaint;

        for (int i = 0; i < NUM_LED_BARS; i++) {
            if((mVolts - MIN_BATTERY_VOLTS) >= i*(MAX_BATTERY_VOLTS-MIN_BATTERY_VOLTS) / NUM_LED_BARS) {
                if (i > 7 * NUM_LED_BARS / 10) {
                    tempPaint = mpaints.BattPwrHi_On;
                } else if (i > 3 * NUM_LED_BARS / 10) {
                    tempPaint = mpaints.BattPwrMid_On;
                } else {
                    tempPaint = mpaints.BattPwrLo_On;
                }
            } else {
                if (i > 7 * NUM_LED_BARS / 10) {
                    tempPaint = mpaints.BattPwrHi_Off;
                } else if (i > 3 * NUM_LED_BARS / 10) {
                    tempPaint = mpaints.BattPwrMid_Off;
                } else {
                    tempPaint = mpaints.BattPwrLo_Off;
                }
            }

            canvas.drawLine(mbarpts.leftwall, mbarpts.barpts[i].y,
                    (msppts.center.x - mbarpts.barpts[i].x),mbarpts.barpts[i].y,
                    tempPaint);
        }
    }

    private void drawPower(Canvas canvas) {
        if(mPower < 0.0f) mPower = 0.0f;
        if(mPower > MAX_POWER) mPower = MAX_POWER;
        Rect bounds = new Rect();
        mpaints.BattPwrText.getTextBounds("Pwr",0,3,bounds);
        canvas.drawText("Pwr", mbarpts.rightwall-bounds.width(), mthrpts.lr.y,mpaints.BattPwrText);
        String pwr_val = String.format("%03d W",(int)mPower);
        mpaints.BattPwrText.getTextBounds(pwr_val, 0, pwr_val.length(),bounds);
        canvas.drawText(pwr_val, mbarpts.rightwall-bounds.width(),
                msppts.circle.top-mpaints.SpeedoOn.getStrokeWidth()/2+bounds.height(),
                mpaints.BattPwrText);
        Paint tempPaint;
        for (int i = 0; i < NUM_LED_BARS; i++) {
            if(mPower >= i*MAX_POWER / NUM_LED_BARS) {
                if (i > 7 * NUM_LED_BARS / 10) {
                    tempPaint = mpaints.BattPwrHi_On;
                } else if (i > 3 * NUM_LED_BARS / 10) {
                    tempPaint = mpaints.BattPwrMid_On;
                } else {
                    tempPaint = mpaints.BattPwrLo_On;
                }
            } else {
                if (i > 7 * NUM_LED_BARS / 10) {
                    tempPaint = mpaints.BattPwrHi_Off;
                } else if (i > 3 * NUM_LED_BARS / 10) {
                    tempPaint = mpaints.BattPwrMid_Off;
                } else {
                    tempPaint = mpaints.BattPwrLo_Off;
                }
            }
            canvas.drawLine(mbarpts.rightwall, mbarpts.barpts[i].y,
                    (msppts.center.x + mbarpts.barpts[i].x), mbarpts.barpts[i].y,
                    tempPaint);

        }
    }
}
