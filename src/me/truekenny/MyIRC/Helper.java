package me.truekenny.MyIRC;

import java.util.ArrayList;

public class Helper {
    /**
     * Конвертирует ArrayList<String> в строку через пробел
     * @param alData Массим строк
     * @return Строка, разделенная пробелами
     */
    public static String convertArrayList(ArrayList<String> alData, String prefix) {
        String result = "";

        for(String data : alData) {
            result += prefix + data + " ";
        }

        return result.trim();
    }
}
