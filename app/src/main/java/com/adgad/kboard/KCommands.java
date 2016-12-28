package com.adgad.kboard;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by arjun on 27/12/16.
 */


public class KCommands {

    public static final int MAX_LOOKBACK = 500;

    InputConnection inputConnection;
    EditorInfo inputEditor;
    static ArrayDeque<String> buffer = new ArrayDeque<String>();

    public KCommands(InputConnection ic, EditorInfo ei) {
        inputConnection = ic;
        inputEditor = ei;
    }


    private int getCursorPosition() {
        ExtractedText extracted = inputConnection.getExtractedText(
                new ExtractedTextRequest(), 0);
        if (extracted == null) {
            return -1;
        }
        return extracted.startOffset + extracted.selectionStart;
    }


    public void d(int n) {
        buffer.push(inputConnection.getTextBeforeCursor(n, 0).toString());
        for(int i=0;i < n; i++) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
        }

    }

    public void dw(int n) {
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
       buffer.push(buf);
    }

    //delete to a character
    public void dt(int n, String parameter) {
        String buf = "";
        for(int i =0; i<n;i++) {
            final int charactersToGet = 30;
            final String splitRegexp = parameter;

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
        buffer.push(buf);
    }

    public void dd(int n) {
        inputConnection.performContextMenuAction(android.R.id.selectAll);
        buffer.push(inputConnection.getSelectedText(0).toString());
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
    }

    public void yy(int n) {
        int currentPosition = getCursorPosition();
        inputConnection.performContextMenuAction(android.R.id.selectAll);
        buffer.push(inputConnection.getSelectedText(0).toString());
        inputConnection.performContextMenuAction(android.R.id.copy);
        inputConnection.setSelection(currentPosition, currentPosition);
    }

    public void y(int n) {
        buffer.push(inputConnection.getSelectedText(0).toString());
        inputConnection.performContextMenuAction(android.R.id.copy);
    }

    public void i(int n, String parameter) {
        for(int i=0;i<n;i++) {
            String lastBufferWord = buffer.peek();
            if (lastBufferWord != null) {
                inputConnection.commitText(parameter.replaceAll("\\$0", lastBufferWord), 1);
            } else {
                inputConnection.commitText(parameter, 1);
            }
        }

    }

    //send
    public void s(int n) {
        if((inputEditor.imeOptions & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_SEND) {
            inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND);
        }
    }

    public void p(int n) {
        String str = buffer.peek();
        if(str != null) {
            for(int i=0;i<n;i++) {
                inputConnection.commitText(str, 0);
            }
        } else {
            inputConnection.performContextMenuAction(android.R.id.paste);
        }

    }

    public void upper(int n, String parameter) {
        for(int i=0;i<n;i++) {
            String lastBufferWord = buffer.peek();
            if (lastBufferWord != null) {
                inputConnection.commitText(parameter.replaceAll("\\$0", lastBufferWord).toUpperCase(), 1);
            } else {
                inputConnection.commitText(parameter.toUpperCase(), 1);
            }
        }
    }

    public void lower(int n, String parameter) {
        for(int i=0;i<n;i++) {
            String lastBufferWord = buffer.peek();
            if (lastBufferWord != null) {
                inputConnection.commitText(parameter.replaceAll("\\$0", lastBufferWord).toLowerCase(), 1);
            } else {
                inputConnection.commitText(parameter.toLowerCase(), 1);
            }
        }
    }

    public void j(int n) {
        int position = getCursorPosition() - n;
        if(position < 0) {
            position = 0;
        }
        inputConnection.setSelection(position, position);
    }

    public void k(int n) {
        int position = getCursorPosition() + n;
        CharSequence textAfterCursor = inputConnection.getTextAfterCursor(n, 0);
        if(textAfterCursor.length() == n) {
            inputConnection.setSelection(position, position);
        } else {
            inputConnection.setSelection(position-(textAfterCursor.length() - n), position-(textAfterCursor.length() - n));
        }
    }

    //move back a word
    public void b(int n) {
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
    public void w(int n) {
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

    //move forward a word
    public void startOfLine(int n) {
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

    //move forward a word
    public void endOfLine(int n) {
        for(int i=0;i<n;i++) {
            final String splitRegexp = "\\n";

            String[] lines = inputConnection.getTextAfterCursor(MAX_LOOKBACK, 0).toString().split(splitRegexp);
            int nextLineLength = lines[0].equals("") ? lines[1].length() + 1 : lines[0].length();
            int position = getCursorPosition() + nextLineLength;
            inputConnection.setSelection(position, position);
        }
    }

    public void e(int n, String cmd) {
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
                    String commandMethod = null;
                    String parameter = null;
                    int numberOfTimes = 1;
                    String[] commandMethodParts = command.split("(\\((?!\\))|,|(?<!\\()\\))"); //split out parameter in brackets
                    if(commandMethodParts.length > 1) { //has parameter
                        commandMethod = commandMethodParts[0];
                        parameter = commandMethodParts[1].replaceFirst("\\$0", buffer.peek());
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

    public boolean execute(String cmd, int n, String parameter) {
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
            e.printStackTrace();
        }
        inputConnection.endBatchEdit();
        return false;
    }

}
