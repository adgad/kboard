package com.adgad.kboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.List;

/**
 * Created by arjun on 11/03/15.
 */
public class KboardIME  extends InputMethodService
    implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private KBoard keyboard;
    private List<Keyboard.Key> mKeys;

    private boolean mIsMad = false;
    private boolean enterIsDone = false;

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new KBoard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        mKeys = keyboard.getKeys();
        for(Keyboard.Key key:mKeys) {
            if(key.codes[0] == -104) {
                key.label = "\uD83D\uDC4D";
            }
        }
        return kv;
    }

    private String getKeyString(int code) {
        switch(code) {
            case -101:
                return mIsMad ? "k." : "k";
            case -102:
                return mIsMad ? "kl." : "kl";
            case -103:
                return mIsMad ? "lol." : "lol";
            case -104:
                return mIsMad ? "\uD83D\uDE12" : "\uD83D\uDC4D";
            case -6:
                return mIsMad ? "MAD" : "normal";
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
                    ic.beginBatchEdit();
                    ic.deleteSurroundingText(Integer.MAX_VALUE, Integer.MAX_VALUE);
                    ic.endBatchEdit();
                }
                break;
            case -6: //MAD
                mIsMad = !mIsMad;
                resetKeyChars();
                break;
            case -7: //enter
               ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                ic.commitText(getKeyString(primaryCode), 1);
                break;
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