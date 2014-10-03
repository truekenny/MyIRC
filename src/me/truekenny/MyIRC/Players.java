package me.truekenny.MyIRC;

import org.bukkit.entity.Player;

import java.util.Hashtable;

/**
 * Информация об игроках
 */
public class Players {
    private static Hashtable<String, PlayerData> playerDataHashtable = new Hashtable<String, PlayerData>();

    public static PlayerData getPlayerData(Player player) {
        String nick = player.getName();

        PlayerData playerData = playerDataHashtable.get(nick);
        if (playerData == null) {
            playerData = new PlayerData(
                    Helper.convertFullIPToIP(player.getAddress().toString()),
                    player.getAddress().getHostName()
            );
            playerDataHashtable.put(nick, playerData);
        }

        return playerData;
    }

    /**
     * Обновляет парамерт Idle
     *
     * @param player
     */
    public static void updateIdle(Player player) {
        getPlayerData(player).updateIdle();
    }

    /**
     * Удаляет ник из памяти
     *
     * @param nick
     */
    public static void remove(String nick) {
        playerDataHashtable.remove(nick);
    }
}
