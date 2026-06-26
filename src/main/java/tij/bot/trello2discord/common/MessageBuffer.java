package tij.bot.trello2discord.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class MessageBuffer {
    private final BlockingQueue<Message> buffer = new LinkedBlockingQueue<>();

    public void queue(Message msg) {
        buffer.offer(msg);
    }

    public Message pop() throws InterruptedException {
        return buffer.take();
    }
}
