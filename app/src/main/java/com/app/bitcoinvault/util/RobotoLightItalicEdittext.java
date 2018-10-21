package com.app.bitcoinvault.util;

/**
 * Font file used for displaying the text in RobotoLightItalic font for Edittext.
 */

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.StringRes;
import android.util.AttributeSet;

public class RobotoLightItalicEdittext extends android.support.v7.widget.AppCompatEditText {

    private CharSequence savedHint;
    public RobotoLightItalicEdittext(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoLightItalicEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoLightItalicEdittext(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Roboto-LightItalic.ttf");
        setTypeface(tf);
    }


    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        validateHint(focused);
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    public void setSavedHint(CharSequence savedHint) {
        this.savedHint = savedHint;
        validateHint(hasFocus());
    }

    public void setSavedHint(@StringRes int resid) {
        setSavedHint(getContext().getResources().getText(resid));
    }

    private void validateHint(boolean focused) {
        if (focused) {
            savedHint = getHint();
        }
        setHint(focused ? "" : savedHint);
    }

}

