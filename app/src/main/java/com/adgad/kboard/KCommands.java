package com.adgad.kboard;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by arjun on 27/12/16.
 */


public class KCommands {

    private static final int MAX_LOOKBACK = 500;

    private InputConnection inputConnection;
    private EditorInfo inputEditor;
    private boolean mAutoSpace;
    private boolean mPassiveAggressive;
    private List<String> mKeys;
    private Collection<Emoji> mEmoji;
    private List<String> mTextKeys = new ArrayList<>();
    private Map<String,String> mCommandKeys = new HashMap<>();
    private String buffer = null;

    KCommands(
            InputConnection ic,
            EditorInfo ei,
            List<String> keys,
            boolean autoSpace,
            boolean passiveAggressive) {
        inputConnection = ic;
        inputEditor = ei;
        mAutoSpace = autoSpace;
        mPassiveAggressive = passiveAggressive;
        mKeys = keys;
        for (String key: mKeys) {
            if (!(key.startsWith("/") && key.contains("!"))) {
                mTextKeys.add(key);
            } else {
                mCommandKeys.put(key.split("!", 2)[0].substring(1), key.split("!", 2)[1]);
            }
        }
        mEmoji = EmojiManager.getAll();
    }

    private void commitText(String key) {
        String word = "";
        if(mAutoSpace && inputConnection != null && inputConnection.getTextBeforeCursor(1,0) != null && inputConnection.getTextBeforeCursor(1,0).length() > 0) {
            word = " ";
        }

        if(mPassiveAggressive && key.length() > 0) {
            String lastLetter = key.substring(key.length() - 1);
            key = key.substring(0,1).toUpperCase() + key.substring(1);
            key = key.replace('!', '.');
            if(!lastLetter.equals(lastLetter.toUpperCase())) {
                key = key + ".";
            }
        }
        inputConnection.commitText(word + key, 1);
    }

    private int getCursorPosition() {
        ExtractedText extracted = inputConnection.getExtractedText(
                new ExtractedTextRequest(), 0);
        if (extracted == null) {
            return -1;
        }
        return extracted.startOffset + extracted.selectionStart;
    }

    //delete character
    void d(int n) {
        CharSequence selected = inputConnection.getSelectedText(0);

        if(selected == null || selected.length() == 0 ) {
            buffer = (inputConnection.getTextBeforeCursor(n, 0).toString());
            inputConnection.deleteSurroundingTextInCodePoints(n, 0);
        } else {
            buffer = selected.toString();
            inputConnection.commitText("", 1);
        }
    }

    //delete previous word
    void dw(int n) {
        String buf = "";
        for(int i =0; i<n;i++) {
            final int charactersToGet = 30;
            final String splitRegexp = " ";

            // delete trailing spaces
            while (inputConnection.getTextBeforeCursor(1, 0).toString().equals(splitRegexp)) {
                buf += inputConnection.getTextBeforeCursor(1, 0).toString();
                inputConnection.deleteSurroundingText(1, 0);
            }

            // delete last word letters
            String[] words = inputConnection.getTextBeforeCursor(charactersToGet, 0).toString().split(splitRegexp);
            String lastWord = words[words.length - 1];
            buf += lastWord;
            inputConnection.deleteSurroundingText(lastWord.length(), 0);
        }
       buffer = (buf);
    }

    //delete to a character
    void dt(int n, String parameter) {
        String buf = "";
        for(int i =0; i<n;i++) {
            final int charactersToGet = 50;
            final String splitRegexp = parameter.replace("\\", "\\\\");

            // delete trailing spaces
            while (inputConnection.getTextBeforeCursor(1, 0).toString().equals(splitRegexp)) {
                buf += inputConnection.getTextBeforeCursor(1, 0).toString();
                inputConnection.deleteSurroundingText(1, 0);
            }

            // delete last word letters
            String[] words = inputConnection.getTextBeforeCursor(charactersToGet, 0).toString().split(splitRegexp);
            String lastWord = words[words.length - 1];
            buf += (parameter + lastWord);
            inputConnection.deleteSurroundingText(lastWord.length() + parameter.length(), 0);
        }
        buffer = (buf);
    }

    //delete everything
    void dd(int n) {
        inputConnection.performContextMenuAction(android.R.id.selectAll);
        buffer = (inputConnection.getSelectedText(0).toString());
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
    }

