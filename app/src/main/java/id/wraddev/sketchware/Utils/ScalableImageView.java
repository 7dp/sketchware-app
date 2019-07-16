package id.wraddev.sketchware.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import id.wraddev.sketchware.R;

import static id.wraddev.sketchware.Activities.MainActivity.TAG;

public class ScalableImageView extends AppCompatImageView {

    public static final int DEFAULT_COLOR = Color.BLUE;
    // Always in these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final int CLICK = 3;
    private static final float TOUCH_TOLERANCE = 4;
    public static int sBrushSize = 10;
    protected float mOriginWidth;
    protected float mOriginHeight;
    private Matrix mMatrix;
    // Mode
    private int mMode = NONE;
    // For zooming usage
    private PointF mLast = new PointF();
    private PointF mStart = new PointF();
    private float mMinScale = 1f;
    private float mMaxScale = 4f;
    private float[] mM;
    public boolean mIsPentoolMode = false;
    private float mSaveScale = 1f;
    private float mX;
    private int mViewWidth;
    private int mViewHeight;
    private int mOldMeasureWidth;
    private int mOldMeasureHeight;
    private int mCurrentColor;
    private int mStrokeWidth;
    private float mY;
    private ScaleGestureDetector mScaleDetector;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private Path mPath;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Point mStartPoint;
    private Paint mPaint;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private List<Path> mPathList = new ArrayList<>();
    private List<Point> mStartPointList = new ArrayList<>();

