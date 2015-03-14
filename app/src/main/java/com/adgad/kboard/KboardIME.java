package com.adgad.kboard;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

/**
 * Created by arjun on 11/03/15.
 */
public class KboardIME  extends InputMethodService
    implements KeyboardView.OnKeyboardActionListener {

    private static final String TAG = "kboard";
    private InputMethodManager mInputMethodManager;

    private KeyboardView kv;
    private KBoard keyboard;
    private List<Keyboard.Key> mKeys;

    private boolean mIsShifted = false;
    private boolean enterIsDone = false;

    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override public void onCreate() {
        super.onCreate();
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    }
    @Override public void onInitializeInterface() {
        keyboard = new KBoard(this, R.xml.qwerty);
        mKeys = keyboard.getKeys();
        for(Keyboard.Key key:mKeys) {
            if(key.codes[0] == -104) {
                key.label = "\uD83D\uDC4D";
            }
        }
    }

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        kv.setKeyboard(keyboard);
        kv.setPreviewEnabled(false);
        kv.setOnKeyboardActionListener(this);
        return kv;
    }

    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        keyboard.setImeOptions(getResources(), attribute.imeOptions);
    }

    private String getKeyString(int code) {
        switch(code) {
            case -101:
                return mIsShifted ? "k." : "k";
            case -102:
                return mIsShifted ? "kl." : "cool";
            case -103:
                return mIsShifted ? "lol." : "lol";
            case -104:
                return mIsShifted ? "\uD83D\uDE12" : "\uD83D\uDC4D";
            case -105:
                return mIsShifted ? "ಥ_ಥ" : "ಠ_ಠ";
            case -106:
                return mIsShifted ? "Right." : "right...";
            case -6:
                return mIsShifted ? "\u2B06" : "\u21ea";

            default:
                return "";
        }
    }

    public void resetKeyChars() {
        String newString;
        for(Keyboard.Key key:mKeys) {
            newString = getKeyString(key.codes[0]);
            if(newString != "") {
                key.label = newString;
            }
        }
        kv.invalidateAllKeys();
    }

    private void playClick(){
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
           am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        playClick();


        switch(primaryCode) {
            case -5: //backspace
                if (ic != null) {
                    ic.deleteSurroundingText(1,0);
                }
                break;
            case -6: //MAD
                mIsShifted = !mIsShifted;
                resetKeyChars();
                break;
            case 10: //enter
               ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
               ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
               break;
            case -201: //subtype switcher
                switchIME();
                break;
            default:
                String word = "";
                if(ic.getTextBeforeCursor(1,0) != null && ic.getTextBeforeCursor(1,0).length() > 0) {
                    word = " ";
                }
                ic.commitText(word + getKeyString(primaryCode), 1);
                break;
            }
    }

    public void switchIME() {
        //final String LATIN = "com.android.inputmethod.latin/.LatinIME";
// 'this' is an InputMethodService
        if (Build.VERSION.SDK_INT >= 16) {
            try {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                final IBinder token = this.getWindow().getWindow().getAttributes().token;
                //imm.setInputMethod(token, LATIN);
                imm.switchToNextInputMethod(token, false);
            } catch (Throwable t) { // java.lang.NoSuchMethodError if API_level<11
                mInputMethodManager.showInputMethodPicker();
                Log.e(TAG, "cannot set the previous input method:");
                t.printStackTrace();
            }
        } else {
            mInputMethodManager.showInputMethodPicker();
        }

    }
    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeUp() {
    }

}