package me.truekenny.MyIRC;

/**
 * Информация об игроке
 */
public class PlayerData {
    private String _lastMessage = "";
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

    public String getLastMessage() {
        return _lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        _lastMessage = lastMessage;
    }

}
