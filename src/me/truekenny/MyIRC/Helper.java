package me.truekenny.MyIRC;

import java.util.ArrayList;

/**
 * Created by truekenny on 29.07.14.
 */

public class Helper {
    /**
     * Конвертирует ArrayList<String> в строку через пробел
     * @param alData
     * @return
     */
    public static String convertArrayList(ArrayList<String> alData) {
        String result = "";

        for(String data : alData) {
            result += data + " ";
        }

        return result.trim();
    }
}
