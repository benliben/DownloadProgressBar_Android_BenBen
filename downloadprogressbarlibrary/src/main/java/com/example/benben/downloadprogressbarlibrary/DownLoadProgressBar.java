package com.example.benben.downloadprogressbarlibrary;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by LiYuanXiong on 2016/9/29 21:28.
 * Email:3187683623@qq.com
 */
public class DownLoadProgressBar extends View implements Runnable {



/****************************************************************************************************************/



    private PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);

    private int DEFAULT_HEIGHT_DP = 35;

    private float MAX_PROGRESS = 100f;

    private Paint textPaint, bgPaint;

    private String progressText;

    private Rect textBouds;

    /**
     * 左右来回移动的滑块
     */
    private Bitmap downLoadBitmap;

    /**
     * 滑块移动最左边位置，作用是用来控制移动
     */
    private float downLoadLeft;

    /**
     * 进度条bitmap，包含滑块
     */
    private Bitmap pgBitmap;

    private Canvas pgCanvas;

    /**
     * 当前进度
     */
    private float progress;

    private boolean isFinish;

    private boolean isStop;

    /**
     * 下载中颜色
     */
    private int loadingColor;

    /**
     * 暂停时颜色
     */
    private int stopColor;

    /**
     * 进度文本，边框，进度条颜色
     */
    private int progressColor;

    private int textSize;

    private Thread thread;


    public DownLoadProgressBar(Context context) {
        this(context, null,0);
    }

    public DownLoadProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownLoadProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
    }

    /**自定义属性*/
    private void initAttrs(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.DownLoadProgressBar);
            textSize = (int) ta.getDimension(R.styleable.DownLoadProgressBar_textSize, dp2px(12));
            loadingColor = ta.getColor(R.styleable.DownLoadProgressBar_loadingColor, Color.parseColor("#40c4ff"));
            stopColor = ta.getColor(R.styleable.DownLoadProgressBar_stopColor, Color.parseColor("#ff9800"));
            ta.recycle();//回收
        }
    }


    /*重写onMeasure方法，当height设置为wrap_content时设置为默认高度*/
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int height = 0;
        switch (heightSpecMode) {
            case MeasureSpec.AT_MOST:
                height = (int) dp2px(DEFAULT_HEIGHT_DP);
                break;
            case MeasureSpec.EXACTLY:
            case MeasureSpec.UNSPECIFIED:
                height = heightSpecSize;
                break;
        }
        setMeasuredDimension(widthSpecSize, height);
        init();
    }


    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        textBouds = new Rect();

        progressColor = loadingColor;
        downLoadBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flicker);
        downLoadLeft = downLoadBitmap.getWidth();

        pgBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        pgCanvas = new Canvas(pgBitmap);

        thread = new Thread(this);
        thread.start();
    }


    /**重写onDraw方法*/
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /**边框*/
        drawBorder(canvas);
        /**进度*/
        drawProgress();
        canvas.drawBitmap(pgBitmap, 0, 0, null);
        /**进度text*/
        drawProgressText(canvas);
        /**变色处理*/
        drawColorProgressText(canvas);
    }

    /**
     * 边框
     */
    private void drawBorder(Canvas canvas) {
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setColor(progressColor);
        bgPaint.setStrokeWidth(dp2px(1));
        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);
    }

    /**
     * 进度
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void drawProgress() {
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setStrokeWidth(0);
        bgPaint.setColor(progressColor);

        float right = (progress / MAX_PROGRESS) * getMeasuredWidth();
        pgCanvas.save(Canvas.CLIP_SAVE_FLAG);
        pgCanvas.clipRect(0, 0, right, getMeasuredHeight());
        pgCanvas.drawColor(progressColor);
        pgCanvas.restore();

        if (!isStop) {
            bgPaint.setXfermode(xfermode);
            pgCanvas.drawBitmap(downLoadBitmap, downLoadLeft, 0, bgPaint);
            bgPaint.setXfermode(null);
        }
    }

    /**
     * 进度text
     */
    private void drawProgressText(Canvas canvas) {
        textPaint.setColor(progressColor);
        progressText = getProgressText();
        textPaint.getTextBounds(progressText, 0, progressText.length(), textBouds);
        int tWidth = textBouds.width();
        int tHeight = textBouds.height();
        float xCoordinate = (getMeasuredWidth() - tWidth) / 2;
        float yCoordinate = (getMeasuredHeight() + tHeight) / 2;
        canvas.drawText(progressText, xCoordinate, yCoordinate, textPaint);
    }

    /**
     * 变色处理
     */
    private void drawColorProgressText(Canvas canvas) {
        textPaint.setColor(Color.WHITE);
        int tWidth = textBouds.width();
        int tHeight = textBouds.height();
        float xCoordinate = (getMeasuredWidth() - tWidth) / 2;
        float yCoordinate = (getMeasuredHeight() + tHeight) / 2;
        float progressWidth = (progress / MAX_PROGRESS) * getMeasuredWidth();
        if (progressWidth > xCoordinate) {
            canvas.save(canvas.CLIP_SAVE_FLAG);
            float right = Math.min(progressWidth, xCoordinate + tWidth);
            canvas.clipRect(xCoordinate, 0, right, getMeasuredHeight());
            canvas.drawText(progressText, xCoordinate, yCoordinate, textPaint);
            canvas.restore();
        }
    }

    public void setProgress(float progress) {
        if (!isStop) {
            this.progress = progress;
            invalidate();
        }
    }

    public float getProgress() {
        return progress;
    }

    public void setStop(boolean stop) {
        isStop = stop;
        if (isStop) {
            progressColor = stopColor;
        } else {
            progressColor = loadingColor;
            thread = new Thread(this);
            thread.start();

        }
        invalidate();
    }

    public void finishLoad() {
        isFinish = true;
        setStop(true);
    }

    public boolean isStop() {
        return isStop;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void toggle() {
        if (!isFinish) {
            if (isStop) {
                setStop(false);
            } else {
                setStop(true);
            }
        }
    }

    @Override
    public void run() {
        int width = downLoadBitmap.getWidth();
        while (!isStop) {
            downLoadLeft += dp2px(5);
            float progressWidth = (progress / MAX_PROGRESS) * getMeasuredWidth();
            if (downLoadLeft >= progressWidth) {
                downLoadLeft = -width;
            }
            postInvalidate();

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String getProgressText() {
        String text = "";
        if (!isFinish) {
            if (!isStop) {
                text = "下载中" + progress + "%";
            } else {
                text = "继续";
            }
        } else {
            text = "下载完成";
        }
        return text;
    }

    private float dp2px(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return dp * density;

    }
}
