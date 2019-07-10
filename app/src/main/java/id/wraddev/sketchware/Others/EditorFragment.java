package id.wraddev.sketchware.Others;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import id.wraddev.sketchware.R;
import id.wraddev.sketchware.Utils.DrawView;
import id.wraddev.sketchware.Utils.ScalableImageView;
import id.wraddev.sketchware.Utils.ShapeView;

import static android.view.MotionEvent.ACTION_DOWN;
import static id.wraddev.sketchware.Activities.MainActivity.TAG;

public class EditorFragment extends AppCompatActivity implements View.OnTouchListener, View.OnDragListener {

    //    DrawingView mDrawingView;
    ImageView mIbUndo;
    ImageView mIbRedo;
    ImageView mIbClear;
    ImageView mIbExport;
    ImageView mIbResetZoom;
    ScalableImageView scalableImageView;
    DrawView mDrawView;
    ShapeView shapeView;

    boolean mHasContent = false;

    private void registerViews() {
//        mDrawingView = findViewById(R.id.drawing_view);
        mIbUndo = findViewById(R.id.ib_undo);
        mIbRedo = findViewById(R.id.ib_redo);
        mIbClear = findViewById(R.id.ib_clear);
        mIbExport = findViewById(R.id.ib_export);
        mIbResetZoom = findViewById(R.id.ib_reset_zoom);
//        mEditorView = findViewById(R.id.photo_editor_view);
        scalableImageView = findViewById(R.id.my_scalable_iv);
        mDrawView = findViewById(R.id.draw_view);
        shapeView = findViewById(R.id.shape_view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_layout);

        registerViews();

        byte[] bitmapBytes = getIntent().getByteArrayExtra("image_array");
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
        Bitmap mutableBitmap = Bitmap.createBitmap(imageBitmap).copy(Bitmap.Config.ARGB_8888, true);
        scalableImageView.setImageBitmap(mutableBitmap);

        final Canvas canvas = new Canvas(mutableBitmap);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GREEN);

        findViewById(R.id.iv_export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHasContent) {
                    Log.e(TAG, "A");
                    drawCircle(canvas);
                } else {
                    Log.e(TAG, "B");
                    drawRectangle(canvas, paint);
                }
            }
        });

        findViewById(R.id.box1).setOnTouchListener(this);
        findViewById(R.id.box2).setOnTouchListener(this);
        findViewById(R.id.box3).setOnTouchListener(this);

        findViewById(R.id.left_container).setOnDragListener(this);
        findViewById(R.id.right_container).setOnDragListener(this);
    }

    private void drawRectangle(Canvas canvas, Paint paint) {
        mHasContent = true;
        Point center = new Point(canvas.getWidth() / 2, canvas.getHeight() / 2);
        int size = 100;
        int left = center.x - size;
        int top = center.y - size;
        int right = center.x + size;
        int bottom = center.y + size;
        Rect rectangle = new Rect(left, top, right, bottom);

        canvas.drawRect(rectangle, paint);
//        Bitmap rectangleBitmap = Bitmap.createBitmap(rectangle.width(), rectangle.height(), Bitmap.Config.ARGB_8888);
//        Bitmap dinoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dinosaur);
//        canvas.drawBitmap(rectangleBitmap, (float) canvas.getWidth() / 2, (float) canvas.getHeight() / 2, paint);
    }

    private void drawCircle(Canvas canvas) {
        mHasContent = false;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle((float) canvas.getWidth() / 2, (float) canvas.getHeight() / 2, 100, paint);
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            // signal for the start of a drag and drop operation
            case DragEvent.ACTION_DRAG_STARTED:
                // do nothing
                break;

            // the drag point has entered the bounding box of the View
            case DragEvent.ACTION_DRAG_ENTERED:
                v.setBackgroundColor(Color.RED);
                break;

            // the user has moved the drag shadow outside the bounding box of the View
            case DragEvent.ACTION_DRAG_EXITED:
                v.setBackgroundColor(v.getId() == R.id.left_container ? 0xFFE8E6E7 : 0xFFB1BEC4);
                break;

            // the drag and drop operation has concluded
            case DragEvent.ACTION_DRAG_ENDED:
                v.setBackgroundColor(v.getId() == R.id.left_container ? 0xFFE8E6E7 : 0xFFB1BEC4);
                break;

            //drag shadow has been released,the drag point is within the bounding box of the View
            case DragEvent.ACTION_DROP:
                View view = (View) event.getLocalState();
                // we want to make sure it is dropped only to left and right parent view
                if (v.getId() == R.id.left_container || v.getId() == R.id.right_container) {

                    ViewGroup source = (ViewGroup) view.getParent();
                    source.removeView(view);

                    RelativeLayout target = (RelativeLayout) v;
                    target.addView(view);

                    String id = event.getClipData().getItemAt(0).getText().toString();
                }
                // make view visible as we set visibility to invisible while starting drag
                view.setVisibility(View.VISIBLE);
                break;
        }

        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == ACTION_DOWN) {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            ClipData data = ClipData.newPlainText("id", view.getResources().getResourceEntryName(view.getId()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                view.startDragAndDrop(data, shadowBuilder, view, 0);
            else view.startDrag(data, shadowBuilder, view, 0);

//            view.setVisibility(View.INVISIBLE);

            return true;
        }
        return false;
    }


//    void undoChange() {
//        mIbUndo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDrawingView.undo();
//                mIbUndo.setEnabled(!mDrawingView.isUndoStackEmpty());
//                mIbRedo.setEnabled(!mDrawingView.isRedoStackEmpty());
//            }
//        });
//    }
//
//    void redoChange() {
//        mIbRedo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDrawingView.redo();
//                mIbUndo.setEnabled(!mDrawingView.isUndoStackEmpty());
//                mIbRedo.setEnabled(!mDrawingView.isRedoStackEmpty());
//            }
//        });
//    }
//
//    void clearCanvas() {
//        mIbClear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDrawingView.clear();
//                mIbUndo.setEnabled(true);
//                mIbRedo.setEnabled(false);
//            }
//        });
//    }
//
//    void resetZoom() {
//        mIbResetZoom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDrawingView.resetZoom();
//            }
//        });
//    }
}
