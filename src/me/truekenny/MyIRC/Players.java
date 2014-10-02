package me.truekenny.MyIRC;

import java.util.Hashtable;

/**
 * Информация об игроках
 */
public class Players {
    private static Hashtable<String, PlayerData> playerDataHashtable = new Hashtable<String, PlayerData>();

    public static PlayerData getPlayerData(String nick) {
        PlayerData playerData = playerDataHashtable.get(nick);
        if (playerData == null) {
            playerData = new PlayerData();
            playerDataHashtable.put(nick, playerData);
        }

        return playerData;
    }

    public static void updateIdle(String nick) {
        getPlayerData(nick).updateIdle();
    }
}
