package com.adgad.kboard;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
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

    InputConnection inputConnection;
    EditorInfo inputEditor;
    static ArrayDeque<String> buffer = new ArrayDeque<String>();

    public KCommands(InputConnection ic, EditorInfo ei) {
        inputConnection = ic;
        inputEditor = ei;
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

    public void dd(int n) {
        inputConnection.performContextMenuAction(android.R.id.selectAll);
        buffer.push(inputConnection.getSelectedText(0).toString());
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
    }

    public void yy(int n) {
        int currentPosition = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).selectionEnd;
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
        int position = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).selectionEnd - n;
        if(position < 0) {
            position = 0;
        }
        inputConnection.setSelection(position, position);
    }

    public void k(int n) {
        int position = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).selectionEnd + n;
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
            final int charactersToGet = 30;
            final String splitRegexp = " ";


            // delete last word letters
            String[] words = inputConnection.getTextBeforeCursor(charactersToGet, 0).toString().split(splitRegexp);
            String lastWord = words[words.length - 1];
            int position = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).selectionEnd - lastWord.length() - 1;
            if(position < 0) {
                position = 0;
            }
            inputConnection.setSelection(position, position);
        }

    }

    //move forward a word
    public void w(int n) {
        for(int i=0;i<n;i++) {
            final int charactersToGet = 30;
            final String splitRegexp = " ";


            String[] words = inputConnection.getTextAfterCursor(charactersToGet, 0).toString().split(splitRegexp);
            String nextWord = words[0];

            int position = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).selectionEnd + nextWord.length() + 1;
            if(inputConnection.getTextAfterCursor(nextWord.length() + 1, 0).length() == nextWord.length() + 1) {
                inputConnection.setSelection(position, position);
            } else {
                inputConnection.setSelection(position-1, position-1);
            }
        }

    }

    public boolean execute(String cmd, int n, String parameter) {
        inputConnection.beginBatchEdit();
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
