MyIRC
=====

- IRC server inside bukkit plugin.

Version
----

- 1.1.2 – Small fixes,
    List bans into IRC (/mode #channel +b),
    hide specific gamers in IRC,
    mode +o for operator into IRC,
    autocomplete nicks for /w and /tell.
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
- Add / Remove ban nick / ip into IRC (like /mode #channel +b user)
- Spy private messages for op
- List IRC users into game by [TAB]
- Global IRC server

```
[ MyIRC Server 1 ]          [ MyIRC Server 4 ]
               \            /
                \          /
            [ Global IRC Server ]
                /          \
               /            \
[ MyIRC Server 2 ]          [ MyIRC Server 3 ]

```

config.yml
----

```yml
irc:
  host: irc.example.com
  gameHost: server.example.com
  port: 6667
  channel: '#minecraft'
  creator: Creator
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
    noSuchNick: No such nick
  game:
    list: IRC users
    noSuchNick: No such nick
rules:
  rewrite:
    hosts: google.com:hide,yahoo.com:microsoft.com
  hide:
    gamers: admin,OpeRaToR

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
- Whois;
- Nick.

10x
----
http://dillinger.io – Markdown online editor