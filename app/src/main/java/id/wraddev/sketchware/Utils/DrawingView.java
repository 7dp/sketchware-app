package id.wraddev.sketchware.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class DrawingView extends AppCompatImageView {
    static final float TOUCH_TOLERANCE = 4;
    static final int INVALID_POINTER_ID = -1;
    float mX;
    float mY;
    float mPosX;
    float mPosY;
    float mLastTouchX;
    float mLastTouchY;
    float mLastGestureX;
    float mLastGestureY;
    float mScaleFactor = 1f;
    int mActivePointerId = INVALID_POINTER_ID;
    boolean mZoomEnabled = true;

    Path mPath;
    Paint mDrawPaint;
    Paint mCanvasPaint;
    Canvas mDrawCanvas;
    Bitmap mCanvasBitmap;
    ScaleGestureDetector mScaleDetector;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        setupDrawing();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        setupDrawing();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mDrawCanvas = new Canvas(mCanvasBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        mScaleDetector.onTouchEvent(event);
        if (mZoomEnabled) {
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (!mScaleDetector.isInProgress()) {
                        float x = event.getX();
                        float y = event.getY();
                        mLastTouchX = x;
                        mLastTouchY = y;
                        mActivePointerId = event.getPointerId(0);
                    }
                    break;

                case MotionEvent.ACTION_POINTER_1_DOWN:
                    if (mScaleDetector.isInProgress()) {
                        mLastGestureX = mScaleDetector.getFocusX();
                        mLastGestureY = mScaleDetector.getFocusY();
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!mScaleDetector.isInProgress()) {
                        int pointerIndex = event.findPointerIndex(mActivePointerId);
                        float x = event.getX(pointerIndex);
                        float y = event.getY(pointerIndex);

                        float dx = x - mLastTouchX;
                        float dy = y - mLastTouchY;

                        mPosX += dx;
                        mPosY += dy;

                        invalidate();

                        mLastTouchX = x;
                        mLastTouchY = y;

                    } else {
                        float gx = mScaleDetector.getFocusX();
                        float gy = mScaleDetector.getFocusY();

                        float gdx = gx - mLastGestureX;
                        float gdy = gy - mLastGestureY;

                        mPosX += gdx;
                        mPosY += gdy;

                        invalidate();

                        mLastGestureX = gx;
                        mLastGestureY = gy;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    mActivePointerId = INVALID_POINTER_ID;
                    break;

                case MotionEvent.ACTION_CANCEL:
                    mActivePointerId = INVALID_POINTER_ID;
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                            >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    int pointerId = event.getPointerId(pointerIndex);

                    if (pointerId == mActivePointerId) {
                        int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mLastTouchX = event.getX(newPointerIndex);
                        mLastTouchY = event.getY(newPointerIndex);
                        mActivePointerId = event.getPointerId(newPointerIndex);
                    } else {
                        int tempPointerIndex = event.findPointerIndex(mActivePointerId);
                        mLastTouchX = event.getX(tempPointerIndex);
                        mLastTouchY = event.getY(tempPointerIndex);
                    }
                    break;
            }
            return true;

        } else {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    touchStart(event.getX(), event.getY());
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchMove(event.getX(), event.getY());
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touchUp();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mPosX, mPosY);

        if (mScaleDetector.isInProgress()) {
            canvas.scale(mScaleFactor, mScaleFactor, mScaleDetector.getFocusX(), mScaleDetector.getFocusY());
        } else {
            canvas.scale(mScaleFactor, mScaleFactor, mLastGestureX, mLastGestureY);
        }
        super.onDraw(canvas);

        mDrawCanvas.drawPath(mPath, mDrawPaint);
        canvas.drawBitmap(mCanvasBitmap, 0, 0, mCanvasPaint);
        canvas.restore();
    }

    private void setupDrawing() {
        mPath = new Path();
        mDrawPaint = new Paint();
        mDrawPaint.setColor(Color.RED);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStrokeWidth(20);
        mDrawPaint.setStyle(Paint.Style.FILL);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mCanvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void setZoomEnabled(boolean b) {
        this.mZoomEnabled = b;
    }

    private void touchStart(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
        mDrawCanvas.drawPath(mPath, mDrawPaint);
        mPath.reset();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10f));
            invalidate();
            return true;
        }
    }
}
