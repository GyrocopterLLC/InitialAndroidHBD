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

    // Current values
    private float mSpeed = 25.8f;
    private float mPower = 0.0f;
    private float mVolts = 0.0f;
    private float mThrottle = 0.05f;

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

        Paint BattPwrLo;
        Paint BattPwrMid;
        Paint BattPwrHi;
        Paint BattPwrText;
    }
    private Paints mpaints = new Paints();

    // Paths, which are colored by Paints
    private class Paths {
        Path SpeedoOn;
        Path SpeedoOff;
        Path ThrottleOn;
        Path ThrottleOff;
    };
    private Paths mpaths = new Paths();

    // Drawing points
    private class SpeedoPoints {
        RectF circle;
        PointF center;
        float radius;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initDrawingTools() {
        // Create the paints for speedometer, power and battery gauges, throttle
        mpaints.SpeedoOn = new Paint();
        mpaints.SpeedoOn.setStyle(Paint.Style.STROKE);
        mpaints.SpeedoOn.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimary,
                null));
        mpaints.SpeedoOn.setStrokeWidth(60f);
        mpaints.SpeedoOn.setAntiAlias(true);
        mpaints.SpeedoOn.setShadowLayer(5f,0f,0f,ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));

        mpaths.SpeedoOn = new Path();
        mpaths.SpeedoOff = new Path();

        mpaints.SpeedoOff = new Paint();
        mpaints.SpeedoOff.setStyle(Paint.Style.STROKE);
        mpaints.SpeedoOff.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorInactive,
                null));
        mpaints.SpeedoOff.setStrokeWidth(60f);
        mpaints.SpeedoOff.setAntiAlias(true);
        mpaints.SpeedoOff.setShadowLayer(5f,0f,0f,ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));

        mpaints.SpeedoWiper = new Paint();
        mpaints.SpeedoWiper.setStyle(Paint.Style.STROKE);
        mpaints.SpeedoWiper.setColor(ResourcesCompat.getColor(getResources(),
                R.color.wiper,
                null));
        mpaints.SpeedoWiper.setStrokeWidth(10f);
        mpaints.SpeedoWiper.setAntiAlias(true);
        mpaints.SpeedoWiper.setShadowLayer(10f,0f,0f,ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));

        msppts.circle = new RectF();
        msppts.center = new PointF();

        mpaints.SpeedoBigText = new Paint();
        mpaints.SpeedoBigText.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));

        //Typeface cooper = ResourcesCompat.getFont(getContext(),R.font.cooper);
        //Typeface.Builder cbuilder = new Typeface.Builder("D:\\Programming\\AndroidStudioProjects\\MyApplication\\app\\src\\main\\res\\font\\cooperblack.ttf");
        //Typeface cooper = cbuilder.build();
        //mpaints.SpeedoBigText.setTypeface(cooper);

        mpaints.SpeedoBigText.setStyle(Paint.Style.FILL_AND_STROKE);
        mpaints.SpeedoBigText.setStrokeWidth(1.0f);

        mpaints.SpeedoSmallText = new Paint();
        mpaints.SpeedoSmallText.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));
        //mpaints.SpeedoSmallText.setTypeface(cooper);
        mpaints.SpeedoSmallText.setStyle(Paint.Style.FILL_AND_STROKE);
        mpaints.SpeedoSmallText.setStrokeWidth(1.0f);

        mpaints.SpeedoUnitsText = new Paint();
        mpaints.SpeedoUnitsText.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));
        //mpaints.SpeedoSmallText.setTypeface(cooper);
        mpaints.SpeedoUnitsText.setStyle(Paint.Style.FILL_AND_STROKE);
        mpaints.SpeedoUnitsText.setStrokeWidth(1.0f);
        mpaints.SpeedoUnitsText.setTextScaleX(0.5f);

        mpaints.SpeedoTicksText = new Paint();
        mpaints.SpeedoUnitsText.setStyle(Paint.Style.FILL_AND_STROKE);
        mpaints.SpeedoUnitsText.setStrokeWidth(1.0f);

        int numticks = (int)MAX_SPEED / 5;
        if((int)MAX_SPEED % 5 == 0) numticks = numticks+1;
        msppts.ticks = new PointF[numticks];

        mthrpts.ul = new PointF();
        mthrpts.ur = new PointF();
        mthrpts.ll = new PointF();
        mthrpts.lr = new PointF();

        mpaths.ThrottleOn = new Path();
        mpaths.ThrottleOff = new Path();

        mpaints.ThrottleOn = new Paint();
        mpaints.ThrottleOn.setStyle(Paint.Style.FILL);
        mpaints.ThrottleOn.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimary,
                null));
        mpaints.ThrottleOn.setAntiAlias(true);
        mpaints.ThrottleOn.setShadowLayer(5f,0f,0f,ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));

        mpaints.ThrottleOff = new Paint();
        mpaints.ThrottleOff.setStyle(Paint.Style.FILL);
        mpaints.ThrottleOff.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorInactive,
                null));
        mpaints.ThrottleOff.setAntiAlias(true);
        mpaints.ThrottleOff.setShadowLayer(5f,0f,0f,ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));

        mpaints.ThrottleText = new Paint();
        mpaints.ThrottleText.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));
        mpaints.ThrottleText.setStyle(Paint.Style.FILL_AND_STROKE);
        mpaints.ThrottleText.setStrokeWidth(1.0f);

        mpaints.BattPwrHi = new Paint();
        mpaints.BattPwrHi.setColor(ResourcesCompat.getColor(getResources(),
                R.color.battPwrHi,
                null));
        mpaints.BattPwrHi.setStyle(Paint.Style.STROKE);
        mpaints.BattPwrHi.setAntiAlias(true);
        mpaints.BattPwrHi.setShadowLayer(5f,0f,0f,ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));

        mpaints.BattPwrMid = new Paint();
        mpaints.BattPwrMid.setColor(ResourcesCompat.getColor(getResources(),
                R.color.battPwrMid,
                null));
        mpaints.BattPwrMid.setStyle(Paint.Style.STROKE);
        mpaints.BattPwrMid.setAntiAlias(true);
        mpaints.BattPwrMid.setShadowLayer(5f,0f,0f,ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));

        mpaints.BattPwrLo = new Paint();
        mpaints.BattPwrLo.setColor(ResourcesCompat.getColor(getResources(),
                R.color.battPwrLo,
                null));
        mpaints.BattPwrLo.setStyle(Paint.Style.STROKE);
        mpaints.BattPwrLo.setAntiAlias(true);
        mpaints.BattPwrLo.setShadowLayer(5f,0f,0f,ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));

        mpaints.BattPwrText = new Paint();
        mpaints.BattPwrText.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark,
                null));
        mpaints.BattPwrText.setStyle(Paint.Style.FILL_AND_STROKE);
        mpaints.BattPwrText.setStrokeWidth(1.0f);
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

        if(widthMode == MeasureSpec.UNSPECIFIED) {
            chosenWidth = preferredWidth;
        } else {
            // Either for MeasureSpec.EXACTLY or MeasureSpec.AT_MOST
            // Width takes precedence over height
            chosenWidth = widthSize;
        }

        if(heightMode == MeasureSpec.UNSPECIFIED) {
            chosenHeight = preferredHeight;
        } else {
            chosenHeight = heightSize;
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
        if(width < height) {
            speedoWidth = 60*width/100;
        } else {
            speedoWidth = 60*height/100;
        }
        // Centered left-to-right
        msppts.circle.left = (width - speedoWidth) / 2;
        msppts.circle.right = width - msppts.circle.left;
        // Bring it down about 1/6 from the top
        msppts.circle.top = speedoWidth/6;
        // Force a square
        msppts.circle.bottom = msppts.circle.right
                - msppts.circle.left + msppts.circle.top;

        msppts.radius = (msppts.circle.right - msppts.circle.left) / 2;
        msppts.center.x = (msppts.circle.left + msppts.circle.right) / 2;
        msppts.center.y = (msppts.circle.top + msppts.circle.bottom) / 2;

//        msppts.textbottom = (float)(Math.sin(Math.PI*(135.0f+180.0f)/180.0f)
//                * msppts.radius) + msppts.center.y;

//        msppts.textcenter = (msppts.circle.top + msppts.textbottom) / 2;


        mpaints.SpeedoBigText.setTextSize(speedoWidth/3);
        mpaints.SpeedoSmallText.setTextSize(speedoWidth/5);
        mpaints.SpeedoUnitsText.setTextSize(speedoWidth/8);
        mpaints.SpeedoTicksText.setTextSize(speedoWidth/15);

        float tempx, tempy;
        for(int i = 0; i < msppts.ticks.length; i++) {
            tempx = (float)(Math.cos((Math.PI/180.0f) * (135f  + (270f*(5f*i)/MAX_SPEED)))
                    * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f + 6*speedoWidth/100)))
                    + msppts.center.x;
            tempy = (float)(Math.sin((Math.PI/180.0f) * (135f  + (270f*(5f*i)/MAX_SPEED)))
                    * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f + 6*speedoWidth/100)))
                    + msppts.center.y;
            msppts.ticks[i] = new PointF(tempx, tempy);
        }

        // Throttle drawing math
        mpaints.ThrottleText.setTextSize(speedoWidth / 15);
        mthrpts.ll.x = (float)(Math.cos(Math.PI*(135.0f)/180.0f))
                * (msppts.radius + (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.x
                + 16f;
        mthrpts.ll.y = (float)(Math.sin(Math.PI*(135.0f)/180.0f))
                * (msppts.radius + (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.y;

        mthrpts.ul.x = (float)(Math.cos(Math.PI*(135.0f)/180.0f))
                * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.x
                + 16f;
        mthrpts.ul.y = (float)(Math.sin(Math.PI*(135.0f)/180.0f))
                * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.y;

        mthrpts.lr.x = (float)(Math.cos(Math.PI*(135.0f+270.0f)/180.0f))
                * (msppts.radius + (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.x
                - 16f;
        mthrpts.lr.y = (float)(Math.sin(Math.PI*(135.0f+270.0f)/180.0f))
                * (msppts.radius + (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.y;

        mthrpts.ur.x = (float)(Math.cos(Math.PI*(135.0f+270.0f)/180.0f))
                * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.x
                - 16f;
        mthrpts.ur.y = (float)(Math.sin(Math.PI*(135.0f+270.0f)/180.0f))
                * (msppts.radius - (mpaints.SpeedoOn.getStrokeWidth()/2.0f)) + msppts.center.y;


        // Battery and power LED bars drawing math
        mpaints.BattPwrText.setTextSize(speedoWidth/5);
        // Total Y height is 10 bars plus the word "Batt" or "Pwr", equal to the height
        // of 80% of the speedometer
        // Battery voltage and power readouts appear almost above the speedo

        float speedoHeight =  mthrpts.ll.y - msppts.circle.top; // Throttle bar is at the bottom
                                                                // of the speedometer
        Rect bounds = new Rect();
        mpaints.BattPwrText.getTextBounds("Batt",0,4,bounds);
        mbarpts.barspacing = ((speedoHeight - bounds.height()-8f) / NUM_LED_BARS);
        mpaints.BattPwrHi.setStrokeWidth(mbarpts.barspacing - 4f); // Get the blank space between, too
        mpaints.BattPwrMid.setStrokeWidth(mbarpts.barspacing - 4f);
        mpaints.BattPwrLo.setStrokeWidth(mbarpts.barspacing - 4f);

        mbarpts.leftwall = 5*width / 100;
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
        float speedoSweepAngle = (360.0f-90.0f)*mSpeed/MAX_SPEED;

        // Draw the background color
        canvas.drawColor(ResourcesCompat.getColor(getResources(),
                R.color.app_background,
                null));

        // Draw the speedometer arc
        mpaths.SpeedoOn.reset();
        mpaths.SpeedoOn.addArc(msppts.circle,
                135.0f,// Start angle = start of speed (clockwise drawing)
                speedoSweepAngle// Sweep angle
        );

        mpaths.SpeedoOff.reset();
        mpaths.SpeedoOff.addArc(msppts.circle,
                135.0f+speedoSweepAngle,
                270.0f-speedoSweepAngle);

        canvas.drawPath(mpaths.SpeedoOn,mpaints.SpeedoOn);
        canvas.drawPath(mpaths.SpeedoOff,mpaints.SpeedoOff);

        // Add the wiper
        float speedoThick = mpaints.SpeedoOn.getStrokeWidth();
        float speedoAngle = 135.0f+speedoSweepAngle;
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
        String small_speed = String.format("%d",(int)(0.5f+10*(mSpeed - (int)mSpeed)));
        Rect bigbounds = new Rect();
        Rect smallbounds = new Rect();
        mpaints.SpeedoBigText.getTextBounds(big_speed, 0, big_speed.length(), bigbounds);

        // Speed is printed in the middle of the speedometer circle, but shifted slightly left
        // from dead center.
        canvas.drawText(big_speed, msppts.center.x-(bigbounds.width()*0.75f),
                msppts.center.y + (bigbounds.height()/2),
                mpaints.SpeedoBigText);
        mpaints.SpeedoSmallText.getTextBounds(small_speed, 0, small_speed.length(), smallbounds);

        canvas.drawText(small_speed, msppts.center.x + 16 + (bigbounds.width()*0.25f),
                msppts.center.y + (bigbounds.height()/2) - smallbounds.height(),
                mpaints.SpeedoSmallText);
        canvas.drawText("MPH", msppts.center.x + 16 + (bigbounds.width()*0.25f),
                msppts.center.y + (bigbounds.height()/2),
                mpaints.SpeedoUnitsText);
    }

    private void drawThrottle(Canvas canvas) {
        // Draw trapezoid shapes at bottom of speedometer circle

        // Determine where to stop the "on" portion of the throttle bar
        float on_end_x = (mthrpts.ur.x - mthrpts.ul.x) // width of bar
            * mThrottle // scaled to throttle
            + mthrpts.ul.x; // Offset from the left

        mpaths.ThrottleOn.reset();
        mpaths.ThrottleOn.moveTo(mthrpts.ul.x, mthrpts.ul.y);
        mpaths.ThrottleOn.lineTo(mthrpts.ll.x, mthrpts.ll.y);
        mpaths.ThrottleOn.lineTo(on_end_x, mthrpts.ll.y); // stop position
        mpaths.ThrottleOn.lineTo(on_end_x, mthrpts.ul.y);
        mpaths.ThrottleOn.close();
        canvas.drawPath(mpaths.ThrottleOn, mpaints.ThrottleOn);

        mpaths.ThrottleOff.reset();
        mpaths.ThrottleOff.moveTo(mthrpts.ur.x, mthrpts.ur.y);
        mpaths.ThrottleOff.lineTo(mthrpts.lr.x, mthrpts.lr.y);
        mpaths.ThrottleOff.lineTo(on_end_x, mthrpts.lr.y); // stop position
        mpaths.ThrottleOff.lineTo(on_end_x, mthrpts.ur.y);
        mpaths.ThrottleOff.close();
        canvas.drawPath(mpaths.ThrottleOff, mpaints.ThrottleOff);

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

        for (int i = 0; i < NUM_LED_BARS; i++) {
            canvas.drawLine(mbarpts.leftwall, mbarpts.barpts[i].y,
                    (msppts.center.x - mbarpts.barpts[i].x),mbarpts.barpts[i].y,
                    mpaints.BattPwrLo);
        }
    }

    private void drawPower(Canvas canvas) {

    }

}
