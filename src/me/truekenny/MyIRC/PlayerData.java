package me.truekenny.MyIRC;

/**
 * Информация об игроке
 */
public class PlayerData {
    public long timeConnect = System.currentTimeMillis() / 1000L;
    public long timeIdle = System.currentTimeMillis() / 1000L;

    public String ip;
    public String host;

    public PlayerData(String ip, String host) {
        this.ip = ip;
        this.host = host;
    }

    public void updateIdle() {
        timeIdle = System.currentTimeMillis() / 1000L;
    }

}
