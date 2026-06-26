package tij.bot.trello2discord.config.utils;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.InputStream;

public class ClasspathDtdResolver implements EntityResolver {
    private static final String DTD_FORMAT = "config.%d.dtd";

    private final String dtdName;

    public ClasspathDtdResolver(int version) {
        this.dtdName = DTD_FORMAT.formatted(version);
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        try {
            InputStream stream =
                    getClass().getClassLoader()
                            .getResourceAsStream(dtdName);

            if (stream == null) {
                throw new IllegalStateException(
                        "Missing DTD in resources: " + dtdName
                );
            }

            return new InputSource(stream);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load internal DTD", e);
        }
    }
}