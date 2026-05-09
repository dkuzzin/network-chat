package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.message.event.MessageEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MessageHistory {
    private final int maxSize;
    private final Deque<MessageEvent> messages = new ArrayDeque<>();

    public MessageHistory(int maxSize){
        this.maxSize = maxSize;
    }

    public synchronized void add(MessageEvent event){
        messages.addLast(event);

        while (messages.size() > maxSize){
            messages.removeFirst();
        }
    }

    public synchronized List<MessageEvent> getHistory(){
        return new ArrayList<>(messages);
    }
}
