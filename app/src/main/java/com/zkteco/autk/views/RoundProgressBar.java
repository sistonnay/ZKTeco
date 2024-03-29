package com.zkteco.autk.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.zkteco.autk.R;

/**
 * Created by rocky on 17-12-28.
 */
public class RoundProgressBar extends View {

    private static final String TAG = RoundProgressBar.class.getSimpleName();

    /**
     * For save and restore instance of progressbar.
     */
    private static final String INSTANCE_STATE = "saved_instance";
    private static final String INSTANCE_TEXT_COLOR = "text_color";
    private static final String INSTANCE_TEXT_SIZE = "text_size";
    private static final String INSTANCE_REACHED_BAR_HEIGHT = "reached_bar_height";
    private static final String INSTANCE_REACHED_BAR_COLOR = "reached_bar_color";
    private static final String INSTANCE_UNREACHED_BAR_HEIGHT = "unreached_bar_height";
    private static final String INSTANCE_UNREACHED_BAR_COLOR = "unreached_bar_color";
    private static final String INSTANCE_MAX = "max";
    private static final String INSTANCE_PROGRESS = "progress";
    private static final String INSTANCE_SUFFIX = "suffix";
    private static final String INSTANCE_PREFIX = "prefix";
    private static final String INSTANCE_TEXT_VISIBILITY = "text_visibility";

    private static final int PROGRESS_TEXT_VISIBLE = 0;

    private final int default_text_color = Color.rgb(66, 145, 241);
    private final int default_reached_color = Color.rgb(66, 145, 241);
    private final int default_unreached_color = Color.rgb(204, 204, 204);

    private final float default_progress_text_offset;
    private final float default_text_size;
    private final float default_reached_bar_height;
    private final float default_unreached_bar_height;
    private final float default_round_width;

    private int mMaxProgress = 100;
    private float mRoundWidth;
    /**
     * Current progress, can not exceed the max progress.
     */
    private int mCurrentProgress = 0;
    /**
     * The progress area bar color.
     */
    private int mReachedBarColor;
    /**
     * The bar unreached area color.
     */
    private int mUnreachedBarColor;
    /**
     * The progress text color.
     */
    private int mTextColor;
    /**
     * The progress text size.
     */
    private float mTextSize;
    /**
     * The height of the reached area.
     */
    private float mReachedBarHeight;
    /**
     * The height of the unreached area.
     */
    private float mUnreachedBarHeight;
    /**
     * The suffix of the number.
     */
    private String mSuffix = "%";
    /**
     * The prefix.
     */
    private String mPrefix = "";
    /**
     * The width of the text that to be drawn.
     */
    private float mDrawTextWidth;

    /**
     * The text that to be drawn in onDraw().
     */
    private String mCurrentDrawText;
    /**
     * The drawn text start.
     */
    private float mDrawTextStart;

    /**
     * The drawn text end.
     */
    private float mDrawTextEnd;
    /**
     * The Paint of the reached area.
     */
    private Paint mReachedBarPaint;
    /**
     * The Paint of the unreached area.
     */
    private Paint mUnreachedBarPaint;
    /**
     * The Paint of the progress text.
     */
    private Paint mTextPaint;

    /**
     * Unreached bar area to draw rect.
     */
    private RectF mRectF = new RectF(0, 0, 0, 0);
    /**
     * The progress text offset.
     */
    private float mOffset;
    /**
     * Determine if need to draw unreached area.
     */
    private boolean mDrawUnreachedBar = true;

    private boolean mDrawReachedBar = true;

