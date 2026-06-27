package tij.bot.trello2discord.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tij.bot.trello2discord.config.events.EventTemplate;
import tij.bot.trello2discord.config.events.FieldTemplate;
import tij.bot.trello2discord.config.utils.ClasspathDtdResolver;
import tij.bot.trello2discord.config.utils.XmlConstants;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ConfigParser {
    private ConfigParser() {}

    public static Config parse(File f) {
        try {
            var factory = DocumentBuilderFactory.newInstance();

            factory.setValidating(false);
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            var builder = factory.newDocumentBuilder();

            Document doc = builder.parse(f);
            Element root = doc.getDocumentElement();

            int version = Integer.parseInt(
                    root.getAttribute(XmlConstants.ATTRIBUTE_CONFIG_VERSION)
            );

            factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);

            builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new ClasspathDtdResolver(version));

            doc = builder.parse(f);
            root = doc.getDocumentElement();

            if (version < XmlConstants.VERSION) {
                System.err.println("Config outdated!");
                System.err.println("It is not guaranteed, that this program will run!");
                System.err.println("Refer to https://github.com/TiJ-code/JTrelloDiscordBot and update your configuration file!");
            }

            return switch (version) {
                case 1 -> parseV1(root);
                case 2 -> parseV2(root);
                default -> throw new IllegalArgumentException(
                        "Unsupported config version: " + version
                );
            };

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse config", e);
        }
    }

    private static Config parseV1(Element root) {
        String channelId = getEntry(root, XmlConstants.ENTRY_DISCORD_CHANNEL_ID);

        String botToken = getEntry(root, XmlConstants.ENTRY_DISCORD_BOT_TOKEN);

        int port = Integer.parseInt(getEntry(root, XmlConstants.ENTRY_PORT));

        List<ConfigUserMapping> mappings = parseUserMappings(root);

        return new Config(channelId, botToken, port, mappings, null);
    }

    private static Config parseV2(Element root) {
        Config configV1 = parseV1(root);

        List<EventTemplate> events = parseEvents(root);

        return new Config(
                configV1.discordChannelId(),
                configV1.discordBotToken(),
                configV1.port(),
                configV1.discordTrelloUserMapping(),
                events);
    }

    private static String getEntry(Element root, String key) {
        NodeList entries = root.getElementsByTagName(XmlConstants.TAG_ENTRY);

        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);

            if (key.equals(entry.getAttribute(XmlConstants.ATTRIBUTE_ENTRY_NAME))) {
                return entry.getTextContent().trim();
            }
        }

        throw new IllegalArgumentException("Missing config entry: " + key);
    }

    private static List<ConfigUserMapping> parseUserMappings(Element root) {
        NodeList mappingGroups = root.getElementsByTagName(XmlConstants.TAG_USER_MAPPINGS);

        if (mappingGroups.getLength() == 0) {
            return List.of();
        }

        Element group = (Element) mappingGroups.item(0);

        List<ConfigUserMapping> result = new ArrayList<>();

        NodeList users = group.getElementsByTagName(XmlConstants.TAG_USER);

        for (int i = 0; i < users.getLength(); i++) {
            Element user = (Element) users.item(i);

            result.add(new ConfigUserMapping(
                    user.getAttribute(XmlConstants.ATTRIBUTE_USER_DISCORD),
                    user.getAttribute(XmlConstants.ATTRIBUTE_USER_TRELLO)
            ));
        }

        return result;
    }

    private static List<EventTemplate> parseEvents(Element root) {
        NodeList events = root.getElementsByTagName(XmlConstants.TAG_EVENT);

        if (events.getLength() == 0)
            return List.of();

        List<EventTemplate> result = new ArrayList<>();

        for (int i = 0; i < events.getLength(); i++) {
            Element event = (Element) events.item(i);

            String name = event.getAttribute(XmlConstants.ATTRIBUTE_EVENT_NAME);

            if (!XmlConstants.ATTRIBUTE_EVENT_NAME_VALUES.contains(name)) {
                throw new RuntimeException("Invalid event name: %s\nPossible values are: %s"
                        .formatted(name, XmlConstants.ATTRIBUTE_EVENT_NAME_VALUES));
            }

            String title = getFormat(event, XmlConstants.ATTR_FORMAT_KEY_VALUE_TITLE);
            String body = getFormat(event, XmlConstants.ATTR_FORMAT_KEY_VALUE_BODY);

            Map<Integer, FieldTemplate> fields = parseFields(event);

            result.add(new EventTemplate(name, title, body, fields));
        }

        return result;
    }

    private static String getFormat(Element parent, String key) {
        NodeList formats = parent.getElementsByTagName(XmlConstants.TAG_FORMAT);

        for (int i = 0; i < formats.getLength(); i++) {
            Element f = (Element) formats.item(i);

            if (key.equals(f.getAttribute(XmlConstants.ATTRIBUTE_FORMAT_KEY))) {
                return f.getTextContent().trim();
            }
        }

        return "";
    }

    private static Map<Integer, FieldTemplate> parseFields(Element event) {
        NodeList fields = event.getElementsByTagName(XmlConstants.TAG_FIELD);

        Map<Integer, FieldTemplate> result = new java.util.HashMap<>();

        for (int i = 0; i < fields.getLength(); i++) {
            Element field = (Element) fields.item(i);

            String title = getFormat(field, XmlConstants.ATTR_FORMAT_KEY_VALUE_TITLE);
            String body = getFormat(field, XmlConstants.ATTR_FORMAT_KEY_VALUE_BODY);

            result.put(i, new FieldTemplate(title, body));
        }

        return result;
    }
}
