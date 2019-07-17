package com.example.david.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class GaugeView extends View {
    private static final String TAG = GaugeView.class.getSimpleName();
    public static final float DEFAULT_MAX_VALUE = 300; // Assuming this is km/h and you drive a super-car
    private final int preferredSize = 300; // If something about the view drawing gets screwy, just choose this size

    // Gauge internal state
    private float mMaxValue;
    private float mCurrentValue;

    // Scale drawing tools
    private Paint onMarkPaint;
    private Paint offMarkPaint;
    private Paint scalePaint;
    private Paint readingPaint;
    private Paint valueTickPaint;
    private Path onPath;
    private Path offPath;
    final RectF oval = new RectF();

    // Drawing colors
    private int ON_COLOR = Color.argb(255, 0xff, 0xA5, 0x00);
    private int OFF_COLOR = Color.argb(255,0x3e,0x3e,0x3e);
    private int TEXT_COLOR = Color.argb(255, 255, 255, 255);
    private int SCALE_SPACING = 20;
    private float SCALE_TEXT_SIZE = 14f;
    private float TEXT_SIZE = 60f;
    private String UNITS;
    private boolean BIDIRECTIONAL = false;
    private boolean DISPLAY_SCALE = false;
    private float MIN_DISPLAYABLE_VALUE = 0.25f;

    // Background for speedy drawing
    private Bitmap mBackground = null;

    // Scale configuration
    private float centerX;
    private float centerY;
    private float radius;
    public GaugeView(Context context) {
        super(context);
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.GaugeView,
                0, 0);
        try{
            mMaxValue = a.getFloat(R.styleable.GaugeView_max_value, DEFAULT_MAX_VALUE);
            UNITS = a.getString(R.styleable.GaugeView_units);
            mCurrentValue = a.getFloat(R.styleable.GaugeView_current_value, 0);
            ON_COLOR = a.getColor(R.styleable.GaugeView_on_color, ON_COLOR);
            OFF_COLOR = a.getColor(R.styleable.GaugeView_off_color, OFF_COLOR);
            TEXT_COLOR = a.getColor(R.styleable.GaugeView_text_color, TEXT_COLOR);
            TEXT_SIZE = a.getDimension(R.styleable.GaugeView_text_size, TEXT_SIZE);
            DISPLAY_SCALE = a.getBoolean(R.styleable.GaugeView_display_scale, DISPLAY_SCALE);
            SCALE_TEXT_SIZE = a.getDimension(R.styleable.GaugeView_scale_text_size, SCALE_TEXT_SIZE);
            SCALE_SPACING = a.getInt(R.styleable.GaugeView_scale_spacing, SCALE_SPACING);
            BIDIRECTIONAL = a.getBoolean(R.styleable.GaugeView_bidirectional, BIDIRECTIONAL);

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
        // Choose center of gauge circle. This is where the current value is printed, too
        centerX = chosenWidth / 2;
        // Near the bottom of the View. About 95% down.
        centerY = chosenHeight * 95 / 100;
        setMeasuredDimension(chosenWidth, chosenHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(DISPLAY_SCALE) {
            if (mBackground == null) {
                mBackground = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas tempDrawing = new Canvas(mBackground);
                drawScale(tempDrawing);
            }
            canvas.drawBitmap(mBackground, 0, 0, null);
        }

        drawArcBackground(canvas);
        drawActiveArc(canvas);
        drawReading(canvas);

    }

    private void drawArcBackground(Canvas canvas){
        // Draws the background of the gauge arc
        offPath.reset();
        if(BIDIRECTIONAL) {
            if(Math.abs(mCurrentValue) < MIN_DISPLAYABLE_VALUE) {
                // Too small to be displayed, show entire field blank
                offPath.addArc(oval, 180, 180);
            }
            else if(mCurrentValue < 0) {
                // Negative value, will grow to left
                // Show entire right-half
                offPath.addArc(oval, 270,90);
                // Show portion of left-half until the value
                offPath.addArc(oval, 180, 90*(1-Math.abs(mCurrentValue/mMaxValue)));
            } else {
                // Positive value, will grow to right
                // Show entire left-half
                offPath.addArc(oval, 180,90);
                // Show portion of right half past the value
                offPath.addArc(oval, 270+90*mCurrentValue/mMaxValue, 90*(1-mCurrentValue/mMaxValue));
            }
            canvas.drawPath(offPath, offMarkPaint);
        } else {
            if (Math.abs(mCurrentValue - mMaxValue) > MIN_DISPLAYABLE_VALUE) {
                offPath.addArc(oval, 180 * (1 + mCurrentValue / mMaxValue), 180 * (1 - mCurrentValue / mMaxValue));
                canvas.drawPath(offPath, offMarkPaint);
            }
        }
    }

    private void drawActiveArc(Canvas canvas){
        // Draws an arc representing the value and a line at the value
        onPath.reset();
        if(BIDIRECTIONAL) {
            if(Math.abs(mCurrentValue) > MIN_DISPLAYABLE_VALUE) {
                if (mCurrentValue < 0) {
                    onPath.addArc(oval, 270 - (90 * (Math.abs(mCurrentValue) / mMaxValue)), 90 * Math.abs(mCurrentValue) / mMaxValue);
                } else {
                    onPath.addArc(oval, 270, 90 * mCurrentValue / mMaxValue);
                }
                canvas.drawPath(onPath,onMarkPaint);
            }
            double x1,y1,x2,y2;
            double angle = 90 - 90*mCurrentValue/mMaxValue;
            x1 = (radius-50) * Math.cos(Math.PI*angle/180) + centerX;
            x2 = (radius+20) * Math.cos(Math.PI*angle/180) + centerX;
            y1 = centerY - (radius-50) * Math.sin(Math.PI*angle/180);
            y2 = centerY - (radius+20) * Math.sin(Math.PI*angle/180);
            canvas.drawLine((float)x1,(float)y1,(float)x2,(float)y2,valueTickPaint);
        } else {
            if (Math.abs(mCurrentValue) > MIN_DISPLAYABLE_VALUE) {
                onPath.addArc(oval, 180, (mCurrentValue / mMaxValue) * 180);
                canvas.drawPath(onPath, onMarkPaint);
            }
            double x1,y1,x2,y2;
            double angle = 180 - 180*mCurrentValue/mMaxValue;
            x1 = (radius-50) * Math.cos(Math.PI*angle/180) + centerX;
            x2 = (radius+20) * Math.cos(Math.PI*angle/180) + centerX;
            y1 = centerY - (radius-50) * Math.sin(Math.PI*angle/180);
            y2 = centerY - (radius+20) * Math.sin(Math.PI*angle/180);
            canvas.drawLine((float)x1,(float)y1,(float)x2,(float)y2,valueTickPaint);
        }
    }

    private void drawScale(Canvas canvas){
        canvas.save();
        canvas.rotate(-180, centerX,centerY);
        Path circle = new Path();
        double quarterCircumference = radius * Math.PI / 2.0;
        double increments = SCALE_SPACING;
        double halfCircumference = radius * Math.PI;
        String message;
        float[] widths;
        float advance;
        if(BIDIRECTIONAL) {
            circle.addCircle(centerX, centerY, radius, Path.Direction.CW);
            // Draw the first entry (negative max value)
            canvas.drawTextOnPath(String.format("%d",(int)(-this.mMaxValue)),
                    circle,
                    0f,
                    -30f,
                    scalePaint);
            // Draw entries up to but not including zero

            for(int i = (int)(-this.mMaxValue+increments); i < 0; i += increments) {
                message = String.format("%d", i);
                widths = new float[message.length()];
                scalePaint.getTextWidths(message, widths);
                advance = 0;
                for(float wid: widths) advance += wid;
                canvas.drawTextOnPath(String.format("%d",i),
                        circle,
                        (float)quarterCircumference + (float)(i*quarterCircumference/this.mMaxValue)-advance/2f,
                        -30f,
                        scalePaint);
            }
            // Make sure that zero is printed
            message = String.format("%d",0);
            widths = new float[message.length()];
            scalePaint.getTextWidths(message, widths);
            advance = 0;
            for(float wid: widths) advance += wid;
            canvas.drawTextOnPath(String.format("%d",0),
                    circle,
                    (float)quarterCircumference - advance / 2f,
                    -30f,
                    scalePaint);
            // Print remaining up to but not including max value
            for(int i = (int)(increments); i < (int)this.mMaxValue; i += increments) {
                message = String.format("%d", i);
                widths = new float[message.length()];
                scalePaint.getTextWidths(message, widths);
                advance = 0;
                for(float wid: widths) advance += wid;
                canvas.drawTextOnPath(String.format("%d",i),
                        circle,
                        (float)quarterCircumference + (float)(i*quarterCircumference/this.mMaxValue)-advance/2f,
                        -30f,
                        scalePaint);
            }
            // Print max value
        } else {

            circle.addCircle(centerX, centerY, radius, Path.Direction.CW);
            // First entry (zero) needs no offset
            canvas.drawTextOnPath(String.format("%d",0),
                    circle,
                    0f,
                    -30f,
                    scalePaint);
            // Draw remaining numbers up to but not including max
            for (int i = (int)increments; i < (int)this.mMaxValue; i += (int)increments) {
                message = String.format("%d", i);
                widths = new float[message.length()];
                scalePaint.getTextWidths(message,widths);
                advance = 0;
                for(double wid:widths) advance += wid;
                canvas.drawTextOnPath(message,
                        circle,
                        (float) (i * halfCircumference / this.mMaxValue) - advance/2f,
                        -30f,
                        scalePaint);
            }

            //circle.addCircle(centerX, centerY, radius, Path.Direction.CW);

        }
        // Add final entry for maximum value
        message = String.format("%d", (int) this.mMaxValue);
        widths = new float[message.length()];
        scalePaint.getTextWidths(message, widths);
        advance = 0;
        for (double wid : widths) advance += wid;
        canvas.drawTextOnPath(String.format("%d", (int) this.mMaxValue),
                circle,
                ((float) (halfCircumference)) - advance,
                -30f,
                scalePaint);


        canvas.restore();
    }

    private void drawReading(Canvas canvas){
        Path path = new Path();
        String message;
        if(UNITS == null) message = String.format("%d", (int)this.mCurrentValue);
        else message = String.format("%d %s",(int)this.mCurrentValue, UNITS);
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
        radius = width/2 - 30.0f - SCALE_TEXT_SIZE;  // Subtract the width of the scale text and
                                                // its offset

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
        offMarkPaint.setStyle(Paint.Style.STROKE);
        offMarkPaint.setShadowLayer(0f, 0f, 0f, OFF_COLOR);

        scalePaint = new Paint(offMarkPaint);
        scalePaint.setStrokeWidth(2f);
        scalePaint.setTextSize(SCALE_TEXT_SIZE);
        scalePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        scalePaint.setColor(TEXT_COLOR);

        readingPaint = new Paint(scalePaint);
        readingPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        readingPaint.setTextSize(TEXT_SIZE);
        readingPaint.setTypeface(Typeface.SANS_SERIF);
        readingPaint.setColor(TEXT_COLOR);

        valueTickPaint = new Paint();
        valueTickPaint.setStyle(Paint.Style.STROKE);
        valueTickPaint.setColor(TEXT_COLOR);
        valueTickPaint.setStrokeWidth(5f);

        onPath = new Path();
        offPath = new Path();
    }

    public float getCurrentValue() {
        return mCurrentValue;
    }

    public float getMaxValue() {
        return mMaxValue;
    }

    public void setCurrentValue(float currentValue) {
        if(currentValue != mCurrentValue) {
            // Saturate between -max and +max for bidirection, 0 and +max for unidirectional
            if(BIDIRECTIONAL) {
                currentValue = Math.min(currentValue, mMaxValue);
                currentValue = Math.max(currentValue, -mMaxValue);
                mCurrentValue = currentValue;
            } else {
                currentValue = Math.min(currentValue, mMaxValue);
                currentValue = Math.max(currentValue, 0);
                mCurrentValue = currentValue;
            }
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

