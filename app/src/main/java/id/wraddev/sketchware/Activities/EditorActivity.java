package id.wraddev.sketchware.Activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.raed.drawingview.DrawingView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import id.wraddev.sketchware.R;

import static id.wraddev.sketchware.Activities.MainActivity.TAG;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

    DrawingView mDrawingView;
    ImageView mIbUndo;
    ImageView mIbRedo;
    ImageView mIbClear;
    ImageView mIbExport;
    ImageView mIbResetZoom;
    Button mBtAddCircle, mBtAddRect;
    Bitmap mImageBitmap;

//    com.raed.drawingview

    private void registerViews() {
        mDrawingView = findViewById(R.id.drawing_view);
        mIbUndo = findViewById(R.id.ib_undo);
        mIbRedo = findViewById(R.id.ib_redo);
        mIbClear = findViewById(R.id.ib_clear);
        mIbExport = findViewById(R.id.ib_export);
        mIbResetZoom = findViewById(R.id.ib_reset_zoom);
        mBtAddCircle = findViewById(R.id.bt_draw_circle);
        mBtAddRect = findViewById(R.id.bt_draw_rectangle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        registerViews();

        byte[] bitmapBytes = getIntent().getByteArrayExtra("image_array");

        if (bitmapBytes == null) Log.e(TAG, "### NULL");
        else {
            mImageBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            mDrawingView.setBackgroundImage(mImageBitmap);
        }

        setViewEnabled(mIbUndo, false);
        setViewEnabled(mIbRedo, false);
        mDrawingView.setUndoAndRedoEnable(true);

        mIbUndo.setOnClickListener(this);
        mIbRedo.setOnClickListener(this);
        mIbClear.setOnClickListener(this);
        mIbExport.setOnClickListener(this);
        mIbResetZoom.setOnClickListener(this);
        mBtAddRect.setOnClickListener(this);
        mBtAddCircle.setOnClickListener(this);

        setupDrawingView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_undo:
                undoChange();
                break;
            case R.id.ib_redo:
                redoChange();
                break;
            case R.id.ib_reset_zoom:
                resetZoom();
                break;
            case R.id.ib_clear:
                clearCanvas();
                break;
            case R.id.ib_export:
                exportToGallery();
                break;
            case R.id.bt_draw_circle:
                drawCircle();
                break;
            case R.id.bt_draw_rectangle:
                drawRectangle();
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDrawingView() {
        final DrawingView.OnDrawListener onDrawListener = new DrawingView.OnDrawListener() {
            @Override
            public void onDraw() {
                setViewEnabled(mIbUndo, true);
                setViewEnabled(mIbRedo, false);
            }
        };
        mDrawingView.setOnDrawListener(onDrawListener);

        final ScaleGestureDetector scaleDetector
                = new ScaleGestureDetector(EditorActivity.this, new MyPinchListener());

//        mDrawingView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                if (event.getPointerCount() == 1) {
//                    mDrawingView.exitZoomMode();
//                    Log.e(TAG, "onTouch");
//                }
//                scaleDetector.onTouchEvent(event);
//                mDrawingView.handleZoomAndTransEvent(event);
//
//                return true;
//            }
//        });
    }

    private void undoChange() {
        mDrawingView.undo();
        setViewEnabled(mIbUndo, !mDrawingView.isUndoStackEmpty());
        setViewEnabled(mIbRedo, !mDrawingView.isRedoStackEmpty());
    }

    private void redoChange() {
        mDrawingView.redo();
        setViewEnabled(mIbUndo, !mDrawingView.isUndoStackEmpty());
        setViewEnabled(mIbRedo, !mDrawingView.isRedoStackEmpty());
    }

    private void clearCanvas() {
        mDrawingView.clear();
        setViewEnabled(mIbUndo, true);
        setViewEnabled(mIbRedo, false);
    }

    private void resetZoom() {
        if (mDrawingView.isInZoomMode()) {
            mDrawingView.resetZoom();
            mDrawingView.exitZoomMode();
        } else mDrawingView.enterZoomMode();
    }

    private void exportToGallery() {
        String picturePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getAbsolutePath();
        String myPath = picturePath.concat("/").concat(getString(R.string.app_name));
        String fileName = "sketchware_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
        Log.e(TAG, "fileName: " + fileName);

        File directory = new File(myPath);
        if (!directory.exists()) directory.mkdir();

        File file = new File(directory, fileName);
        FileOutputStream fos = null;
        Bitmap photo = mDrawingView.exportDrawing();
        try {
            fos = new FileOutputStream(file.getAbsoluteFile());
            photo.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
                Toast.makeText(getApplicationContext(), "Image saved successfully", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setViewEnabled(ImageView imageView, boolean enabled) {
        if (!enabled) {
            imageView.setColorFilter(ContextCompat.getColor(
                    EditorActivity.this, android.R.color.darker_gray), PorterDuff.Mode.SRC_IN);
            imageView.setEnabled(false);
        } else {
            imageView.setColorFilter(ContextCompat.getColor(
                    EditorActivity.this, android.R.color.black), PorterDuff.Mode.SRC_IN);
            imageView.setEnabled(true);
        }
    }

    private void drawRectangle() {
//        new ShapeView(EditorActivity.this, Color.RED, 100, 100);
        Bitmap bitmap = Bitmap.createBitmap(mDrawingView.getWidth(), mDrawingView.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawRect(20, 20, 50, 50, paint);

        ImageView imageView = new ImageView(EditorActivity.this);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setImageBitmap(bitmap);
    }

    void drawCircle() {
        Bitmap bitmap = mImageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);

        int cx = canvas.getWidth() / 2;
        int cy = canvas.getHeight() / 2;
        int radius = Math.min(canvas.getWidth(), canvas.getHeight() / 2);

        canvas.drawCircle(cx, cy, radius, paint);
    }

    public static class MyPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.e(TAG, "onScale");
            return true;
        }
    }
}
