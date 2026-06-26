package tij.bot.trello2discord;

import com.sun.net.httpserver.HttpServer;
import tij.bot.trello2discord.common.MessageBuffer;
import tij.bot.trello2discord.config.Config;
import tij.bot.trello2discord.config.ConfigSystem;
import tij.bot.trello2discord.discord.DiscordWorker;
import tij.bot.trello2discord.trello.TrelloWebhookHandler;

import java.net.InetSocketAddress;
import java.util.Optional;

public class Main {
    static void main() {
        Optional<Config> optConfig = ConfigSystem.loadConfig();
        if (optConfig.isEmpty()) {
            System.out.println("Config could not be loaded. Interrupting startup process...");
            System.out.println("Simply restart the system once you have configured it.");
            return;
        }
        Config config = optConfig.get();
        System.out.println("Config loaded successfully.");

        MessageBuffer buffer = new MessageBuffer();

        try {
            DiscordWorker discordWorker = new DiscordWorker(config, buffer);
            discordWorker.startConsuming();

            HttpServer server = HttpServer.create(new InetSocketAddress(config.port()), 0);
            server.createContext("/trello-webhook", new TrelloWebhookHandler(buffer));
            server.setExecutor(null);
            server.start();

            System.out.println("=========================================");
            System.out.println("Trello2Discord Bot is fully online.");
            System.out.println("Listening for webhooks on port: " + config.port());
            System.out.println("=========================================");

        } catch (Exception e) {
            System.err.println("Critical failure during startup:");
            e.printStackTrace();
        }
    }
}