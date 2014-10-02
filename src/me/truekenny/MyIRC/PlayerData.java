package me.truekenny.MyIRC;

/**
 * Информация об игроке
 */
public class PlayerData {
    public long timeConnect = System.currentTimeMillis() / 1000L;
    public long timeIdle = System.currentTimeMillis() / 1000L;

    public void updateIdle() {
        timeIdle = System.currentTimeMillis() / 1000L;
    }

}