    private boolean mIfDrawText = true;

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.roundProgressBarStyle);
    }

    public RoundProgressBar(Context context, AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        default_reached_bar_height = dp2px(1.5f);
        default_unreached_bar_height = dp2px(1.0f);
        default_text_size = sp2px(10);
        default_progress_text_offset = dp2px(3.0f);
        default_round_width = dp2px(30.0f);

        // load styled attributes.
        final TypedArray attributes = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.RoundProgressBar,
                        defStyleAttr, 0);

        mReachedBarColor = attributes.getColor(
                R.styleable.RoundProgressBar_progress_reached_color,
                default_reached_color);
        mUnreachedBarColor = attributes.getColor(
                R.styleable.RoundProgressBar_progress_unreached_color,
                default_unreached_color);
        mTextColor = attributes.getColor(
                R.styleable.RoundProgressBar_progress_text_color,
                default_text_color);
        mTextSize = attributes.getDimension(
                R.styleable.RoundProgressBar_progress_text_size,
                default_text_size);

        mReachedBarHeight = attributes.getDimension(
                R.styleable.RoundProgressBar_progress_reached_bar_height,
                default_reached_bar_height);
        mUnreachedBarHeight = attributes.getDimension(
                R.styleable.RoundProgressBar_progress_unreached_bar_height,
                default_unreached_bar_height);
        mOffset = attributes.getDimension(
                R.styleable.RoundProgressBar_progress_text_offset,
                default_progress_text_offset);
        mRoundWidth = attributes.getDimension(
                R.styleable.RoundProgressBar_progress_round_width,
                default_round_width);

        int textVisible = attributes.getInt(
                R.styleable.RoundProgressBar_progress_text_visibility,
                PROGRESS_TEXT_VISIBLE);
        if (textVisible != PROGRESS_TEXT_VISIBLE) {
            mIfDrawText = false;
        }

        setProgress(attributes.getInt(
                R.styleable.RoundProgressBar_progress_current, 0));
        setMax(attributes
                .getInt(R.styleable.RoundProgressBar_progress_max, 100));

        attributes.recycle();
        initializePainters();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "Current Progress:" + mCurrentProgress);

        if (mIfDrawText) {
            calculateDrawRectF();
        } else {
            calculateDrawRectFWithoutProgressText();
        }
        if (mDrawReachedBar) {
            canvas.drawArc(mRectF, 0, 360 * mCurrentProgress / mMaxProgress,
                    false, mReachedBarPaint);
        }

        if (mDrawUnreachedBar) {
            canvas.drawArc(mRectF, 360 * mCurrentProgress / mMaxProgress,
                    (360 - 360 * mCurrentProgress / mMaxProgress), false,
                    mUnreachedBarPaint);
        }

        if (mIfDrawText)
            canvas.drawText(mCurrentDrawText, mDrawTextStart, mDrawTextEnd,
                    mTextPaint);
    }

    private void calculateDrawRectFWithoutProgressText() {
        int center = getWidth() / 2; // 获取圆心的x坐标
        int radius = (int) (center - mRoundWidth / 2); // 圆环的半径

        mRectF.left = center - radius;
        mRectF.top = center - radius;
        mRectF.right = center + radius;
        mRectF.bottom = center + radius;
    }

    private void calculateDrawRectF() {

        mCurrentDrawText = String.format("%d", getProgress() * 100 / getMax());
        mCurrentDrawText = mPrefix + mCurrentDrawText + mSuffix;
        mDrawTextWidth = mTextPaint.measureText(mCurrentDrawText);

        int center = getWidth() / 2; // 获取圆心的x坐标
        int radius = (int) (center - mRoundWidth / 2); // 圆环的半径

        if (getProgress() == 0) {
            mDrawReachedBar = false;
            mDrawTextStart = getPaddingLeft();
        } else {
            mDrawReachedBar = true;
            mRectF.left = center - radius;
            mRectF.top = center - radius;
            mRectF.right = center + radius;
            mRectF.bottom = center + radius;

            // 计算位置，文字居中
            FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
            float baseline = mRectF.top
                    + (mRectF.bottom - mRectF.top - fontMetrics.bottom + fontMetrics.top)
                    / 2 - fontMetrics.top;

            mDrawTextStart = mRectF.centerX();
            mDrawTextEnd = baseline;
        }

        float unreachedBarStart = mDrawTextStart + mDrawTextWidth + mOffset;
        if (unreachedBarStart >= getWidth() - getPaddingRight()) {
            mDrawUnreachedBar = false;
        } else {
            mDrawUnreachedBar = true;
        }
    }

    private void initializePainters() {
        mReachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReachedBarPaint.setColor(mReachedBarColor);
        mReachedBarPaint.setStyle(Paint.Style.STROKE);
        mReachedBarPaint.setAntiAlias(true);
        mReachedBarPaint.setStrokeWidth(10);

        mUnreachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnreachedBarPaint.setColor(mUnreachedBarColor);
        mUnreachedBarPaint.setStyle(Paint.Style.STROKE);
        mUnreachedBarPaint.setAntiAlias(true);
        mUnreachedBarPaint.setStrokeWidth(10);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setStrokeWidth(10);
    }

    /**
     * Get progress text color.
     *
     * @return progress text color.
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * Get progress text size.
     *
     * @return progress text size.
     */
    public float getProgressTextSize() {
        return mTextSize;
    }

    public void setProgressTextSize(float textSize) {
        this.mTextSize = textSize;
        mTextPaint.setTextSize(mTextSize);
        invalidate();
    }

    public int getUnreachedBarColor() {
        return mUnreachedBarColor;
    }

    public void setUnreachedBarColor(int barColor) {
        this.mUnreachedBarColor = barColor;
        mUnreachedBarPaint.setColor(mReachedBarColor);
        invalidate();
    }

    public int getReachedBarColor() {
        return mReachedBarColor;
    }

    public void setReachedBarColor(int progressColor) {
        this.mReachedBarColor = progressColor;
        mReachedBarPaint.setColor(mReachedBarColor);
        invalidate();
    }

    public int getProgress() {
        return mCurrentProgress;
    }

    public void setProgress(int progress) {
        if (progress <= getMax() && progress >= 0) {
            this.mCurrentProgress = progress;
            postInvalidate();
        }
    }

    public int getMax() {
        return mMaxProgress;
    }

    public void setMax(int maxProgress) {
        if (maxProgress > 0) {
            this.mMaxProgress = maxProgress;
            invalidate();
        }
    }

    public float getReachedBarHeight() {
        return mReachedBarHeight;
    }

    /**
     * @param height
     */
    public void setReachedBarHeight(float height) {
        mReachedBarHeight = height;
    }

    public float getUnreachedBarHeight() {
        return mUnreachedBarHeight;
    }

    /**
     * @param height
     */
    public void setUnreachedBarHeight(float height) {
        mUnreachedBarHeight = height;
    }

    public void setProgressTextColor(int textColor) {
        this.mTextColor = textColor;
        mTextPaint.setColor(mTextColor);
        invalidate();
    }

    public String getSuffix() {
        return mSuffix;
    }

    public void setSuffix(String suffix) {
        if (suffix == null) {
            mSuffix = "";
        } else {
            mSuffix = suffix;
        }
    }

    public String getPrefix() {
        return mPrefix;
    }

    public void setPrefix(String prefix) {
        if (prefix == null)
            mPrefix = "";
        else {
            mPrefix = prefix;
        }
    }

    public float dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public float sp2px(float sp) {
        final float scale = getResources().getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public boolean getProgressTextVisibility() {
        return mIfDrawText;
    }

    public void setProgressTextVisibility(ProgressTextVisibility visibility) {
        mIfDrawText = visibility == ProgressTextVisibility.Visible;
        postInvalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor());
        bundle.putFloat(INSTANCE_TEXT_SIZE, getProgressTextSize());
        bundle.putFloat(INSTANCE_REACHED_BAR_HEIGHT, getReachedBarHeight());
        bundle.putFloat(INSTANCE_UNREACHED_BAR_HEIGHT, getUnreachedBarHeight());
        bundle.putInt(INSTANCE_REACHED_BAR_COLOR, getReachedBarColor());
        bundle.putInt(INSTANCE_UNREACHED_BAR_COLOR, getUnreachedBarColor());
        bundle.putInt(INSTANCE_MAX, getMax());
        bundle.putInt(INSTANCE_PROGRESS, getProgress());
        bundle.putString(INSTANCE_SUFFIX, getSuffix());
        bundle.putString(INSTANCE_PREFIX, getPrefix());
        bundle.putBoolean(INSTANCE_TEXT_VISIBILITY, getProgressTextVisibility());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mTextColor = bundle.getInt(INSTANCE_TEXT_COLOR);
            mTextSize = bundle.getFloat(INSTANCE_TEXT_SIZE);
            mReachedBarHeight = bundle.getFloat(INSTANCE_REACHED_BAR_HEIGHT);
            mUnreachedBarHeight = bundle
                    .getFloat(INSTANCE_UNREACHED_BAR_HEIGHT);
            mReachedBarColor = bundle.getInt(INSTANCE_REACHED_BAR_COLOR);
            mUnreachedBarColor = bundle.getInt(INSTANCE_UNREACHED_BAR_COLOR);
            initializePainters();
            setMax(bundle.getInt(INSTANCE_MAX));
            setProgress(bundle.getInt(INSTANCE_PROGRESS));
            setPrefix(bundle.getString(INSTANCE_PREFIX));
            setSuffix(bundle.getString(INSTANCE_SUFFIX));
            setProgressTextVisibility(bundle
                    .getBoolean(INSTANCE_TEXT_VISIBILITY) ? ProgressTextVisibility.Visible
                    : ProgressTextVisibility.Invisible);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public enum ProgressTextVisibility {
        Visible, Invisible
    }
}
