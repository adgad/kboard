package com.adgad.kboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
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


    private String ellipsize(String input, int maxLength) {
        String ellip = "...";
        if (input == null || input.length() <= maxLength
                || input.length() < ellip.length()) {
            return input;
        }
        return input.substring(0, maxLength - ellip.length()).concat(ellip);
    }

    public boolean isLuckyKey(Keyboard.Key key) {
        return key.codes[0] == -99;
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
        int borderColor = sharedPref.getInt("bgcolor", R.color.md_teal200);
        int pressedColor = sharedPref.getInt("pressedcolor", R.color.md_teal500);
        int textColor = sharedPref.getInt("textcolor", R.color.material_black);
        boolean spacing = sharedPref.getBoolean("spacing", false);
        int radius = 5;
        boolean isBold = sharedPref.getBoolean("textBold", true);

        //darken the border color
        float[] hsv = new float[3];
        Color.colorToHSV(borderColor, hsv);
        hsv[2] *= 0.85f; // value component
        borderColor = Color.HSVToColor(hsv);



        mPaint.setColor(textColor);

        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {

            mBackground.setColor(borderColor);



            if((key.pressed == true && !isLuckyKey(key)) || (isLuckyKey(key) && !key.pressed)) {
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
                String label = key.popupCharacters != null ? key.popupCharacters.toString() : key.label.toString();
                boolean isCommandKey = label.charAt(0) == '/' && label.indexOf("!") > 0;
                if(key.codes[0] == 10) {
                    mPaint.setTextSize(68); //enter icon is small so make it bigger
                } else {
                    mPaint.setTextSize(34);
                }
                if (isCommandKey) {
                    label = label.substring(1, label.indexOf("!"));
                    mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, isBold ? Typeface.ITALIC : Typeface.BOLD_ITALIC));
                    mPaint.setUnderlineText(true);
                } else {
                    mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, isBold ? Typeface.BOLD : Typeface.NORMAL));
                    mPaint.setUnderlineText(false);
                }
                canvas.drawText(ellipsize(label, 14), key.x + ((key.width - marginRight) / 2) + marginLeft, key.y + ((key.height - marginBottom) / 2) + marginTop, mPaint);
            }
        }
    }

}
