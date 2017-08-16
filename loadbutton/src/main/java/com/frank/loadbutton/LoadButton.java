package com.frank.loadbutton;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by frank on 2017/5/16.
 */

public class LoadButton extends View {
    private static final String TAG = "LoadButton";

    private final int mStrokeColor;
    private final int mTextColor;
    private final float mProgressWidth;
    private OnClickListener mListenner;
    private Paint mPaint;

    private int mDefaultWidth;
    private int mDefaultRadiu;
    //  中间矩形的宽度
    private int rectWidth;

    private TextPaint mTextPaint;

    private int mDefaultTextSize;

    private int mTopBottomPadding;
    private int mLeftRightPadding;

    private String mText;

    private int mTextWidth;
    private int mTextSize;
    private int mRadiu;


    private Path mPath;

    private RectF leftRect;
    private RectF rightRect;
    private RectF contentRect;
    private RectF progressRect;

    private int left;
    private int right;
    private int top;
    private int bottom;

    private boolean isUnfold;

    private int mBackgroundColor;

    private State mCurrentState;
    private float circleSweep;
    private ObjectAnimator loadAnimator;
    private ObjectAnimator shrinkAnim;

    private Drawable mSuccessedDrawable;
    private Drawable mErrorDrawable;
    private Drawable mPauseDrawable;

    private boolean progressReverse;
    private int mProgressSecondColor;
    private int mProgressColor;
    private int mProgressStartAngel;

    public LoadListenner getListenner() {
        return mLoadListenner;
    }

    public void setListenner(LoadListenner listenner) {
        this.mLoadListenner = listenner;
    }

    LoadListenner mLoadListenner;

    public void setCircleSweep(float circleSweep) {
        this.circleSweep = circleSweep;
        invaidateSelft();
    }


    enum State {
        INITIAL,
        FODDING,
        LOADDING,
        COMPLETED_ERROR,
        COMPLETED_SUCCESSED,
        LOADDING_PAUSE
    }


    public LoadButton(Context context) {
        this(context, null);
    }

