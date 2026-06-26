package tij.bot.trello2discord.trello;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import tij.bot.trello2discord.common.Message;
import tij.bot.trello2discord.common.MessageBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrelloWebhookHandler implements HttpHandler {
    private static final String RQ_HEAD = "HEAD";
    private static final String RQ_POST = "POST";

    private static final int HTTP_CODE_OK = 200;
    private static final int HTTP_CODE_BAD_REQUEST = 400;
    private static final int HTTP_CODE_NOT_ALLOWED = 405;

    private final MessageBuffer buffer;

    public TrelloWebhookHandler(MessageBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String requestMethod = httpExchange.getRequestMethod();

        if (RQ_HEAD.equalsIgnoreCase(requestMethod)) {
            httpExchange.sendResponseHeaders(HTTP_CODE_OK, -1);
            return;
        }

        if (RQ_POST.equalsIgnoreCase(requestMethod)) {
            InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody());
            String query = new BufferedReader(isr).lines().collect(Collectors.joining("\n"));

            try {
                JSONObject json = new JSONObject(query);
                Optional<Message> optMsg = processTrelloEvent(json);
                if (optMsg.isEmpty()) {
                    httpExchange.sendResponseHeaders(HTTP_CODE_BAD_REQUEST, -1);
                    return;
                }
                buffer.queue(optMsg.get());
            } catch (Exception e) {
                System.err.println("Failed to parse Trello payload: " + e.getMessage());
            }

            String response = "OK";
            httpExchange.sendResponseHeaders(HTTP_CODE_OK, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            httpExchange.sendResponseHeaders(HTTP_CODE_NOT_ALLOWED, -1);
        }
    }

    private Optional<Message> processTrelloEvent(JSONObject json) {
        return TrelloJsonParser.parse(json);
    }
}
