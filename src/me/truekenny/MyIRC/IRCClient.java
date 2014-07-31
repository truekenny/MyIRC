package me.truekenny.MyIRC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IRCClient implements Runnable {
    /**
     * Сокет клиента
     */
    private Socket sock;

    /**
     * Входящий поток
     */
    private BufferedReader in;

    /**
     * Исходящий поток
     */
    private OutputStream out;

    /**
     * Хост клиента
     */
    private String host;

    /**
     * Экземпляр сервера
     */
    private IRCServer ircServer;

    /**
     * Окончание строки при отправке клиенту
     */
    private static final String CRLF = "\r\n";

    /**
     * Имя клиента
     */
    private String nick = null;

    /**
     * Идентификатор клиента
     */
    private String id;

    /**
     * Занятость клиента
     */
    private boolean busy = false;

    /**
     * Паттерн для проверки ника
     */
    public static final Pattern nickPattern = Pattern.compile("[a-zA-Zа-яА-Я0-9ёЁ_]{3,15}");

    /**
     * Объект для логирования сообщений плагина
     */
    Logger log = Logger.getLogger("Minecraft");

    /**
     * Обрабатывает подключение нового клиента
     *
     * @param srv
     * @param s
     * @param i
     */
    public IRCClient(IRCServer srv, Socket s, int i) {
        try {
            ircServer = srv;
            sock = s;
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = s.getOutputStream();
            host = s.getInetAddress().getHostName();
            id = "" + i;
            write("NOTICE AUTH :id " + id);
            new Thread(this).start();
        } catch (IOException e) {
            log.info("failed ClientConnection " + e);
        }
    }

    public String toString() {
        return id + " " + host + " " + nick;
    }

    public String getHost() {
        return ircServer.myIRC.host(host);
    }

    public String getId() {
        return id;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean b) {
        busy = b;
    }

    public String getNick() {
        return nick;
    }

    public String getFullName() {
        return nick + "!" + id + "@" + getHost();
    }

    /**
     * Закрывает подключение
     */
    public void close() {
        log.info("= " + id + ": disconnected");

        ircServer.kill(this);
        try {
            sock.close();
        } catch (IOException e) {
        }
    }

    /**
     * Отправляет сообщение клиенту
     *
     * @param s
     */
    public void write(String s) {
        log.info("> " + id + ": «" + s + "»");

        s = s + CRLF;
        byte buf[];
        buf = s.getBytes(Charset.forName("UTF-8"));

        try {
            out.write(buf, 0, buf.length);
        } catch (IOException e) {
            close();
        }
    }

    /**
     * Читает с клиента строку
     *
     * @return
     */
    private String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    static private final int NICK = 1;

    static private final int QUIT = 2;

    static private final int PRIVMSG = 3;

    static private final int PART = 4;

    static private final int WHO = 5;

    static private final int WHOIS = 6;

    static private final int PING = 7;

    static private Hashtable<String, Integer> keys = new Hashtable<String, Integer>();

    static private String keystrings[] = {"", "nick", "quit", "privmsg", "part", "who", "whois", "ping"};

    static {
        for (int i = 0; i < keystrings.length; i++)
            keys.put(keystrings[i], new Integer(i));
    }

    /**
     * Возвращает идентификатор команды
     *
     * @param s
     * @return
     */
    private int lookup(String s) {
        Integer i = keys.get(s.toLowerCase());
        return i == null ? -1 : i.intValue();
    }

    /**
     * Главный цикл ожидающий данные с клиента
     */
    public void run() {
        String s;
        StringTokenizer st;
        while ((s = readLine()) != null) {
            log.info("< " + id + ": «" + s + "»");

            st = new StringTokenizer(s);

            if (st.hasMoreTokens() == false) continue;

            String keyword = st.nextToken();
            switch (lookup(keyword)) {
                default:
                    log.info("bogus keyword: " + keyword + "\r");
                    break;
                case NICK:
                    if(nick != null) continue;

                    if (st.hasMoreTokens() == false) continue;
                    String newNick = st.nextToken();

                    Matcher nickMatcher = nickPattern.matcher(newNick);
                    if (nickMatcher.matches() == false) {
                        write(":" + ircServer.host + " 432 * " + newNick + " :" + ircServer.myIRC.config.getString("messages.irc.erroneusNickname"));

                        continue;
                    }

                    if (ircServer.myIRC.isUniqueNick(newNick) == false) {
                        write(":" + ircServer.host + " 433 * " + newNick + " :" + ircServer.myIRC.config.getString("messages.irc.nicknameInUse"));

                        continue;
                    }

                    //nick = st.nextToken() + (st.hasMoreTokens() ? " " + st.nextToken(CRLF) : "");
                    nick = newNick;
                    log.info("[" + new Date() + "] " + this + "\r");
                    ircServer.set(id, this);

                    sendStatistic();

                    join();

                    break;
                case PART:
                case QUIT:
                    close();
                    return;
                case PRIVMSG:
                    if (st.hasMoreTokens() == false) continue;
                    String dest = st.nextToken();

                    if (dest.equals(ircServer.channel) == false) {
                        write(":" + ircServer.host + " 404 " + nick + " " + dest + " :" + ircServer.myIRC.config.getString("messages.irc.privateOff"));

                        continue;
                    }

                    if (st.hasMoreTokens() == false) continue;
                    String body = st.nextToken(CRLF).trim();
                    // ircServer.sendto(dest, body);
                    ircServer.privmsg(id, getFullName(), body);
                    break;
                /*
                case PART:
                    busy = true;
                    ircServer.part(id);
                    break;
                */
                case WHO:
                    if (st.hasMoreTokens() == false) continue;
                    String who = st.nextToken();
                    ircServer.who(this, who);

                    break;

                case WHOIS:
                    if (st.hasMoreTokens() == false) continue;
                    String whois = st.nextToken();
                    ircServer.whois(this, whois);

                    break;

                case PING:
                    if (st.hasMoreTokens() == false) continue;
                    String idPing = st.nextToken();

                    write("PONG " + idPing);

                    break;
            }
        }
        close();
    }

    /**
     * Отправляет статистику пользователю
     */
    public void sendStatistic() {
        write(":" + ircServer.host + " 001 " + nick + " :Welcome to the MyIRC Network, " + getFullName());
        write(":" + ircServer.host + " 005 " + nick + " PREFIX=(ohv)@%+");
        write("NOTICE " + nick + " :Ingame " + ircServer.myIRC.userList());
        write("NOTICE " + nick + " :Inchat " + ircServer.userList());
    }

    /**
     * Добавляет пользователя на канал
     */
    public void join() {
        // Сообщение для пользователя
        write(":" + getFullName() + " JOIN :" + ircServer.channel);
        write(":" + ircServer.host + " 353 " + nick + " = " + ircServer.channel + " :" +
                Helper.convertArrayList(ircServer.userList(), "") + " " + Helper.convertArrayList(ircServer.myIRC.userList(), "+"));
        write(":" + ircServer.host + " 366 " + nick + " " + ircServer.channel + " :End of /NAMES list.");

        // Сообщение для пользователей IRC
        ircServer.join(id, getFullName());
    }
}