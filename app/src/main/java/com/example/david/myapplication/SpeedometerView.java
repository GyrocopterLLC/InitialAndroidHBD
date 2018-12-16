package com.example.david.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class SpeedometerView extends View {
    private static final String TAG = SpeedometerView.class.getSimpleName();
    public static final float DEFAULT_MAX_SPEED = 300; // Assuming this is km/h and you drive a super-car
    private final int preferredSize = 300; // If something about the view drawing gets screwy, just choose this size

    // Speedometer internal state
    private float mMaxSpeed;
    private float mCurrentSpeed;

    // Scale drawing tools
    private Paint onMarkPaint;
    private Paint offMarkPaint;
    private Paint scalePaint;
    private Paint readingPaint;
    private Path onPath;
    private Path offPath;
    final RectF oval = new RectF();

    // Drawing colors
    private int ON_COLOR = Color.argb(255, 0xff, 0xA5, 0x00);
    private int OFF_COLOR = Color.argb(255,0x3e,0x3e,0x3e);
    private int SCALE_COLOR = Color.argb(255, 255, 255, 255);
    private int TEXT_SHADOW_COLOR = Color.RED;
    private int SCALE_SPACING = 20;
    private float SCALE_SIZE = 14f;
    private float READING_SIZE = 60f;

    // Scale configuration
    private float centerX;
    private float centerY;
    private float radius;
    public SpeedometerView(Context context) {
        super(context);
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.SpeedometerView,
                0, 0);
        try{
            mMaxSpeed = a.getFloat(R.styleable.SpeedometerView_max_speed, DEFAULT_MAX_SPEED);
            mCurrentSpeed = a.getFloat(R.styleable.SpeedometerView_current_speed, 0);
            ON_COLOR = a.getColor(R.styleable.SpeedometerView_on_color, ON_COLOR);
            OFF_COLOR = a.getColor(R.styleable.SpeedometerView_off_color, OFF_COLOR);
            SCALE_COLOR = a.getColor(R.styleable.SpeedometerView_scale_color, SCALE_COLOR);
            SCALE_SIZE = a.getDimension(R.styleable.SpeedometerView_scale_text_size, SCALE_SIZE);
            READING_SIZE = a.getDimension(R.styleable.SpeedometerView_reading_text_size, READING_SIZE);
            SCALE_SPACING = a.getInt(R.styleable.SpeedometerView_scale_spacing, SCALE_SPACING);
            TEXT_SHADOW_COLOR = a.getColor(R.styleable.SpeedometerView_text_shadow_color, TEXT_SHADOW_COLOR);
        } finally{
            a.recycle();
        }
        initDrawingTools();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int chosenHeight;
        int chosenWidth;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if(widthMode == MeasureSpec.UNSPECIFIED) {
            chosenWidth = preferredSize;
        } else {
            // Either for MeasureSpec.EXACTLY or MeasureSpec.AT_MOST
            // Width takes precedence over height
            chosenWidth = widthSize;
        }

        if(heightMode == MeasureSpec.UNSPECIFIED) {
            chosenHeight = preferredSize;
        } else {
            if(heightMode == MeasureSpec.EXACTLY) {
                chosenHeight = heightSize;
            } else {
                // MeasureSpec.AT_MOST
                // Choose a height appropriate for the given width
                // Since the View is mostly a semicircle, it should be about
                // twice as wide as tall. Aiming for a 16:9 ratio
                chosenHeight = chosenWidth * 9 / 16;
            }
        }
        // Choose center of speedometer circle. This is where the current speed is printed, too
        centerX = chosenWidth / 2;
        // Near the bottom of the View. About 95% down.
        centerY = chosenHeight * 95 / 100;
        setMeasuredDimension(chosenWidth, chosenHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawScaleBackground(canvas);
        drawScale(canvas);
        drawLegend(canvas);
        drawReading(canvas);

    }

    private void drawScaleBackground(Canvas canvas){
        offPath.reset();
        for(int i = -180; i < 0; i+=4){
            offPath.addArc(oval, i, 2f);
        }
        canvas.drawPath(offPath, offMarkPaint);
    }

    private void drawScale(Canvas canvas){
        onPath.reset();
        for(int i = -180; i < (mCurrentSpeed/mMaxSpeed)*180 - 180; i+=4){
            onPath.addArc(oval, i, 2f);
        }
        canvas.drawPath(onPath, onMarkPaint);
    }

    private void drawLegend(Canvas canvas){
        canvas.save();
        canvas.rotate(-180, centerX,centerY);
        Path circle = new Path();
        double halfCircumference = radius * Math.PI;
        double increments = SCALE_SPACING;
        for(int i = 0; i < this.mMaxSpeed; i += increments){
            circle.addCircle(centerX, centerY, radius, Path.Direction.CW);
            canvas.drawTextOnPath(String.format("%d", i),
                    circle,
                    (float) (i*halfCircumference/this.mMaxSpeed),
                    -30f,
                    scalePaint);
        }

        canvas.restore();
    }

    private void drawReading(Canvas canvas){
        Path path = new Path();
        String message = String.format("%d mph", (int)this.mCurrentSpeed);
        float[] widths = new float[message.length()];
        readingPaint.getTextWidths(message, widths);
        float advance = 0;
        for(double width:widths)
            advance += width;
        path.moveTo(centerX - advance/2, centerY);
        path.lineTo(centerX + advance/2, centerY);
        canvas.drawTextOnPath(message, path, 0f, 0f, readingPaint);
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {

        // Setting up the oval area in which the arc will be drawn
        // Oval will take up ~80% of the View's area
        if (width > height){
            radius = width*4/10;
        }else{
            // Gotta squish it in
            radius = height/4;
        }
        oval.set(centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);
    }

    private void initDrawingTools(){
        onMarkPaint = new Paint();
        onMarkPaint.setStyle(Paint.Style.STROKE);
        onMarkPaint.setColor(ON_COLOR);
        onMarkPaint.setStrokeWidth(35f);
        onMarkPaint.setShadowLayer(5f, 0f, 0f, ON_COLOR);
        onMarkPaint.setAntiAlias(true);

        offMarkPaint = new Paint(onMarkPaint);
        offMarkPaint.setColor(OFF_COLOR);
        offMarkPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        offMarkPaint.setShadowLayer(0f, 0f, 0f, OFF_COLOR);

        scalePaint = new Paint(offMarkPaint);
        scalePaint.setStrokeWidth(2f);
        scalePaint.setTextSize(SCALE_SIZE);
        scalePaint.setShadowLayer(5f, 0f, 0f, TEXT_SHADOW_COLOR);
        scalePaint.setColor(SCALE_COLOR);

        readingPaint = new Paint(scalePaint);
        readingPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        readingPaint.setShadowLayer(3f, 0f, 0f, TEXT_SHADOW_COLOR);
        readingPaint.setTextSize(65f);
        readingPaint.setTypeface(Typeface.SANS_SERIF);
        readingPaint.setColor(SCALE_COLOR);

        onPath = new Path();
        offPath = new Path();
    }

    public float getCurrentSpeed() {
        return mCurrentSpeed;
    }

    public void setCurrentSpeed(float mCurrentSpeed) {
        if(mCurrentSpeed != this.mCurrentSpeed) {
            if (mCurrentSpeed > this.mMaxSpeed)
                this.mCurrentSpeed = mMaxSpeed;
            else if (mCurrentSpeed < 0)
                this.mCurrentSpeed = 0;
            else
                this.mCurrentSpeed = mCurrentSpeed;
            this.invalidate();
        }
    }

    public void setColors(int onColor, int offColor)
    {
        ON_COLOR = onColor;
        OFF_COLOR = offColor;
        initDrawingTools();
    }

}

