package com.app.bitcoinvault.util;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Used to setting the typeface of the textviews.
 */
public class FontManager {

    public static final String ROOT = "fonts/",
            FONTAWESOME = ROOT + "bitvault.ttf";

    public static final String ROBOTOLIGHT = ROOT + "Roboto-Light.ttf";
    public static final String ROBOTOITALIC = ROOT + "Roboto-Italic.ttf";

    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

}

