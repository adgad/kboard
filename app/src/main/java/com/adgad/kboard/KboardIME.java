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
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;


import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


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
    private Vibrator vib;
    private List<Keyboard.Key> mKeys;

    private List<String> keys;
    private boolean mAutoSpace;
    private boolean mAutoSend;
    private boolean mVibrateOnClick;
    private boolean mSoundOnClick;
    private int mScreen = 0;
    private int totalScreens = 0;
    private final int KEYS_PER_SCREEN = 9;

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
        vib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        initPrefs();
    }

    private void initPrefs() {
        mAutoSpace = sharedPref.getBoolean("autospace", true);
        mAutoSend = sharedPref.getBoolean("autosend", false);
        mVibrateOnClick = sharedPref.getBoolean("vibrate_on", false);
        mSoundOnClick = sharedPref.getBoolean("sound_on", false);
        setKeys();

    }


    private void setKeys() {
        Gson gson = new Gson();



        String defaultJson = gson.toJson((Object) Keys.getDefault());
        String keysAsString = sharedPref.getString(Keys.STORAGE_KEY, defaultJson);
        keys = gson.fromJson(keysAsString, ArrayList.class);
        totalScreens = (int)Math.ceil((double)keys.size() / KEYS_PER_SCREEN);

    }
    @Override public void onInitializeInterface() {
        keyboard = new KBoard(this, R.xml.qwerty2);
        mKeys = keyboard.getKeys();
        resetKeyChars();
    }

    @Override
    public View onCreateInputView() {
        String c = sharedPref.getString("color_scheme", "material_dark");
        int keyboard_id;
        switch(c) {
            case "material_dark":
                keyboard_id = R.layout.material_dark;
                break;
            case "material_orange":
                keyboard_id = R.layout.material_orange;
                break;
            case "material_purple":
                keyboard_id = R.layout.material_purple;
                break;
            case "material_indigo":
                keyboard_id = R.layout.material_indigo;
                break;
            case "material_teal":
                keyboard_id = R.layout.material_teal;
                break;
            default:
                keyboard_id = R.layout.material_dark;
        }

        kv = (KeyboardView)getLayoutInflater().inflate(keyboard_id, null);
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
        if (code == -6) {
            return (mScreen + 1) + "/" + totalScreens;
        } else if (code < -100 && code >= (-100 - KEYS_PER_SCREEN)) {
            int indOfKey = -(code + 101 - (mScreen * KEYS_PER_SCREEN));
            if (indOfKey < keys.size()) {
                return keys.get(indOfKey);
            } else {
                return "NO_VALUE";
            }
        } else {
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
            if(newString == "NO_VALUE") {
                key.label = "";
            }
            else if(newString != "") {
                key.label = ellipsize(newString, 18);
            }
        }
        if(kv != null) {
            kv.invalidateAllKeys();
        }
    }

    private void playClick(){
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
           am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.3f);
    }

    private void vibrate(){
        vib.vibrate(40);
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();

        if(mSoundOnClick) {
            playClick();
        }

        if(mVibrateOnClick) {
            vibrate();
        }

        switch(primaryCode) {
            case -5: //backspace
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                break;
            case -6: //MAD
                switchScreens();
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
                String key = getKeyString(primaryCode);
                if(key == "NO_VALUE") {
                    key = "";
                }
                ic.commitText(word + key, 1);
                final EditorInfo ei = getCurrentInputEditorInfo();
                if(mAutoSend && (ei.imeOptions & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_SEND) {
                    ic.performEditorAction(EditorInfo.IME_ACTION_SEND);
                }
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
        switchScreens();
    }

    private void switchScreens() {
        mScreen = (mScreen < totalScreens - 1) ? mScreen + 1 : 0;
        resetKeyChars();
    }

    @Override
    public void swipeRight() {
        switchScreens();

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

    public static class Keys {
        public static ArrayList<String> getDefault() {
            ArrayList<String> defaultKeys = new ArrayList<>();
            defaultKeys.add("k");
            defaultKeys.add("Can't talk now. Speak later.");
            defaultKeys.add("lol!");
            defaultKeys.add("👍");
            defaultKeys.add("ಠ_ಠ");
            defaultKeys.add("haha");
            defaultKeys.add("¯\\_(ツ)_/¯");
            defaultKeys.add("See you later!");
            defaultKeys.add("\uD83D\uDE12");

            defaultKeys.add("ಥ_ಥ");
            defaultKeys.add("Thank you");
            defaultKeys.add("Sorry");
            defaultKeys.add("( ͡° \u035Cʖ ͡°)");
            defaultKeys.add("(╯°□°）╯︵ ┻━┻)");
            defaultKeys.add("Hey!");
            defaultKeys.add("Good thanks, yourself?");
            defaultKeys.add("Where are you?");
            defaultKeys.add("Cool.");


            return defaultKeys;
        }

        public static final String STORAGE_KEY = "userKeys-defaults";
    }
}
