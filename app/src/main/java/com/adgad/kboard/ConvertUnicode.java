package com.adgad.kboard;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arjun on 19/12/18.
 */
class ConvertUnicode {

    private static String normal = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";


    static String convert(String src, String type) {
        Map<String, String> conversions = new HashMap<>();
        conversions.put("circle", "⓪①②③④⑤⑥⑦⑧⑨ⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩⒶⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏ");
        conversions.put("darkcircle", "⓿❶❷❸❹❺❻❼❽❾🅐🅑🅒🅓🅔🅕🅖🅗🅘🅙🅚🅛🅜🅝🅞🅟🅠🅡🅢🅣🅤🅥🅦🅧🅨🅩🅐🅑🅒🅓🅔🅕🅖🅗🅘🅙🅚🅛🅜🅝🅞🅟🅠🅡🅢🅣🅤🅥🅦🅧🅨🅩");

        if(!conversions.containsKey(type)) {
            return src;
        }
        String convert = conversions.get(type);
        for(int i = 0; i < normal.length(); i++) {
            src = src.replaceAll(String.valueOf(normal.charAt(i)), String.valueOf(convert.charAt(i)));
        }
        return src;
    }
}

