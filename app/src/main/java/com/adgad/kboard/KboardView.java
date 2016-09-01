package com.adgad.kboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;


import java.util.List;

/**
 * Created by arjun on 12/06/16.
 */
public class KboardView extends KeyboardView {


    private SharedPreferences sharedPref;
    private GestureDetector mGestureDetector;
    private Canvas mCanvas;

    public KboardView(Context context, AttributeSet attrs) {

        super(context, attrs);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener());

    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(36);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        int bgColor = sharedPref.getInt("bgcolor", R.color.md_teal200);
        int pressedColor = sharedPref.getInt("pressedcolor", R.color.md_teal500);
        int textColor = sharedPref.getInt("textcolor", R.color.material_black);
        boolean isBold = sharedPref.getBoolean("textBold", true);


        paint.setTypeface(Typeface.create(Typeface.DEFAULT, isBold ? Typeface.BOLD : Typeface.NORMAL));

        paint.setColor(textColor);

        Paint bg = new Paint();
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            if(key.pressed == true) {
                bg.setColor(pressedColor);
            } else {
                bg.setColor(bgColor);
            }
            canvas.drawRect(key.x, key.y, key.x + key.width, key.y +  key.height, bg );
            if (key.icon != null) {
                key.icon.setBounds(key.x + (key.width/2) - 30, key.y + (key.height/2) - 40 , key.x + (key.width/2) + 30, key.y + (key.height/2) + 20);
                key.icon.setColorFilter(textColor, PorterDuff.Mode.MULTIPLY);
                key.icon.draw(canvas);
            } else if(key.label != null) {
                canvas.drawText(key.label.toString(), key.x + (key.width/2), key.y + (key.height/2), paint);
            }
        }
    }

}
