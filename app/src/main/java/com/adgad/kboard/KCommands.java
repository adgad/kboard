package com.adgad.kboard;

import android.content.ClipDescription;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import org.apache.commons.text.StringEscapeUtils;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by arjun on 27/12/16.
 */


public class KCommands {

    private static final int MAX_LOOKBACK = 500;

    private final InputConnection inputConnection;
    private final EditorInfo inputEditor;
    private final boolean mAutoSpace;
    private final boolean mPassiveAggressive;
    private final Collection<Emoji> mEmoji;
    private final List<String> mTextKeys = new ArrayList<>();
    private final Map<String,String> mCommandKeys = new HashMap<>();
    private String buffer = null;
    private final KboardIME mIme;

    public KCommands(
            KboardIME ime,
            InputConnection ic,
            EditorInfo ei,
            List<String> keys,
            boolean autoSpace,
            boolean passiveAggressive) {
        inputConnection = ic;
        inputEditor = ei;
        mAutoSpace = autoSpace;
        mPassiveAggressive = passiveAggressive;
        List<String> mKeys = keys;
        mIme = ime;
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
        Objects.requireNonNull(inputConnection).commitText(word + key, 1);
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
    public void d(int n) {
        CharSequence selected = inputConnection.getSelectedText(0);

        if(selected == null || selected.length() == 0 ) {
            buffer = (inputConnection.getTextBeforeCursor(n, 0).toString());
            inputConnection.deleteSurroundingText(n,0);
        } else {
            buffer = selected.toString();
            inputConnection.commitText("", 1);
        }
    }

    //delete previous word
    public void dw(int n) {
        StringBuilder buf = new StringBuilder();
        for(int i =0; i<n;i++) {
            final int charactersToGet = 30;
            final String splitRegexp = " ";

            // delete trailing spaces
            while (inputConnection.getTextBeforeCursor(1, 0).toString().equals(splitRegexp)) {
                buf.append(inputConnection.getTextBeforeCursor(1, 0).toString());
                inputConnection.deleteSurroundingText(1, 0);
            }

            // delete last word letters
            String[] words = inputConnection.getTextBeforeCursor(charactersToGet, 0).toString().split(splitRegexp);
            String lastWord = words[words.length - 1];
            buf.append(lastWord);
            inputConnection.deleteSurroundingText(lastWord.length(), 0);
        }
       buffer = (buf.toString());
    }

    //delete to a character
    public void dt(int n, String parameter) {
        StringBuilder buf = new StringBuilder();
        for(int i =0; i<n;i++) {
            final int charactersToGet = 50;
            final String splitRegexp = parameter.replace("\\", "\\\\");

            // delete trailing spaces
            while (inputConnection.getTextBeforeCursor(1, 0).toString().equals(splitRegexp)) {
                buf.append(inputConnection.getTextBeforeCursor(1, 0).toString());
                inputConnection.deleteSurroundingText(1, 0);
            }

            // delete last word letters
            String[] words = inputConnection.getTextBeforeCursor(charactersToGet, 0).toString().split(splitRegexp);
            String lastWord = words[words.length - 1];
            buf.append(parameter).append(lastWord);
            inputConnection.deleteSurroundingText(lastWord.length() + parameter.length(), 0);
        }
        buffer = (buf.toString());
    }

    //delete everything
    public void dd(int n) {
        inputConnection.performContextMenuAction(android.R.id.selectAll);
        buffer = (inputConnection.getSelectedText(0).toString());
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
    }

    //delete selected or all
    public void ds(int n) {
        CharSequence selected = inputConnection.getSelectedText(0);
        if(selected == null || selected.length() == 0 ) {
            inputConnection.performContextMenuAction(android.R.id.selectAll);
        }
        buffer = (inputConnection.getSelectedText(0).toString());
        inputConnection.commitText("", 1);
    }

    //copy all
    public void yy(int n) {
        int currentPosition = getCursorPosition();
        inputConnection.performContextMenuAction(android.R.id.selectAll);
        buffer = (inputConnection.getSelectedText(0).toString());
        inputConnection.performContextMenuAction(android.R.id.copy);
        inputConnection.setSelection(currentPosition, currentPosition);
    }

    //copy selected
    public void y(int n) {
        buffer = (inputConnection.getSelectedText(0).toString());
        inputConnection.performContextMenuAction(android.R.id.copy);
    }

    //copy selected or all
    public void ys(int n) {
        CharSequence selected = inputConnection.getSelectedText(0);
        if(selected == null || selected.length() == 0 ) {
            int currentPosition = getCursorPosition();
            inputConnection.performContextMenuAction(android.R.id.selectAll);
            buffer = (inputConnection.getSelectedText(0).toString());
            inputConnection.performContextMenuAction(android.R.id.copy);
            inputConnection.setSelection(currentPosition, currentPosition);
        } else {
            buffer = (inputConnection.getSelectedText(0).toString());
        }
    }


    //select all
    public void sa(int n) {
        int currentPosition = getCursorPosition();
        inputConnection.performContextMenuAction(android.R.id.selectAll);
    }


    //insert text
    public void i(int n, String parameter) {
        for(int i=0;i<n;i++) {
                commitText(replaceDollarWords(parameter));
        }

    }

    //insert text raw (without autospace etc)
    public void iraw(int n, String parameter) {
        for(int i=0;i<n;i++) {
                inputConnection.commitText(parameter, 1);
        }
    }

    //
    public void fancy(int n, String parameter) {
        for(int i=0;i<n;i++) {
            inputConnection.commitText(ConvertUnicode.convert(buffer, parameter), 1);
        }
    }

    //find and replace
    public void fr(int n, String parameter) {
        String from = parameter.split(";")[0];
        String to = parameter.split(";")[1];
        dd(1);
        String contents = buffer;
        inputConnection.commitText(contents.replaceAll(from, to), 1);

    }

    //send
    public void s(int n) {
        if((inputEditor.imeOptions & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_SEND ) {
            inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND);
        // Whatsapp seems to now have IME_ACTION_DONE, but sending still works so do that instead
        } else if((inputEditor.imeOptions & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_DONE ) {
            inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND);
        }
    }

    //paste from buffer
    public void p(int n) {
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
    public void pc(int n) {
        for(int i=0;i<n;i++) {
            inputConnection.performContextMenuAction(android.R.id.paste);
        }
    }

    //make uppercase
    public void upper(int n, String parameter) {
        for(int i=0;i<n;i++) {
                inputConnection.commitText(parameter.toUpperCase(), 1);
        }
    }

    //make lowercase
    public void lower(int n, String parameter) {
        for(int i=0;i<n;i++) {

                inputConnection.commitText(parameter.toLowerCase(), 1);

        }
    }

    //random from kboard keys
    public void rnd(int n) {
        rnd(n, null);
    }

    //random from list or all kboard keys
    public void rnd(int n, String parameter) {
        List<String> textKeys;
        if(parameter!= null && parameter.length() > 0) {
            textKeys = new ArrayList<>(Arrays.asList(parameter.split(";", 100)));
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
    public void rnde(int n) {
        for(int i=0; i<n; i++) {
            Random random = new Random();
            int index = random.nextInt(mEmoji.size());
            iraw(1, ((Emoji) mEmoji.toArray()[index]).getUnicode());
        }
    }

    //move cursor left
    public void j(int n) {
        int position = getCursorPosition() - n;
        if(position < 0) {
            position = 0;
        }
        inputConnection.setSelection(position, position);
    }

    //move cursor right
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

    //move to start of line
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

    //move to end of line
    public void endOfLine(int n) {
        for(int i=0;i<n;i++) {
            final String splitRegexp = "\\n";

            String[] lines = inputConnection.getTextAfterCursor(MAX_LOOKBACK, 0).toString().split(splitRegexp);
            int nextLineLength = lines[0].equals("") ? lines[1].length() + 1 : lines[0].length();
            int position = getCursorPosition() + nextLineLength;
            inputConnection.setSelection(position, position);
        }
    }

    public void curl(int n, String parameter) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(mIme);
        RequestFuture<String> future = RequestFuture.newFuture();

        final int repeat = n;


        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, parameter, future, future){
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", "curl");
                headers.put("Accept", "text/plain");
                return headers;
            }
        };

        queue.add(stringRequest);

        try {
            String response = future.get(15, TimeUnit.SECONDS);
            i(repeat, response);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private Uri getImageUri (Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(mIme.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void img(int n, String parameter) {
        RequestQueue queue = Volley.newRequestQueue(mIme);
        RequestFuture<Bitmap> future = RequestFuture.newFuture();
        ImageRequest imageRequest = new ImageRequest(parameter, future, 0,0,null,null, future);
        queue.add(imageRequest);
        try {
            Bitmap response = future.get(30, TimeUnit.SECONDS);
            Uri uri = getImageUri(response);
            InputContentInfo inputContentInfo = new InputContentInfo(
                    uri,

                    new ClipDescription("", new String[]{"image/png"})
            );
            int flags = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                flags |= inputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
            }
            inputConnection.commitContent(inputContentInfo, flags, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void utf(int n, String parameter) {
        for(int i=0;i<n;i++) {
            inputConnection.commitText(StringEscapeUtils.unescapeJava(parameter), 1);
        }
    }

    //execute subcommand
    public void e(final int n, final String command) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String cmd = command;
                //TODO your background code
                if(cmd.startsWith("!")) {
                    cmd = cmd.substring(1);
                }
                for(int i=0;i<n;i++) {
                    String commands[];
                    if(cmd.matches("^\\d+e.*")) {
                        commands = new String[1];
                        commands[0] = cmd;
                        int numberOfTimes = cmd.indexOf("e") > 0 ? Integer.parseInt(cmd.substring(0, cmd.indexOf("e"))) : 1;
                        String parameter = replaceDollarWords(cmd.substring(cmd.indexOf("(") + 1, cmd.lastIndexOf(")")));
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
                                parameter = replaceDollarWords(commandMethodParts[1]);

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
        }).start();

    }

    private String replaceDollarWords(String initial) {
        String newWord = initial;
        if(buffer != null) {
            newWord = newWord.replace("$0", buffer);
        }
        if(KboardAccessibilityService.getCurrentWhatsappName() != null) {
            String fullName = KboardAccessibilityService.getCurrentWhatsappName();
            String[] names = fullName.split(" ");

            if(names.length > 1) {
                newWord = newWord.replace("$fname", names[0]);
                newWord = newWord.replace("$lname", names[1]);
            } else {
                newWord = newWord.replace("$fname", fullName);
            }

            newWord = newWord.replace("$name", KboardAccessibilityService.getCurrentWhatsappName());
        }
        return newWord;
    }

    private void execute(String cmd, int n, String parameter) {
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
            return;
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
    }

}
