# Trello 2 Discord Bot

A lightweight webhook-based bot that forwards certain Trello activity into Discord.

This bot listens for Trello webhook events, converts them into structured messages using
configurable templates, and posts them into a Discord channel.

## Features

- Discord webhook forwarding using a Discord bot
- XML-based configuration
- Event-specific template messages
- Dynamic JSON path rendering
- User mapping (Trello 2 Discord)
- Custom message fields
- Supports formatting placeholders
- DTD validation for configuration files

## Supported Events

| Event                      | Description                                |
|----------------------------|--------------------------------------------|
| `CARD_CREATED`             | Triggered when a new card is created       |
| `CARD_COMMENTED`           | Triggered when someones comments on a card |
| `CARD_MOVED`               | Triggered when cards move between lists    |
| `CARD_ADDED_LABEL`         | Triggered when labels are added            |
| `CARD_REMOVED_LABEL`       | Triggered when labels are removed          |
| `CARD_DESCRIPTION_CHANGED` | Triggered when card descriptions change    |
| `CARD_TITLE_CHANGED`       | Triggered when card titles change          |

---

# Installation

Clone:

```bash
git clone https://github.com/TiJ-code/JTrelloDiscordBot.git
cd JTrelloDiscordBot
```

Build:

```bash
mvn clean package
```

Run:

```bash
java -jar target/trello2discord-<version>.jar
```

--- 

# Configuration

Configuration stored in:

```text
config.xml
```

The configuration format is validated using:

```text
config.2.dtd
```

If no configuration is present, the program will not start up normally.
However, it will copy a default configuration file parallel to the executable jar.

This example config file looks like this:

```xml
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE config SYSTEM "config.2.dtd">
<config version="2">
    <entry name="discord.channel.id">YOUR_CHANNEL_ID_HERE</entry>
    <entry name="discord.bot.token">YOUR_BOT_TOKEN_HERE</entry>
    <entry name="port">65000</entry>
    <userMapping>
        <user discord.user.id="DISCORD_ID" trello.user.id="TRELLO_ID"/>
    </userMapping>
    <events>
        <event name="CARD_CREATED">
            <format key="title">📥 New Card Created</format>
            <format key="body">**Card:** {#data.card.name}</format>
            <fields>
                <field>
                    <format key="title">List</format>
                    <format key="body">{#data.card.name}</format>
                </field>
            </fields>
        </event>
        <event name="CARD_COMMENTED">
            <format key="title">💬 New Comment on Card</format>
            <format key="body">**Card:** {#data.card.name}{!N}{!N}**Comment:** {#data.text}</format>
        </event>
        <event name="CARD_MOVED">
            <format key="title">🚚 Card Moved</format>
            <format key="body">**Card:** {#card.name}</format>
            <fields>
                <field>
                    <format key="title">From</format>
                    <format key="body">{#data.listBefore.name}</format>
                </field>
                <field>
                    <format key="title">To</format>
                    <format key="body">{#data.listAfter.name}</format>
                </field>
            </fields>
        </event>
        <event name="CARD_ADDED_LABEL">
            <format key="title">🏷️ Added Label To Card</format>
            <format key="body">**Card:** {#data.card.name}</format>
            <fields>
                <field>
                    <format key="title">Label</format>
                    <format key="body">{#data.label.name}</format>
                </field>
            </fields>
        </event>
        <event name="CARD_REMOVED_LABEL">
            <format key="title">🏷️ Removed Label From Card</format>
            <format key="body">**Card:** {#data.card.name}</format>
            <fields>
                <field>
                    <format key="title">Label</format>
                    <format key="body">{#data.label.name}</format>
                </field>
            </fields>
        </event>
        <event name="CARD_DESCRIPTION_CHANGED">
            <format key="title">📝 Description Updated</format>
            <format key="body">**Card:** {#card.name}</format>
            <fields>
                <field>
                    <format key="title">Before</format>
                    <format key="body">{#data.old.desc}</format>
                </field>
                <field>
                    <format key="title">After</format>
                    <format key="body">{#data.card.desc}</format>
                </field>
            </fields>
        </event>
        <event name="CARD_TITLE_CHANGED">
            <format key="title">✏️ Title Updated</format>
            <format key="body">**Card:** {#data.card.name}</format>
            <fields>
                <field>
                    <format key="title">Before</format>
                    <format key="body">{#data.old.name}</format>
                </field>
                <field>
                    <format key="title">After</format>
                    <format key="body">{#data.card.name}</format>
                </field>
            </fields>
        </event>
    </events>
</config>
```

## Entries

```xml
<entry name="discord.channel.id"></entry>
```
Target discord channel.

```xml
<entry name="discord.bot.token"></entry>
```
Token of your Discord application.

```xml
<entry name="port"></entry>
```
Local HTTP port used for incoming Trello webhook requests.

```xml
<userMapping>
  <user discord.user.id="123" trello.user.id="abc"/>
</userMapping>
```
Allows Trello users to be mapped to Discord users.

### Event Templates

Events define how messages appear in Discord.

Structure:

```xml
<event name="EVENT">
  <format key="title"></format>
  <format key="body"></format>
  <fields>
    ...
  </fields>
</event>
```

##### Template Variables

Templates support JSON path access.
Those JSON paths represent the webhook JSON content paths.

Syntax:

```text
{#path.to.value}
```

Example:

```xml
{#data.card.name}
```

##### Escape Sequences

Templates also support formatting sequences.

**New Line**

```text
{!N}
```

Example:

```xml
<format key="body">
Line 1{!N}Line 2
</format>
```

##### Fields

Additional Discord embed fields.

Example:

```xml
<fields>

    <field>

        <format key="title">
            Label
        </format>

        <format key="body">
            {#data.label.name}
        </format>

    </field>

</fields>
```


##### Example

```xml
<event name="CARD_COMMENTED">

    <format key="title">
        💬 New Comment
    </format>

    <format key="body">
        **Card:** {#data.card.name}{!N}{!N}
        **Comment:** {#data.text}
    </format>

</event>
```

Result:

```text
💬 New Comment

Card: Improve renderer

Comment: Looks good now.
```

#### JSON Path Rules

Templates operate relative to:

```text
action
```

Meaning:

```text
{#data.card.name}
```

maps to:

```json
action.data.card.name
```

Do not include:

```text
action.
```

inside templates.


# Notes

* Trello payloads are not fully consistent across event types.
* Some events may omit old values.
* Move events may contain either:

  * position updates
  * list changes
* Missing paths resolve to empty strings.
