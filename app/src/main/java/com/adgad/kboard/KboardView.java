package com.adgad.kboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;


import java.util.List;

/**
 * Created by arjun on 12/06/16.
 */
public class KboardView extends KeyboardView {


    private SharedPreferences sharedPref;
    private Canvas mCanvas;
    private Paint mPaint = new Paint();
    private Paint mBackground = new Paint();
    private Paint mKey = new Paint();


    public KboardView(Context context, AttributeSet attrs) {

        super(context, attrs);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

    }

    @Override
    public void onDraw(Canvas canvas) {
        mCanvas = canvas;
        int height = mCanvas.getHeight();
        int width = mCanvas.getWidth();
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(36);
        mPaint.setAntiAlias(true);
        mPaint.setSubpixelText(true);
        int bgColor = sharedPref.getInt("bgcolor", R.color.md_teal200);
        int pressedColor = sharedPref.getInt("pressedcolor", R.color.md_teal500);
        int textColor = sharedPref.getInt("textcolor", R.color.material_black);
        boolean spacing = sharedPref.getBoolean("spacing", false);
        int radius = 5;
        boolean isBold = sharedPref.getBoolean("textBold", true);


        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, isBold ? Typeface.BOLD : Typeface.NORMAL));

        mPaint.setColor(textColor);

        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            mBackground.setColor(bgColor);
            mBackground.setAlpha(200);
            if(key.pressed == true) {
                mKey.setColor(pressedColor);
            } else {
                mKey.setColor(bgColor);
            }

            int marginBottom = spacing ? (key.y + key.height == height) ? 20 : 6 : 0;
            int marginTop = spacing ? (key.y == 0) ? 20 :6 : 0;
            int marginLeft = spacing ? (key.x == 0) ? 20 : 5 : 0;
            int marginRight = spacing ?(key.x + key.width == width) ? 20 : 5 : 0;

            canvas.drawRect(key.x, key.y, key.x + key.width, key.y + key.height, mBackground);

            if (spacing && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(key.x + marginLeft, key.y + marginTop, key.x + key.width - marginRight, key.y + key.height - marginBottom, radius, radius, mKey);
            } else {
                canvas.drawRect(key.x + marginLeft, key.y + marginTop, key.x + key.width - marginRight, key.y + key.height - marginBottom, mKey);

            }
            if (key.icon != null) {
                key.icon.setBounds(key.x + (key.width/2) - 30, key.y + (key.height/2) - 40, key.x + (key.width/2) + 30, key.y + (key.height/2) + 20);
                key.icon.setColorFilter(textColor, PorterDuff.Mode.MULTIPLY);
                key.icon.draw(canvas);
            } else if(key.label != null) {
                canvas.drawText(key.label.toString(), key.x + ((key.width - marginRight)/2) + marginLeft, key.y + ((key.height - marginBottom)/2) + marginTop, mPaint);
            }
        }
    }

}
