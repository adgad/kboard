package com.adgad.kboard;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.view.inputmethod.EditorInfo;

import androidx.core.content.ContextCompat;

/**
 * Created by arjun on 14/03/15.
 */
class KBoard extends Keyboard {
    private Key mEnterKey;

    public KBoard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }


    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
        Key key = new LatinKey(res, parent, x, y, parser);
        if (key.codes[0] == 10) {
            mEnterKey = key;
        }
        return key;
    }

    public void setImeOptions(Context context, int options) {
        Resources res = context.getResources();
        if (mEnterKey == null) {
            return;
        }

        switch (options&(EditorInfo.IME_MASK_ACTION|EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_keyboard_key_go);
                break;
            case EditorInfo.IME_ACTION_NEXT:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_keyboard_key_next);
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                mEnterKey.icon = ContextCompat.getDrawable(context, R.drawable.sym_keyboard_search);
                mEnterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEND:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_keyboard_key_send);
                break;
            default:
                mEnterKey.icon = ContextCompat.getDrawable(context, R.drawable.sym_keyboard_return);
                mEnterKey.label = null;
                break;
        }
    }

    class LatinKey extends Key {
        public LatinKey(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {

            super(res, parent, x, y, parser);
        }

        @Override
        public boolean isInside(int x, int y) {
            return super.isInside(x, codes[0] == KEYCODE_CANCEL ? y - 10 : y);
        }
    }
}
