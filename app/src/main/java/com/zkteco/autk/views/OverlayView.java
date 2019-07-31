package com.zkteco.autk.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.zkteco.autk.R;

/**
 * author: Created by Ho Dao on 2019/7/30 0030 21:47
 * email: 372022839@qq.com (github: sistonnay)
 */
public class OverlayView extends ImageView {

    private Paint mPaint;
    private OverlayTheme mTheme = new OverlayTheme();

    public OverlayView(Context context) {
        this(context, null);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.OverlayView,
                defStyleAttr, 0);
        mTheme = getThemeFromTypedArray(attributes);

        mPaint = new Paint();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null); //开启硬件离屏缓存，否则PorterDuff.Mode.CLEAR为黑色
    }

    public OverlayTheme getThemeFromTypedArray(TypedArray ta) {
        OverlayTheme theme = new OverlayTheme();
        if (ta != null) {
            theme.circleWidth = ta.getDimension(
                    R.styleable.OverlayView_circle_edge_width, theme.circleWidth);
            theme.circleEdgeColor = ta.getColor(
                    R.styleable.OverlayView_circle_edge_color, theme.circleEdgeColor);
            theme.circleFillColor = ta.getColor(
                    R.styleable.OverlayView_circle_fill_color, theme.circleFillColor);
            theme.blankPlaceBackground = ta.getDrawable(
                    R.styleable.OverlayView_blank_place_background);
            if (theme.blankPlaceBackground == null) {
                theme.blankPlaceBackground = new ColorDrawable(Color.parseColor("#FFFF5722"));
            }
        }
        return theme;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawOverlay(canvas);
        super.onDraw(canvas);
    }

    private void drawOverlay(Canvas canvas) {
        mPaint.reset();
        final Rect dstRect = new Rect(0, 0, getWidth(), getHeight());
        if (mTheme.blankPlaceBackground instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) mTheme.blankPlaceBackground).getBitmap();
            final Rect resRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, resRect, dstRect, mPaint);
        } else if (mTheme.blankPlaceBackground instanceof ColorDrawable) {
            int color = ((ColorDrawable) mTheme.blankPlaceBackground).getColor();
            canvas.drawColor(color);
        } else {
            int color = Color.parseColor("#FFFF5722");
            canvas.drawColor(color);
        }

        int wc = getWidth();
        int hc = getHeight() * 2 / 3;
        int rc = Math.min(wc, hc) * 2 / 3 / 2;
        int cx = wc / 2;
        int cy = hc / 2;

        mPaint.reset();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR/*DST_OUT*/)); //PorterDuff.Mode.CLEAR为抠空，不上色
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mTheme.circleFillColor);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(cx, cy, rc, mPaint);

        mPaint.reset();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mTheme.circleWidth);
        mPaint.setColor(mTheme.circleEdgeColor);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(cx, cy, rc, mPaint);
    }

    public void setTheme(OverlayTheme theme) {
        if (theme != null) {
            mTheme = theme;
            invalidate();
        }
    }

    public static class OverlayTheme {
        public float circleWidth = 5;
        public int circleEdgeColor = Color.parseColor("#FF4CAF50");
        public int circleFillColor = Color.parseColor("#8000BCD4");
        public Drawable blankPlaceBackground = new ColorDrawable(Color.parseColor("#FFFF5722"));
    }
}