    public ScalableImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mMatrix = new Matrix();
        mM = new float[9];
        setImageMatrix(mMatrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                PointF current = new PointF(event.getX(), event.getY());

                if (event.getPointerCount() == 2) {
                    Log.e(TAG, "onTouch: 2 FINGERS");

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.e(TAG, "#2 ACTION_DOWN");
                            mLast.set(current);
                            mStart.set(mLast);
                            mMode = DRAG;
                            break;

                        case MotionEvent.ACTION_MOVE:
                            if (mMode == DRAG) {
                                Log.e(TAG, "#2 ACTION_MOVE");
                                float deltaX = current.x - mLast.x;
                                float deltaY = current.y - mLast.y;
                                float fixTransX = getFixDragTrans(deltaX, mViewWidth, mOriginWidth * mSaveScale);
                                float fixTransY = getFixDragTrans(deltaY, mViewHeight, mOriginHeight * mSaveScale);
                                mMatrix.postTranslate(fixTransX, fixTransY);
                                fixTrans();
                                mLast.set(current.x, current.y);
                                touchMove(event.getX(), event.getY());
                                postInvalidate();
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                            Log.e(TAG, "#2 ACTION_UP");
                            mMode = NONE;
                            int xDiff = (int) Math.abs(current.x - mStart.x);
                            int yDiff = (int) Math.abs(current.y - mStart.y);

                            if (xDiff < CLICK && yDiff < CLICK) performClick();
                            break;

                        case MotionEvent.ACTION_POINTER_UP:
                            Log.e(TAG, "#2 ACTION_POINTER_UP");
                            mMode = NONE;
                            break;
                    }

                } else if (event.getPointerCount() == 1) {

                    if (mIsPentoolMode) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                            paint.setColor(Color.RED);
                            paint.setStrokeWidth(mPaint.getStrokeWidth());
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeJoin(Paint.Join.ROUND);
                            paint.setStrokeCap(Paint.Cap.ROUND);

                            drawPath((int) event.getX(), (int) event.getY(), mCanvas, paint);
                        }
                    }
                } else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.e(TAG, "onTouch action down X: " + event.getX());
                            Log.e(TAG, "onTouch action down Y: " + event.getY());
                            touchStart(event.getX(), event.getY());
                            postInvalidate();
                            break;

                        case MotionEvent.ACTION_MOVE:
                            touchMove(event.getX(), event.getY());
                            postInvalidate();
                            break;

                        case MotionEvent.ACTION_UP:
                            touchEnd();
                            postInvalidate();
                            break;

                        case MotionEvent.ACTION_POINTER_UP:
                            Log.e(TAG, "#1 ACTION_POINTER_UP");
                            break;
                    }
                }

                setImageMatrix(mMatrix);

                postInvalidate();
                return true;    // indicate event was handled
            }
        });
    }

    public void init(DisplayMetrics metrics) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        this.mCanvas = new Canvas(mBitmap);

        mCurrentColor = DEFAULT_COLOR;
        mStrokeWidth = sBrushSize;
    }

    public void init(DisplayMetrics metrics, Canvas canvas) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        this.mCanvas = canvas;

        mCurrentColor = DEFAULT_COLOR;
        mStrokeWidth = sBrushSize;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        for (FingerPath path : paths) {
            mPaint.setColor(path.mColor);
            mPaint.setStrokeWidth(path.mStrokeWidth);
            mPaint.setAntiAlias(true);
            mCanvas.drawPath(path.mPath, mPaint);
            Log.e(TAG, "onDraw: ");
        }
        mBitmapPaint.setAntiAlias(true);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(mCurrentColor, mStrokeWidth, mPath);
        paths.add(fp);

        mPath.reset();
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

    private void touchEnd() {
        mPath.lineTo(mX, mY);
    }

    private void drawPath(int x, int y, Canvas canvas, Paint paint) {
        // Draw a path on user touch
        Path path = new Path();

        if (mPathList.isEmpty()) {
            path.moveTo(x, y);
            mPathList.add(path);
            mStartPoint = new Point(x, y);
            mStartPointList.add(mStartPoint);
            addMarkerPoint(x, y, canvas, paint);

        } else {
            boolean mustClear = false;
            if (x <= mStartPoint.x + 25 && x >= mStartPoint.x - 25
                    && y <= mStartPoint.y + 25 && y >= mStartPoint.y - 25) {

                mPathList.get(mPathList.size() - 1).lineTo(mStartPoint.x, mStartPoint.y);
                mPathList.get(mPathList.size() - 1).close();
                mustClear = true;

            } else {
                mPathList.add(mPathList.get(mPathList.size() - 1));
                mPathList.get(mPathList.size() - 1).lineTo(x, y);
                addMarkerPoint(x, y, canvas, paint);
            }

            canvas.drawPath(mPathList.get(mPathList.size() - 1), paint);
            if (mustClear) mPathList.clear();
            postInvalidate();
        }

        Log.e(TAG, "path count: " + mPathList.size());
        Log.e(TAG, "x: " + x);
        Log.e(TAG, "y: " + y);
    }

    /**
     * Method for draw the marker
     * Draw 2 rectangle for fill and outline, outline rectangle overlap the fill rectangle
     *
     * @param x      For x coordinate
     * @param y      For y coordinate
     * @param canvas For marker drawing floor
     * @param paint  For coloring the marker
     */

    private void addMarkerPoint(int x, int y, Canvas canvas, Paint paint) {
        int size = 10;
        float size2 = (float) size / 4;
        Point point = new Point(x, y);

        // configure paint
        paint.setColor(getResources().getColor(R.color.red_outline));
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawRect(point.x - size, point.y - size, point.x + size, point.y + size, paint);
        postInvalidate();

        paint.setColor(getResources().getColor(R.color.red_fill));
        canvas.drawRect(point.x - size + size2,
                point.y - size + size2,
                point.x + size - size2,
                point.y + size - size2, paint);
        postInvalidate();

        // configure paint
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    public void setMaxZoom(float maxZoom) {
        mMaxScale = maxZoom;
    }

    private float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) return 0;
        return delta;
    }

    private void fixTrans() {
        mMatrix.getValues(mM);
        float transX = mM[Matrix.MTRANS_X];
        float transY = mM[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, mViewWidth, mOriginWidth * mSaveScale);
        float fixTransY = getFixTrans(transY, mViewHeight, mOriginHeight * mSaveScale);

        if (fixTransX != 0 || fixTransY != 0) mMatrix.postTranslate(fixTransX, fixTransY);
    }

    private float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans;
        float maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }
        if (trans < minTrans) return -trans + minTrans;
        if (trans > maxTrans) return -trans + maxTrans;
        return 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec);

        /*
         * Rescale image on rotation
         */

        if (mOldMeasureHeight == mViewWidth && mOldMeasureHeight == mViewHeight
                || mViewWidth == 0 || mViewHeight == 0) return;

        mOldMeasureHeight = mViewHeight;
        mOldMeasureWidth = mViewWidth;

        if (mSaveScale == 1) {
            // Fit to screen
            float scale;

            Drawable dwb = getDrawable();
            if (dwb == null || dwb.getIntrinsicWidth() == 0 || dwb.getIntrinsicHeight() == 0)
                return;

            int bmpWidth = dwb.getIntrinsicWidth();
            int bmpHeight = dwb.getIntrinsicHeight();

            Log.e(TAG, "bmWidth: " + bmpWidth + " bmHeight : " + bmpHeight);

            float scaleX = (float) mViewWidth / bmpWidth;
            float scaleY = (float) mViewHeight / bmpHeight;
            scale = Math.min(scaleX, scaleY);
            mMatrix.setScale(scale, scale);

            // Center the image

            float redundantYSpace = mViewHeight - (scale * bmpHeight);
            float redundantXSpace = mViewWidth - (scale * bmpWidth);

            redundantXSpace /= 2;
            redundantYSpace /= 2;

            mMatrix.postTranslate(redundantXSpace, redundantYSpace);

            mOriginWidth = mViewWidth - 2 * redundantXSpace;
            mOriginHeight = mViewHeight - 2 * redundantYSpace;
            setImageMatrix(mMatrix);
        }

        fixTrans();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mMode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float originScale = mSaveScale;
            mSaveScale *= scaleFactor;

            if (mSaveScale > mMaxScale) {
                mSaveScale = mMaxScale;
                scaleFactor = mMaxScale / originScale;
            } else if (mSaveScale < mMinScale) {
                mSaveScale = mMinScale;
                scaleFactor = mMinScale / originScale;
            }

            if (mOriginWidth * mSaveScale <= mViewWidth
                    || mOriginHeight * mSaveScale <= mViewHeight)
                mMatrix.postScale(scaleFactor, scaleFactor, (float) mViewWidth / 2, (float) mViewHeight / 2);
            else
                mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());

            fixTrans();
            return true;
        }
    }
}
