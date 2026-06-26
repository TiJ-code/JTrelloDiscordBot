package tij.bot.trello2discord;

import tij.bot.trello2discord.config.Config;
import tij.bot.trello2discord.config.ConfigSystem;

import java.util.Optional;

public class Main {
    static void main() {
        Optional<Config> optConfig = ConfigSystem.loadConfig();
        if (optConfig.isEmpty()) {
            System.out.println("Config could not be loaded. Interrupting startup process...");
            System.out.println("Simply restart the system, once you configured");
            return;
        }

        System.out.println(optConfig.get());
    }
}
