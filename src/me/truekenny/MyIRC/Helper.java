package me.truekenny.MyIRC;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Helper {
    /**
     * Префикс оператора
     */
    public static String opPrefix = "@";

    /**
     * Префикс voice
     */
    public static String voicePrefix = "+";

    /**
     * Режим оператора
     */
    public static String opMode = "o";

    /**
     * Режим voice
     */
    public static String voiceMode = "v";

    /**
     * Конвертирует ArrayList<String> в строку через пробел
     *
     * @param nicks  Массим строк
     * @param prefix Префикс по умолчанию
     * @param myirc  Экземпляр плагина
     * @return Строка, разделенная пробелами
     */
    public static String convertArrayList(ArrayList<String> nicks, String prefix, MyIRC myirc) {
        String result = "";

        for (String nick : nicks) {
            result += (prefix.equals(voicePrefix) && isOp(nick, myirc) ? opPrefix : prefix)+nick + " ";
        }

        return result.trim();
    }

    /**
     * Возвращает IP адрес
     *
     * @param fullIP Полный адрес (host/ip:port)
     * @return IP
     */
    public static String convertFullIPToIP(String fullIP) {
        return fullIP.split("/")[1].split(":")[0];
    }

    /**
     * Ник является оператором
     *
     * @param nick  Ник
     * @param myirc Жкземпляр плагина
     * @return Результат
     */
    public static boolean isOp(String nick, MyIRC myirc) {
        Player player = myirc.getServer().getPlayer(nick);

        return (player instanceof Player && player.isOp());
    }
}
