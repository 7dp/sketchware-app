package id.wraddev.sketchware;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SandeepActivity extends AppCompatActivity implements View.OnClickListener {

    private Button enableZoomBtn;
    private DrawableView drawbleView;
    private CustomImageView touchImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sandeep_layout);
        drawbleView = findViewById(R.id.drawble_view);
        enableZoomBtn = findViewById(R.id.enable_zoom);
        touchImageView = findViewById(R.id.zoom_iv);
        enableZoomBtn.setOnClickListener(this);
        drawbleView.setDrawingEnabled(false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.enable_zoom:
                if (enableZoomBtn.getText().equals("disable zoom")) {
                    touchImageView.setZoomEnable(false);
                    drawbleView.setDrawingEnabled(true);
                    enableZoomBtn.setText("enable zoom");
                } else {
                    touchImageView.setZoomEnable(true);
                    drawbleView.setDrawingEnabled(false);
                    enableZoomBtn.setText("disable zoom");
                }
                break;

            default:
                break;
        }
    }
}
