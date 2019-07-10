package id.wraddev.sketchware.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import id.wraddev.sketchware.R;

public class ShapeView extends View {

    static final int TOUCH_MODE_TAP = 1;
    static final int TOUCH_MODE_DOWN = 2;
    final int mImageWidth = 100;
    final int mImageHeight = 100;
    Paint mPaint;
    Rect mRect;
    Bitmap bitmap;
    int mScreenHeight;
    int mScreenWidth;
    int prevX;
    int prevY;
    Rect mImagePosition;
    Region mImageRegion;
    boolean canImageMove;
    private int mTouchSlop;
    private int mTouchMode;

    public ShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.dinosaur);
        mPaint = new Paint();
        mPaint.setTextSize(25);
        mPaint.setColor(Color.RED);
        //Size for image
        mImagePosition = new Rect(10, 10, mImageWidth, mImageHeight);
        mImageRegion = new Region();
        mImageRegion.set(mImagePosition);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mScreenHeight = display.getHeight();
        mScreenWidth = display.getWidth();
        canImageMove = false;
    }


    public boolean onTouchEvent(MotionEvent event) {
        int positionX = (int) event.getRawX();
        int positionY = (int) event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mTouchMode = TOUCH_MODE_DOWN;

                if (mImageRegion.contains(positionX, positionY)) {
                    prevX = positionX;
                    prevY = positionY;
                    canImageMove = true;
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                if (canImageMove) {
                    // Check if we have moved far enough that it looks more like a
                    // scroll than a tap
                    final int distY = Math.abs(positionY - prevY);
                    final int distX = Math.abs(positionX - prevX);

                    if (distX > mTouchSlop || distY > mTouchSlop) {
                        int deltaX = positionX - prevX;
                        int deltaY = positionY - prevY;
                        // Check if delta is added, is the rectangle is within the visible screen
                        if ((mImagePosition.left + deltaX) > 0
                                && ((mImagePosition.right + deltaX) < mScreenWidth)
                                && (mImagePosition.top + deltaY) > 0
                                && ((mImagePosition.bottom + deltaY) > 0)) {
                            // invalidate current position as we are moving...
                            mImagePosition.left = mImagePosition.left + deltaX;
                            mImagePosition.top = mImagePosition.top + deltaY;
                            mImagePosition.right = mImagePosition.left + mImageWidth;
                            mImagePosition.bottom = mImagePosition.top + mImageHeight;
                            mImageRegion.set(mImagePosition);
                            prevX = positionX;
                            prevY = positionY;

                            invalidate();
                        }
                    }
                }
            }
            break;
            case MotionEvent.ACTION_UP:
                canImageMove = false;
                break;
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        // make the entire canvas white
        paint.setColor(Color.RED);
        Rect rect = new Rect(0, 0, this.getWidth(), this.getHeight());
        canvas.drawRect(mImagePosition, paint);
        //canvas.drawBitmap(bitmap, null,mImagePosition, null);
    }


}
