package ml.melun.mangaview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

public class NeumorphicBackground extends Drawable {

    public static void setNeumorphicBackground(View view){
        StateListDrawable res = new StateListDrawable();
        res.addState(new int[]{android.R.attr.state_pressed}, new NeumorphicBackground(true));
        res.addState(new int[]{}, new NeumorphicBackground(false));
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        ViewCompat.setBackground(view, res);
    }


    Paint lightShadowPaint, darkShadowPaint, backgroundPaint;
    RectF rect;
    boolean flat = false;
    int shapeRadius = 20;
    int shadowRadius = 8;
    int offsetX = 8;
    int offsetY = 8;
    int lightShadowColor = Color.parseColor("#80FFFFFF");
    int darkShadowColor = Color.parseColor("#80D1CDC7");
    int backgroundColor = Color.parseColor("#EFEEEE");
    int borderColor = Color.parseColor("#33FFFFFF");

    private NeumorphicBackground(boolean flat){
        this.flat = flat;
        if(!flat) {
            lightShadowPaint = new Paint();
            lightShadowPaint.setColor(Color.TRANSPARENT);
            lightShadowPaint.setAntiAlias(true);
            lightShadowPaint.setShadowLayer(shadowRadius, -offsetX, -offsetY, lightShadowColor);
            lightShadowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));

            darkShadowPaint = new Paint();
            darkShadowPaint.setColor(Color.TRANSPARENT);
            darkShadowPaint.setAntiAlias(true);
            darkShadowPaint.setShadowLayer(shadowRadius, offsetX, offsetY, darkShadowColor);
            darkShadowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        }

        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setAntiAlias(true);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if(!flat) {
            canvas.drawRoundRect(rect, shapeRadius, shapeRadius, lightShadowPaint);
            canvas.drawRoundRect(rect, shapeRadius, shapeRadius, darkShadowPaint);
        }
        canvas.drawRoundRect(rect, shapeRadius, shapeRadius, backgroundPaint);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        rect = new RectF(left + shadowRadius + offsetX, top + shadowRadius + offsetY ,
                right - shadowRadius - offsetX, bottom - shadowRadius - offsetY);
    }

    @Override
    public void setAlpha(int i) {
        if(!flat) {
            lightShadowPaint.setAlpha(i);
            darkShadowPaint.setAlpha(i);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        lightShadowPaint.setColorFilter(colorFilter);
        darkShadowPaint.setColorFilter(colorFilter);
    }
}