    public LoadButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDefaultRadiu = 40;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadButton);
        mDefaultTextSize = 24;
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.LoadButton_android_textSize,
                mDefaultTextSize);
        mStrokeColor = typedArray.getColor(R.styleable.LoadButton_stroke_color, Color.RED);
        mTextColor = typedArray.getColor(R.styleable.LoadButton_content_color, Color.WHITE);
        mText = typedArray.getString(R.styleable.LoadButton_android_text);
        mRadiu = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_radiu, mDefaultRadiu);
        mTopBottomPadding = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_contentPaddingTB, 10);
        mLeftRightPadding = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_contentPaddingLR, 10);
        mBackgroundColor = typedArray.getColor(R.styleable.LoadButton_backColor, Color.WHITE);
        mProgressColor = typedArray.getColor(R.styleable.LoadButton_progressColor, Color.WHITE);
        mProgressSecondColor = typedArray.getColor(R.styleable.LoadButton_progressSecondColor, Color.parseColor("#c3c3c3"));
        mProgressWidth = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_progressedWidth, 2);

        mSuccessedDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadSuccessDrawable);
        mErrorDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadErrorDrawable);
        mPauseDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadPauseDrawable);
        typedArray.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mStrokeColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mProgressWidth);

        mDefaultWidth = 200;


        mTextPaint = new TextPaint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);


        rectWidth = mDefaultWidth - mDefaultRadiu * 2;

        leftRect = new RectF();
        rightRect = new RectF();
        contentRect = new RectF();
        isUnfold = true;

        mListenner = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentState == State.FODDING) {
                    return;
                }

                if (mCurrentState == State.INITIAL) {
                    if (isUnfold) {
//                        初始化动画
                        shringk();
                    }
                } else if (mCurrentState == State.COMPLETED_ERROR) {

                    if (mLoadListenner != null) {
//                        回调方法，失败标志
                        mLoadListenner.onClick(false);
                    }


                } else if (mCurrentState == State.COMPLETED_SUCCESSED) {
                    if (mLoadListenner != null) {
                        mLoadListenner.onClick(true);
                    }
                } else if (mCurrentState == State.LOADDING_PAUSE) {
                    if (mLoadListenner != null) {
                        mLoadListenner.needLoading();
                        load();
                    }
                } else if (mCurrentState == State.LOADDING) {
                    mCurrentState = State.LOADDING_PAUSE;
                    cancelAnimation();
                    invaidateSelft();
                }

            }
        };

        setOnClickListener(mListenner);

        mCurrentState = State.INITIAL;

        if (mSuccessedDrawable == null) {
            mSuccessedDrawable = context.getResources().getDrawable(R.drawable.yes);
        }
        if (mErrorDrawable == null) {
            mErrorDrawable = context.getResources().getDrawable(R.drawable.no);
        }
        if (mPauseDrawable == null) {
            mPauseDrawable = context.getResources().getDrawable(R.drawable.pause);
        }

        mProgressSecondColor = Color.parseColor("#c3c3c3");
        mProgressColor = Color.WHITE;


    }


    public void reset() {
        mCurrentState = State.INITIAL;
        rectWidth = getWidth() - mRadiu * 2;
        isUnfold = true;
        cancelAnimation();
        invaidateSelft();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        获取父view建议宽度的长度，和模式
//      AT_MOST不超过父view，UNSPECIFIED可以无限大（类似listview等）,EXACTLY精确的px值
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//      实际改控件的宽度，高度
        int resultW = widthSize;
        int resultH = heightSize;

        int contentW = 0;
        int contentH = 0;

        if (widthMode == MeasureSpec.AT_MOST) {
//            文字宽度
            mTextWidth = (int) mTextPaint.measureText(mText);
//            整个控件宽度，文字宽度+与父view的Padding宽度+两个半径宽度
            contentW += mTextWidth + mLeftRightPadding * 2 + mRadiu * 2;
//            如果控件宽度超过父view宽度就设置父view建议宽度为宽度
            resultW = contentW < widthSize ? contentW : widthSize;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            contentH += mTopBottomPadding * 2 + mTextSize;
            resultH = contentH < heightSize ? contentH : heightSize;
        }
//        设置当控件宽高小于圆的直径就让他为圆的直径
        resultW = resultW < 2 * mRadiu ? 2 * mRadiu : resultW;
        resultH = resultH < 2 * mRadiu ? 2 * mRadiu : resultH;
//        设置圆的半径为高度的一半
        mRadiu = resultH / 2;
//        中间矩形的宽度
        rectWidth = resultW - 2 * mRadiu;
//        设置控件的宽度的方法
        setMeasuredDimension(resultW, resultH);
        Log.d(TAG, "onMeasure: w:" + resultW + " h:" + resultH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
//        画控件的轮廓
        drawPath(canvas, cx, cy);
//        画中间的文字或图案
        drawContent(canvas, cx, cy);
    }

    /**
     * 用于画中间的文字和图案的
     *
     * @author IVRING
     * @time 2017/8/16 17:06
     */
    private void drawContent(Canvas canvas, int cx, int cy) {
        int textDescent = (int) mTextPaint.getFontMetrics().descent;
        int textAscent = (int) mTextPaint.getFontMetrics().ascent;
        int delta = Math.abs(textAscent) - textDescent;
        // TODO: 2017/8/16
        int circleR = mRadiu / 2;
//        这个就是将初始时的字体设置为居中显示的，ascent，descent没有看的特别懂
        if (mCurrentState == State.INITIAL) {
            canvas.drawText(mText, cx, cy + delta / 2, mTextPaint);
//            这个是画的旋转的圈圈，没看懂。有时间研究吧
        } else if (mCurrentState == State.LOADDING) {
            if (progressRect == null) {
                progressRect = new RectF();
            }
            progressRect.set(cx - circleR, cy - circleR, cx + circleR, cy + circleR);

            mPaint.setColor(mProgressSecondColor);
            canvas.drawCircle(cx, cy, circleR, mPaint);
            mPaint.setColor(mProgressColor);
            Log.d(TAG, "onDraw() pro:" + progressReverse + " swpeep:" + circleSweep);
            if (circleSweep != 360) {
                mProgressStartAngel = progressReverse ? 270 : (int) (270 + circleSweep);
                canvas.drawArc(progressRect
                        , mProgressStartAngel, progressReverse ? circleSweep : (int) (360 - circleSweep),
                        false, mPaint);
            }

            mPaint.setColor(mBackgroundColor);
//            错误，成功，暂停状态时画个图片
        } else if (mCurrentState == State.COMPLETED_ERROR) {
            mErrorDrawable.setBounds(cx - circleR, cy - circleR, cx + circleR, cy + circleR);
            mErrorDrawable.draw(canvas);
        } else if (mCurrentState == State.COMPLETED_SUCCESSED) {
            mSuccessedDrawable.setBounds(cx - circleR, cy - circleR, cx + circleR, cy + circleR);
            mSuccessedDrawable.draw(canvas);
        } else if (mCurrentState == State.LOADDING_PAUSE) {
            mPauseDrawable.setBounds(cx - circleR, cy - circleR, cx + circleR, cy + circleR);
            mPauseDrawable.draw(canvas);
        }
    }

    /**
     * 画轮廓方法
     *
     * @param canvas 画布
     * @param cx     控件长度的一半
     * @param cy     控件高度的一半
     */
    private void drawPath(Canvas canvas, int cx, int cy) {
        if (mPath == null) {
            mPath = new Path();
        }

        mPath.reset();

//        这个left坐标是相对于父view的坐标，如果空间最大时left其实就是0，这么计算时为了当我们变化内部矩形时，画出的这个轮廓也跟着改变
        left = cx - rectWidth / 2 - mRadiu;
        top = 0;
        right = cx + rectWidth / 2 + mRadiu;
        bottom = getHeight();
//        设置左边的矩形（实际上画出来的时候是圆弧，这个矩形只是为了给画圆弧圈定空间范围）
        leftRect.set(left, top, left + mRadiu * 2, bottom);
//        设置右边的矩形
        rightRect.set(right - mRadiu * 2, top, right, bottom);
//        中间的矩形
        contentRect.set(cx - rectWidth / 2, top, cx + rectWidth / 2, bottom);
//        将path的起点移动到左边圆弧的启点（最下角）
        mPath.moveTo(cx - rectWidth / 2, bottom);
//        画圆弧时是有中心点的，这个中心点其实就是左边矩形的中心。90f代表起点的角度，x轴正方向为0f，顺时针转动的角度。
        mPath.arcTo(leftRect,
                90.0f, 180f);
        mPath.lineTo(cx + rectWidth / 2, top);
        mPath.arcTo(rightRect,
                270.0f, 180f);
//      这个方法是直接将最终点连接到起点。
        mPath.close();


        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBackgroundColor);
        canvas.drawPath(mPath, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mStrokeColor);
    }

    public void setRectWidth(int width) {
        rectWidth = width;
        invaidateSelft();
    }

    private void invaidateSelft() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    /**
     * 属性动画，设置this的rectWidth参数，从最大变到0，（中间矩形缩小为0）
     *
     * @author IVRING
     * @time 2017/8/16 17:21
     */
    public void shringk() {
        if (shrinkAnim == null) {
            shrinkAnim = ObjectAnimator.ofInt(this, "rectWidth", rectWidth, 0);
        }
        shrinkAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isUnfold = false;
                load();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        shrinkAnim.setDuration(500);
        shrinkAnim.start();
        mCurrentState = State.FODDING;
    }

    /**
     * 加载动画，转圈圈
     *
     * @author IVRING
     * @time 2017/8/16 17:19
     */
    public void load() {
        if (loadAnimator == null) {
            loadAnimator = ObjectAnimator.ofFloat(this, "circleSweep", 0, 360);
        }

        loadAnimator.setDuration(1000);
//        设置无限循环
        loadAnimator.setRepeatMode(ValueAnimator.RESTART);
        loadAnimator.setRepeatCount(ValueAnimator.INFINITE);

        loadAnimator.removeAllListeners();

        loadAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
//                Log.d(TAG,"onAnimationRepeat:"+progressReverse);
                progressReverse = !progressReverse;
            }
        });
        loadAnimator.start();
        mCurrentState = State.LOADDING;
    }

    public void loadSuccessed() {
        mCurrentState = State.COMPLETED_SUCCESSED;
        cancelAnimation();
        invaidateSelft();
    }

    public void loadFailed() {
        mCurrentState = State.COMPLETED_ERROR;
        cancelAnimation();
        invaidateSelft();
    }

    /**
     * 当view销毁(消失)时回自己调用。
     *
     * @author IVRING
     * @time 2017/8/16 17:26
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        cancelAnimation();

    }

    /**
     * 停止动画的方法
     *
     * @author IVRING
     * @time 2017/8/16 17:26
     */
    private void cancelAnimation() {
        if (shrinkAnim != null && shrinkAnim.isRunning()) {
            shrinkAnim.removeAllListeners();
            shrinkAnim.cancel();
            shrinkAnim = null;
        }
        if (loadAnimator != null && loadAnimator.isRunning()) {
            loadAnimator.removeAllListeners();
            loadAnimator.cancel();
            loadAnimator = null;
        }
    }

    public interface LoadListenner {

        void onClick(boolean isSuccessed);

        void needLoading();
    }


}
