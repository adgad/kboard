package com.adgad.kboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
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
    implements KboardView.OnKeyboardActionListener,
    SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "kboard";
    private InputMethodManager mInputMethodManager;
    private SharedPreferences sharedPref;
    private KboardView kv;
    private KBoard keyboard;
    private Vibrator vib;
    private List<Keyboard.Key> mKeys;

    private List<String> keys;
    private boolean mPassiveAggressive;
    private boolean mAutoSpace;
    private boolean mAutoSend;
    private boolean mVibrateOnClick;
    private boolean mSoundOnClick;
    private int mScreen = 0;
    private int totalScreens = 0;
    private int mRows = 5;
    private int mKeysPerScreen = 12;
    private final int KEYS_PER_ROW = 4;



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
        mPassiveAggressive = sharedPref.getBoolean("passive_aggressive", false);
        mRows = Integer.parseInt(sharedPref.getString("rows", "5"));
        mKeysPerScreen = mRows * KEYS_PER_ROW;
        setKeys();

    }


    private void setKeys() {
        Gson gson = new Gson();

        String defaultJson = gson.toJson((Object) Keys.getDefault());
        String keysAsString = sharedPref.getString(Keys.STORAGE_KEY, defaultJson);
        keys = gson.fromJson(keysAsString, ArrayList.class);
        totalScreens = (int)Math.ceil((double)keys.size() / (mRows * KEYS_PER_ROW));

    }
    @Override public void onInitializeInterface() {
        setKeyboard();
    }

    private void setKeyboard() {
        if(mRows == 8) {
            keyboard = new KBoard(this, R.xml.eight_rows);
        } else if (mRows == 7) {
            keyboard = new KBoard(this, R.xml.seven_rows);
        } else if (mRows == 6) {
            keyboard = new KBoard(this, R.xml.six_rows);
        } else if (mRows == 5) {
            keyboard = new KBoard(this, R.xml.five_rows);
        } else if (mRows == 4) {
            keyboard = new KBoard(this, R.xml.four_rows);
        } else {
            keyboard = new KBoard(this, R.xml.normal);
        }
        mKeys = keyboard.getKeys();
        resetKeyChars();
    }

    @Override
    public View onCreateInputView() {
        setKeyboard();
        kv = (KboardView)getLayoutInflater().inflate(R.layout.material_dark, null);
        kv.setKeyboard(keyboard);
        kv.setBackgroundColor(getResources().getColor(R.color.white));
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
        } else if (code < -100 && code >= (-100 - mKeysPerScreen)) {
            int indOfKey = -(code + 101 - (mScreen * mKeysPerScreen));
            if (indOfKey < keys.size()) {
                return keys.get(indOfKey);
            } else {
                return "NO_VALUE";
            }
        } else {
            return "";
        }
    }


    public void resetKeyChars() {
        String newString;
        for(Keyboard.Key key:mKeys) {
            newString = getKeyString(key.codes[0]);
            if(newString == "NO_VALUE") {
                key.label = "";
                key.popupCharacters = "";
            }
            else if(newString != "") {
                key.label = newString;
                key.popupCharacters = newString;
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
    public void onRelease(int primaryCode) {
        InputConnection ic = getCurrentInputConnection();
        KCommands commands = new KCommands(
                this,
                ic,
                getCurrentInputEditorInfo(),
                keys,
                mAutoSpace,
                mPassiveAggressive);

        if(mSoundOnClick) {
            playClick();
        }

        if(mVibrateOnClick) {
            vibrate();
        }

        switch(primaryCode) {
            case -5: //backspace

                commands.d(1);
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
                String keyString = getKeyString(primaryCode);
                if((keyString.startsWith("/") && keyString.contains("!"))) {
                    parseCommand(commands, keyString);
                } else {
                    sendReply(commands, keyString);
                }
                break;
            }
    }

    public void parseCommand(KCommands kc, String cmd) {
        String[] cmdSplit = cmd.split("!", 2);
        String cmdAction = cmdSplit[1];
        kc.e(1, cmdAction);
    }

    public void sendReply(KCommands commands, String key) {
        if(key == "NO_VALUE") {
            return;
        }
        commands.i(1, key);
        if(mAutoSend) {
            commands.s(1);
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
    public void onKey(int primaryCode, int[] keyCodes) {
    }

    @Override
    public void onPress(int primaryCode) {
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
        setKeyboard();
        if(keyboard != null && mKeys != null && kv != null) {
            kv.setKeyboard(keyboard);
            resetKeyChars();
        }
    }

    public static class Keys {
        public static ArrayList<String> getDefault() {
            ArrayList<String> defaultKeys = new ArrayList<>();
            defaultKeys.add("k");
            defaultKeys.add("lol!");
            defaultKeys.add("Good thanks, yourself?");
            defaultKeys.add("thanks");
            defaultKeys.add("👍");
            defaultKeys.add("ಠ_ಠ");
            defaultKeys.add("haha");
            defaultKeys.add("¯\\_(ツ)_/¯");
            defaultKeys.add("/exec!dt(!),e($0)");
            defaultKeys.add("\uD83D\uDE12");

            defaultKeys.add("/🅰🅱🅲!ds,fancy(darksquare)");
            defaultKeys.add("/🄰🄱🄲!ds,fancy(square)");
            defaultKeys.add("/🅐🅑🅒!ds,fancy(darkcircle)");
            defaultKeys.add("/ⓐⓑⓒ!ds,fancy(circle)");
            defaultKeys.add("/𝚊𝚋𝚌!ds,fancy(monospace)");
            defaultKeys.add("/𝕒𝕓𝕔!ds,fancy(double)");
            defaultKeys.add("/𝔞𝔟𝔠!ds,fancy(fancy)");
            defaultKeys.add("/𝖆𝖇𝖈!ds,fancy(fancybold)");

            defaultKeys.add("ಥ_ಥ");
            defaultKeys.add("thank you");
            defaultKeys.add("sorry");
            defaultKeys.add("( ͡° \u035Cʖ ͡°)");
            defaultKeys.add("(╯°□°）╯︵ ┻━┻");
            defaultKeys.add("hey!");
            defaultKeys.add("cool");

            defaultKeys.add("yes");
            defaultKeys.add("no");
            defaultKeys.add("maybe");
            defaultKeys.add("don't mind");
            defaultKeys.add("sure, whatever");
            defaultKeys.add("xxx");
            defaultKeys.add("Can't talk now. Speak later.");
            defaultKeys.add("I'll be late");
            defaultKeys.add("okay");

            defaultKeys.add("/Italicise Previous!dw,i(_$0_)");
            defaultKeys.add("/Italicise Next!i(__),j");
            defaultKeys.add("/Bolden Previous!dw,i(*$0*)");
            defaultKeys.add("/Bolden Next!i(**),j");
            defaultKeys.add("/Copy All!yy");
            defaultKeys.add("/Paste!p");
            defaultKeys.add("/-1w!b");
            defaultKeys.add("/+1w!w");
            defaultKeys.add("/Delete Word!dw");
            return defaultKeys;
        }

        public static final String STORAGE_KEY = "userKeys-defaults";
    }
}
