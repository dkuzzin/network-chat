package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.message.event.MessageEvent;
import ru.nsu.ccfit.kuzin.common.message.response.UserInfo;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolWriter;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class ChatRoom {
    private final Map<String, ClientSession> sessionsById = new HashMap<>();
    private final Map<String, ClientSession> sessionsByName = new HashMap<>();
    private static final int MAX_NAME_LENGTH = 20; //TODO анести в конфиг
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final Logger logger = Logger.getLogger(ChatRoom.class.getName());
    private final MessageHistory messageHistory;

    public ChatRoom(int historySize){
        this.messageHistory = new MessageHistory(historySize);
    }


    public synchronized ChatResult<ClientSession> login(String name, String clientType,
                                            ProtocolWriter writer){

        ChatResult<String> vapResult =  validateAndPrepareName(name);
        if (!vapResult.isSuccess()){
            return ChatResult.error(vapResult.getErrorMessage());
        }

        String normalName = vapResult.getValue();

        if (sessionsByName.containsKey(getLowerCaseName(normalName))){
            return ChatResult.error("Name is already used");
        }

        String sessionId = UUID.randomUUID().toString(); //todo разобраться, добавить настройку
        ClientSession session = new ClientSession(sessionId, normalName, clientType, writer);

        sessionsById.put(sessionId, session);
        sessionsByName.put(getLowerCaseName(normalName), session);

        return ChatResult.success(session);
    }

    public synchronized boolean disconnect(ClientSession session){
        if (session == null){
            return false;
        }
        ClientSession current = sessionsById.get(session.getSessionId());
        if (current == null){
            return false;
        }
        sessionsById.remove(session.getSessionId());
        sessionsByName.remove(getLowerCaseName(session.getName()));
        return true;
    }

    private ChatResult<String> validateAndPrepareName(String name){
        if (name == null){
            return ChatResult.error("Name cannot be empty");
        }
        String trimmedName = name.trim();

        if (trimmedName.isEmpty()){
            return ChatResult.error("Name cannot be empty");
        }
        if (trimmedName.length() > MAX_NAME_LENGTH){
            return ChatResult.error("Name is too long. Max length: " + MAX_NAME_LENGTH);
        }

        return ChatResult.success(trimmedName);
    }

    public synchronized ChatResult<List<UserInfo>> getUser(String sessionId){
        if (sessionId == null || !sessionsById.containsKey(sessionId)){
            return ChatResult.error("You don't have access to list of users, login please");
        }
        List<UserInfo> users = new ArrayList<>();

        for (ClientSession session : sessionsById.values()){
            users.add(new UserInfo(session.getName(), session.getClientType()));
        }

        return ChatResult.success(users);
    }

    public synchronized ChatResult<MessageEvent> createMessageEvent(String sessionId, String text){
        ClientSession sender = sessionsById.get(sessionId);
        if (sender == null){
            return ChatResult.error("Invalid session. The sender is not logged in");
        }

        if (text == null || text.isBlank()){
            return ChatResult.error("Message cannot be empty");
        }

        String preparedText = text.trim();

        if (preparedText.length() > MAX_MESSAGE_LENGTH){
            return ChatResult.error("Message is too long. Max length: " + MAX_MESSAGE_LENGTH);
        }
        MessageEvent event = new MessageEvent(sender.getName(), preparedText);
        messageHistory.add(event);
        return ChatResult.success(event);
    }

    public void broadcast(Message message){
        List<ClientSession> receivers;
        synchronized (this){
            receivers = new ArrayList<>(sessionsById.values());
        }

        for (ClientSession receiver : receivers){
            try{
                receiver.sendToClient(message);
            } catch (IOException e) {
                disconnect(receiver);
            }
        }
    }

    public void broadcastExcept(Message message, String excludedSessionId){
        List<ClientSession> receivers;

        synchronized (this){
            receivers = new ArrayList<>();

            for (ClientSession session: sessionsById.values()){
                if (!session.getSessionId().equals(excludedSessionId)){
                    receivers.add(session);
                }
            }
        }

        for (ClientSession receiver : receivers){
            try{
                receiver.sendToClient(message);
            }catch (IOException e){
                logger.warning("Failed to send message to " + receiver.getName() + ": " + e.getMessage());
                disconnect(receiver);
            }
        }
    }

    private String getLowerCaseName(String name){
        return name.toLowerCase(Locale.ROOT);
    }

    public List<MessageEvent> getMessageHistory() {
        return messageHistory.getHistory();
    }
}
