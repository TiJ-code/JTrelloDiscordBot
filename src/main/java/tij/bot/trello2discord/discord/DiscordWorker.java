package tij.bot.trello2discord.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import tij.bot.trello2discord.common.Constants;
import tij.bot.trello2discord.common.Message;
import tij.bot.trello2discord.common.MessageBuffer;
import tij.bot.trello2discord.config.Config;
import tij.bot.trello2discord.config.ConfigUserMapping;

import java.util.List;
import java.util.Optional;

public class DiscordWorker {

    private final JDA jda;
    private final String channelId;
    private final MessageBuffer buffer;
    private final List<ConfigUserMapping> userMappings;

    public DiscordWorker(Config config, MessageBuffer buffer) throws InterruptedException {
        this.channelId = config.discordChannelId();
        this.userMappings = config.discordTrelloUserMapping();
        this.buffer = buffer;

        System.out.println("Connecting to Discord...");
        this.jda = JDABuilder.createDefault(config.discordBotToken()).build().awaitReady();
        System.out.println("Discord connected successfully.");
    }

    public void startConsuming() {
        Thread consumerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Message msg = buffer.pop();
                    publish(msg);
                } catch (InterruptedException e) {
                    System.err.println("Discord worker thread interupted.");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error publishing to Discord: " + e.getMessage());
                }
            }
        });

        consumerThread.setDaemon(true);
        consumerThread.setName("Discord-Publisher-Thread");
        consumerThread.start();
    }

    private void publish(Message msg) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            System.err.println("Error: Could not find Discord channel with ID: " + channelId);
            return;
        }

        Optional<String> discordAuthorId = userMappings.stream()
                .filter(m -> m.trelloUserId().equals(msg.getAuthorName()))
                .map(ConfigUserMapping::discordUserId)
                .findFirst();

        User discordAuthor = discordAuthorId
                .map(id -> jda.getUserById(Long.parseLong(id)))
                .orElse(null);

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Constants.COLOR_TRELLO_BLUE)
                .setTitle(msg.getTitle())
                .setDescription(msg.getBody())
                .setFooter(
                        "Trello • " + msg.getBoardName(),
                        "https://www.vectorlogo.zone/logos/trello/trello-icon.svg"
                );

        if (discordAuthor != null) {
            embed.setAuthor(
                    discordAuthor.getEffectiveName(),
                    null,
                    discordAuthor.getEffectiveAvatarUrl()
            );
        } else {
            embed.setAuthor(
                    msg.getAuthorName(),
                    null,
                    msg.getAuthorAvatarUrl()
            );
        }

        msg.getFields().forEach((k, v) -> embed.addField(k, v, false));

        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
