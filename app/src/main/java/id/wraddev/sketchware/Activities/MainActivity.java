package id.wraddev.sketchware.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import id.wraddev.sketchware.R;
import id.wraddev.sketchware.Utils.ScalableImageView;

import static android.content.Intent.ACTION_PICK;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "sketchware";
    private static final int REQUEST_CODE = 200;

    ImageView mIvSave;
    ScalableImageView mScalableImageView;
    FloatingActionButton mFabPentool;
    FloatingActionButton mFabCircle;
    FloatingActionButton mFabRectangle;
    FloatingActionButton mFabPinch;
    FloatingActionMenu mActionMenu;
    DisplayMetrics metrics = new DisplayMetrics();

    private List<Path> mPathList = new ArrayList<>();

//    public static final int DEFAULT_COLOR = Color.RED;
//    public static int sBrushSize = 20;
//    private int mCurrentColor;
//    private int mStrokeWidth;
//    private static final float TOUCH_TOLERANCE = 4;
//    private float mX, mY;
//    private ArrayList<FingerPath> paths = new ArrayList<>();
//    private Bitmap mBitmap;
//    private Canvas mCanvas;
//    private Paint mPaint;
//    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
//    private Path mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        mActionMenu.setClosedOnTouchOutside(true);

        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScalableImageView.init(metrics);


    }

    private void initViews() {
        mIvSave = findViewById(R.id.iv_save);
        mScalableImageView = findViewById(R.id.scalable_image_view);
        mFabCircle = findViewById(R.id.circle_item);
        mFabPentool = findViewById(R.id.pentool_item);
        mFabPinch = findViewById(R.id.pinch_item);
        mFabRectangle = findViewById(R.id.rectangle_item);
        mActionMenu = findViewById(R.id.fab_menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: " + data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE && data != null) {
            try {
                setupDrawingCanvas(getImageBitmap(data.getData()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupDrawingCanvas(Bitmap mutableBitmap) {
        mScalableImageView.setImageBitmap(mutableBitmap);

        Canvas canvas = new Canvas(mutableBitmap);
        mScalableImageView.init(metrics, canvas);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GREEN);

        drawRectangle(canvas, paint);
        drawCircle(canvas);
        enterPentoolMode();
        enterNormalMode();
        saveBitmapToGallery(mutableBitmap);
    }

    private Bitmap getImageBitmap(Uri bitmapData) throws FileNotFoundException {
        InputStream stream = getContentResolver().openInputStream(bitmapData);
        Bitmap selectedBitmap = BitmapFactory.decodeStream(stream).copy(Bitmap.Config.RGB_565, true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 10, bos);

        Log.e(TAG, "byteCount: " + selectedBitmap.getByteCount());
        return selectedBitmap;
    }

    private void drawRectangle(final Canvas canvas, final Paint paint) {
        mFabRectangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionMenu.close(true);
                Point center = new Point(canvas.getWidth() / 2, canvas.getHeight() / 2);
                int size = 100;
                int left = center.x - size;
                int top = center.y - size;
                int right = center.x + size;
                int bottom = center.y + size;
                Rect rectangle = new Rect(left, top, right, bottom);

                canvas.drawRect(rectangle, paint);

                mScalableImageView.postInvalidate();
            }
        });
    }

    private void drawCircle(final Canvas canvas) {
        mFabCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionMenu.close(true);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(0f);
                canvas.drawCircle((float) canvas.getWidth() / 2, (float) canvas.getHeight() / 2, 100, paint);
                mScalableImageView.postInvalidate();
            }
        });
    }

    void enterPentoolMode() {
        mFabPentool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionMenu.close(true);
                mScalableImageView.mIsPentoolMode = true;
            }
        });
    }

    void enterNormalMode() {
        mFabPinch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionMenu.close(true);
                mScalableImageView.mIsPentoolMode = false;
            }
        });
    }

    private void saveBitmapToGallery(final Bitmap image) {
        mIvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMessage("Sketchware is saving image...");
                progressDialog.setIndeterminate(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                progressDialog.show();

                String picturePath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath();
                String myPath = picturePath.concat("/").concat(getString(R.string.app_name));
                String fileName = "sketchware_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                Log.e(TAG, "fileName: " + fileName);

                File directory = new File(myPath);
                if (!directory.exists()) directory.mkdir();

                File file = new File(directory, fileName);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file.getAbsoluteFile());
                    image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                        MediaScannerConnection.scanFile(
                                MainActivity.this,
                                new String[]{file.toString()},
                                null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {
                                        Log.e(TAG, "onScanCompleted SCANNED PATH: " + path);
                                        Log.e(TAG, "onScanCompleted SCANNED URI: " + uri);
                                    }
                                });
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Image saved successfully", Toast.LENGTH_LONG).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_open, menu);
        menu.findItem(R.id.menu_item_open).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startPickImage();
                return true;
            }
        });
        return true;
    }

    private void startPickImage() {
        Intent pickIntent = new Intent(ACTION_PICK);
        pickIntent.setType("image/*");
        startActivityForResult(pickIntent, REQUEST_CODE);
    }
}
