MyIRC
=====

- IRC server inside bukkit plugin.

Version
----

- 1.1.1 – Change nick in IRC (/nick),
    change irc user NICK to NICK_ on join NICK in game,
    change irc user NICK_ to NICK on quit NICK from game;
- 1.1 – Private message (/w or /tell);
- 1.0.2 – List IRC users (/irc);
- 1.0.1 – Fix (onNickChange);
- 1.0 – message between Ingame players and IRC users, hide user host.

Roadway
----

- Dynmap messages;
- Colors between game and irc;
- Hide player in IRC.

config.yml
----

```yml
irc:
  host: irc.example.com
  gameHost: server.example.com
  port: 6667
  channel: '#minecraft'
  creator: example.com
  topic: Welcome to MyIRC channel
messages:
  console:
    onEnable: MyIRC loaded!
    onDisable: MyIRC disabled!
    playerListener: PlayerListener loaded!
  irc:
    erroneusNickname: Erroneus Nickname
    nicknameInUse: Nickname is already in use
    kickOnSameNick: Someone came into the game with your nickname
    noSuchNick: No such nick or channel
  game:
    list: IRC users
    noSuchNick: No such nick
rules:
  hide:
    hosts: google.com:hide,yahoo.com:microsoft.com,.*example.net:clear

```

Implemented ingame commands
----

- /irc – list IRC users;
- /w – send private messages between game<->game, irc<->irc, game<->irc;
- /tell – same /w.

Implemented IRC commands
----

- Join (auto-join);
- Part;
- Quit;
- Privmsg;
- Who;
- Whois.

10x
----
http://dillinger.io – Markdown online editor