package com.adgad.kboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by arjun on 11/03/15.
 */
public class KboardIME  extends InputMethodService
    implements KeyboardView.OnKeyboardActionListener,
    SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "kboard";
    private InputMethodManager mInputMethodManager;
    private SharedPreferences sharedPref;
    private KeyboardView kv;
    private KBoard keyboard;
    private List<Keyboard.Key> mKeys;

    private boolean mIsShifted = false;
    private Map<Integer, String> normalKeys;
    private Map<Integer, String> shiftedKeys;
    private boolean mAutoSpace;
    private boolean mAutoSend;

    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        initPrefs();
    }

    private void initPrefs() {
        mAutoSpace = sharedPref.getBoolean("autospace", true);
        mAutoSend = sharedPref.getBoolean("autosend", false);
        normalKeys = new HashMap<Integer, String>();
        shiftedKeys = new HashMap<Integer, String>();

        normalKeys.put(-101, sharedPref.getString("normal1", "k"));
        normalKeys.put(-102, sharedPref.getString("normal2", "cool"));
        normalKeys.put(-103, sharedPref.getString("normal3", "lol"));
        normalKeys.put(-104, sharedPref.getString("normal4", "👍"));
        normalKeys.put(-105, sharedPref.getString("normal5", "ಠ_ಠ"));
        normalKeys.put(-106, sharedPref.getString("normal6", "right..."));



        shiftedKeys.put(-101, sharedPref.getString("shifted1", "k."));
        shiftedKeys.put(-102, sharedPref.getString("shifted2", "kl."));
        shiftedKeys.put(-103, sharedPref.getString("shifted3", "lol."));
        shiftedKeys.put(-104, sharedPref.getString("shifted4", "😒"));
        shiftedKeys.put(-105, sharedPref.getString("shifted5", "ಥ_ಥ"));
        shiftedKeys.put(-106, sharedPref.getString("shifted6", "Right."));

    }
    @Override public void onInitializeInterface() {
        keyboard = new KBoard(this, R.xml.qwerty);
        mKeys = keyboard.getKeys();
        resetKeyChars();
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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        switch(code) {
            case -6:
                return mIsShifted ? "\u2B06" : "\u21ea";
            case -101:
            case -102:
            case -103:
            case -104:
            case -105:
            case -106:
                return mIsShifted ? shiftedKeys.get(code) : normalKeys.get(code);
            default:
                return "";
        }
    }

    String ellipsize(String input, int maxLength) {
        String ellip = "...";
        if (input == null || input.length() <= maxLength
                || input.length() < ellip.length()) {
            return input;
        }
        return input.substring(0, maxLength - ellip.length()).concat(ellip);
    }

    public void resetKeyChars() {
        String newString;
        for(Keyboard.Key key:mKeys) {
            newString = getKeyString(key.codes[0]);
            if(newString != "") {
                key.label = ellipsize(newString, 10);
            }
        }
        if(kv != null) {
            kv.invalidateAllKeys();
        }
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
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
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
            case -301: //settings
                Intent i = new Intent(this, PrefsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;
            default:

                String word = "";
                if(mAutoSpace && ic.getTextBeforeCursor(1,0) != null && ic.getTextBeforeCursor(1,0).length() > 0) {
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        initPrefs();
        if(keyboard != null && mKeys != null && kv != null) {
            resetKeyChars();
        }
    }
}