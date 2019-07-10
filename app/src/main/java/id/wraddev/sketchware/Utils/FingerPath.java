package id.wraddev.sketchware.Utils;

import android.graphics.Path;

public class FingerPath {

    public int mColor;
    public int mStrokeWidth;
    public Path mPath;

    public FingerPath(int color, int strokeWidth, Path path) {
        this.mColor = color;
        this.mStrokeWidth = strokeWidth;
        this.mPath = path;
    }
}