    //delete selected or all
    void ds(int n) {
        CharSequence selected = inputConnection.getSelectedText(0);
        if(selected == null || selected.length() == 0 ) {
            inputConnection.performContextMenuAction(android.R.id.selectAll);
        }
        buffer = (inputConnection.getSelectedText(0).toString());
        inputConnection.commitText("", 1);
    }

    //copy all
    void yy(int n) {
        int currentPosition = getCursorPosition();
        inputConnection.performContextMenuAction(android.R.id.selectAll);
        buffer = (inputConnection.getSelectedText(0).toString());
        inputConnection.performContextMenuAction(android.R.id.copy);
        inputConnection.setSelection(currentPosition, currentPosition);
    }

    //copy selected
    void y(int n) {
        buffer = (inputConnection.getSelectedText(0).toString());
        inputConnection.performContextMenuAction(android.R.id.copy);
    }

    //select all
    void sa(int n) {
        int currentPosition = getCursorPosition();
        inputConnection.performContextMenuAction(android.R.id.selectAll);
    }


    //insert text
    void i(int n, String parameter) {
        for(int i=0;i<n;i++) {
            if (buffer != null) {
                commitText(parameter.replaceAll("\\$0", buffer));
            } else {
                commitText(parameter);
            }
        }

    }

    //insert text raw (without autospace etc)
    void iraw(int n, String parameter) {
        for(int i=0;i<n;i++) {
            if (buffer != null) {
                inputConnection.commitText(parameter.replaceAll("\\$0", buffer), 1);
            } else {
                inputConnection.commitText(parameter, 1);
            }
        }
    }

    //
    void fancy(int n, String parameter) {
            inputConnection.commitText(ConvertUnicode.convert(buffer, parameter), 1);
    }

    //find and replace
    void fr(int n, String parameter) {
        String from = parameter.split(";")[0];
        String to = parameter.split(";")[1];
        dd(1);
        String contents = buffer;
        inputConnection.commitText(contents.replaceAll(from, to), 1);

    }

    //send
    void s(int n) {
        if((inputEditor.imeOptions & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_SEND) {
            inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND);
        }
    }

    //paste from buffer
    void p(int n) {
        String str = buffer;
        if(str != null) {
            for(int i=0;i<n;i++) {
                inputConnection.commitText(str, 0);
            }
        } else {
            inputConnection.performContextMenuAction(android.R.id.paste);
        }

    }

    //paste from clipboard
    void pc(int n) {
        for(int i=0;i<n;i++) {
            inputConnection.performContextMenuAction(android.R.id.paste);
        }
    }

    //make uppercase
    void upper(int n, String parameter) {
        for(int i=0;i<n;i++) {
            String lastBufferWord = buffer;
            if (lastBufferWord != null) {
                inputConnection.commitText(parameter.replaceAll("\\$0", lastBufferWord).toUpperCase(), 1);
            } else {
                inputConnection.commitText(parameter.toUpperCase(), 1);
            }
        }
    }

    //make lowercase
    void lower(int n, String parameter) {
        for(int i=0;i<n;i++) {
            String lastBufferWord = buffer;
            if (lastBufferWord != null) {
                inputConnection.commitText(parameter.replaceAll("\\$0", lastBufferWord).toLowerCase(), 1);
            } else {
                inputConnection.commitText(parameter.toLowerCase(), 1);
            }
        }
    }

    //random from kboard keys
    void rnd(int n) {
        rnd(n, null);
    }

    //random from list or all kboard keys
    void rnd(int n, String parameter) {
        List<String> textKeys = new ArrayList<>();
        if(parameter!= null && parameter.length() > 0) {
            textKeys.addAll(Arrays.asList(parameter.split(";", 100)));
        } else {
            textKeys = mTextKeys;
        }
        for(int i=0; i<n; i++) {
            Random random = new Random();
            int index = random.nextInt(textKeys.size());
            i(1, textKeys.get(index));
        }
    }

    //random emoji
    void rnde(int n) {
        for(int i=0; i<n; i++) {
            Random random = new Random();
            int index = random.nextInt(mEmoji.size());
            iraw(1, ((Emoji) mEmoji.toArray()[index]).getUnicode());
        }
    }

    //move cursor left
    void j(int n) {
        int position = getCursorPosition() - n;
        if(position < 0) {
            position = 0;
        }
        inputConnection.setSelection(position, position);
    }

    //move cursor right
    void k(int n) {
        int position = getCursorPosition() + n;
        CharSequence textAfterCursor = inputConnection.getTextAfterCursor(n, 0);
        if(textAfterCursor.length() == n) {
            inputConnection.setSelection(position, position);
        } else {
            inputConnection.setSelection(position-(textAfterCursor.length() - n), position-(textAfterCursor.length() - n));
        }
    }

