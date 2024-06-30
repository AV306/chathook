# ChatHook
A logical server mod that forwards Minecraft chat, game and command messages to a Discord webhook.

### How to Use
The config file will be generated automatically in config/chathook.properties.
After that you can change these settings:

``` properties
# Your webhook url
webhook_url=https://discord.com/api/webhooks/[id]/[token]
# Whether the mod should relay anything
enabled=true
# Whether normal chat messages should be sent via webhook
log_chat_messages=true
# Whether login/leave and death messages should be sent via webhook
log_game_messages=true
# Whether command messages should be sent via webhook 
# Command messages are something like this: /say It's MuffinTime. | /me was killed by
log_command_messages=true
```

The webhook URL should be in the form of `https://discord.com/api/webhooks/[id]/[token]`.

Now, just fire up the server and you're all set!

### Commands
```
/chathook [enable|disable]
/chathook [logChat] [enable|disable]
/chathook [logGame] [enable|disable]
/chathook [logCommand] [enable|disable]
/chathook [webhook] [<url>] 
```


### TODO
 * Everything done :)
