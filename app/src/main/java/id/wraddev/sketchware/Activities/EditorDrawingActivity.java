package id.wraddev.sketchware.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import id.wraddev.sketchware.R;
import id.wraddev.sketchware.Utils.DrawingView;

public class EditorDrawingActivity extends AppCompatActivity {
    DrawingView mDrawingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_drawing_layout);

        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.colorAccent));
        paint.setStrokeWidth(10f);

        mDrawingView = findViewById(R.id.my_drawing_view);

        byte[] bitmapBytes = getIntent().getByteArrayExtra("image_array");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

        Bitmap alteredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(alteredBitmap);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        mDrawingView.setImageBitmap(alteredBitmap);

        final Button button = findViewById(R.id.button_paint);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setSelected(!button.isSelected());
                mDrawingView.setZoomEnabled(!button.isSelected());
            }
        });
    }
}
