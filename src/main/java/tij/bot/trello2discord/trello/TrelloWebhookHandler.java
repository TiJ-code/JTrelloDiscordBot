package tij.bot.trello2discord.trello;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.net.HttpURLConnection;
import org.json.JSONObject;
import tij.bot.trello2discord.common.Message;
import tij.bot.trello2discord.common.MessageBuffer;
import tij.bot.trello2discord.config.events.EventRegistry;
import tij.bot.trello2discord.config.utils.IgnoreRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrelloWebhookHandler implements HttpHandler {
    private static final String RQ_HEAD = "HEAD";
    private static final String RQ_POST = "POST";

    private final MessageBuffer buffer;
    private final TrelloJsonParser parser;

    public TrelloWebhookHandler(MessageBuffer buffer, EventRegistry eventRegistry, IgnoreRegistry ignoreRegistry) {
        this.buffer = buffer;
        this.parser = new TrelloJsonParser(eventRegistry, ignoreRegistry);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String requestMethod = httpExchange.getRequestMethod();

        if (RQ_HEAD.equalsIgnoreCase(requestMethod)) {
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
            return;
        }

        if (RQ_POST.equalsIgnoreCase(requestMethod)) {
            InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody());
            String query = new BufferedReader(isr).lines().collect(Collectors.joining("\n"));

            try {
                JSONObject json = new JSONObject(query);
                Optional<Message> optMsg = processTrelloEvent(json);
                if (optMsg.isEmpty()) {
                    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1);
                    return;
                }
                buffer.queue(optMsg.get());
            } catch (Exception e) {
                System.err.println("Failed to parse Trello payload: " + e.getMessage());
            }

            String response = "OK";
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, -1);
        }
    }

    private Optional<Message> processTrelloEvent(JSONObject json) {
        return parser.parse(json);
    }
}