    //move back a word
    void b(int n) {
        for(int i=0;i<n;i++) {
            final String splitRegexp = " ";


            // delete last word letters
            String[] words = inputConnection.getTextBeforeCursor(MAX_LOOKBACK, 0).toString().split(splitRegexp);
            String lastWord = words[words.length - 1];
            int position = getCursorPosition() - lastWord.length() - 1;
            if(position < 0) {
                position = 0;
            }
            inputConnection.setSelection(position, position);
        }

    }

    //move forward a word
    void w(int n) {
        for(int i=0;i<n;i++) {
            final String splitRegexp = " ";

            String[] words = inputConnection.getTextAfterCursor(MAX_LOOKBACK, 0).toString().split(splitRegexp);
            String nextWord = words[0];

            int position = getCursorPosition() + nextWord.length() + 1;
            if(inputConnection.getTextAfterCursor(nextWord.length() + 1, 0).length() == nextWord.length() + 1) {
                inputConnection.setSelection(position, position);
            } else {
                inputConnection.setSelection(position-1, position-1);
            }
        }
    }

    //move to start of line
    void startOfLine(int n) {
        for(int i=0;i<n;i++) {
            final String splitRegexp = "(?<=\\n)";

            String[] lines = inputConnection.getTextBeforeCursor(MAX_LOOKBACK, 0).toString().split(splitRegexp);
            String lastLine = lines[lines.length - 1];
            int position = getCursorPosition() - lastLine.length();
            if(position < 0) {
                position = 0;
            }
            inputConnection.setSelection(position, position);
        }
    }

    //move to end of line
    void endOfLine(int n) {
        for(int i=0;i<n;i++) {
            final String splitRegexp = "\\n";

            String[] lines = inputConnection.getTextAfterCursor(MAX_LOOKBACK, 0).toString().split(splitRegexp);
            int nextLineLength = lines[0].equals("") ? lines[1].length() + 1 : lines[0].length();
            int position = getCursorPosition() + nextLineLength;
            inputConnection.setSelection(position, position);
        }
    }

    //execute subcommand
    void e(int n, String cmd) {
        //Remove leading !
        if(cmd.startsWith("!")) {
            cmd = cmd.substring(1);
        }
        for(int i=0;i<n;i++) {
            String commands[];
            if(cmd.matches("^\\d+e.*")) {
                commands = new String[1];
                commands[0] = cmd;
                int numberOfTimes = cmd.indexOf("e") > 0 ? Integer.parseInt(cmd.substring(0, cmd.indexOf("e"))) : 1;
                String parameter = cmd.substring(cmd.indexOf("(") + 1, cmd.lastIndexOf(")"));
                e(numberOfTimes, parameter);
            } else {
                commands = cmd.split(",");
                for(String command : commands) {
                    String commandMethod;
                    String parameter = null;
                    int numberOfTimes = 1;
                    String[] commandMethodParts = command.split("(\\((?!\\))|,|(?<!\\()\\))"); //split out parameter in brackets
                    if(commandMethodParts.length > 1) { //has parameter
                        commandMethod = commandMethodParts[0];
                        parameter = commandMethodParts[1].replaceAll("\\$0", buffer);
                    } else {
                        commandMethod = commandMethodParts[0];
                    }


                    String[] commandParts = commandMethod.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)"); //split between number and non-number
                    if(commandParts.length > 1) { //has numericPart
                        numberOfTimes = Integer.parseInt(commandParts[0]);
                        commandMethod = commandParts[1];
                    } else {
                        commandMethod = commandParts[0];
                    }

                    execute(commandMethod, numberOfTimes, parameter);

                }
            }


        }

    }

    private boolean execute(String cmd, int n, String parameter) {
        inputConnection.beginBatchEdit();

        if(cmd.equals("^")) {
            cmd = "startOfLine";
        }
        if(cmd.equals("$")) {
            cmd = "endOfLine";
        }
        try {
            if(parameter != null) {
                getClass().getMethod(cmd, int.class, String.class).invoke(this, n, parameter);
            } else {
                getClass().getMethod(cmd, int.class).invoke(this, n);

            }
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            if(mCommandKeys.containsKey(cmd) && inputConnection != null) {
                e(n, mCommandKeys.get(cmd));
            }
            e.printStackTrace();
        }
        inputConnection.endBatchEdit();
        return false;
    }

}
