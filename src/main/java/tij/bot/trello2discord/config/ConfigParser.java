package tij.bot.trello2discord.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tij.bot.trello2discord.config.utils.ClasspathDtdResolver;
import tij.bot.trello2discord.config.utils.XmlConstants;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

            return switch (version) {
                case 1 -> parseV1(root);
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

        return new Config(channelId, botToken, port, mappings);
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
}
