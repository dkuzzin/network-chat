package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.protocol.ProtocolWriter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ChatRoom {
    private final Map<String, ClientSession> sessionsById = new HashMap<>();
    private final Map<String, ClientSession> sessionsByName = new HashMap<>();
    private static final int MAX_NAME_LENGTH = 20; //TODO анести в конфиг

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

    public synchronized void disconnect(ClientSession session){
        if (session == null){
            return;
        }
        ClientSession current = sessionsById.get(session.getSessionId());
        if (current == null){
            return;
        }
        sessionsById.remove(session.getSessionId());
        sessionsByName.remove(getLowerCaseName(session.getName()));

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

    private String getLowerCaseName(String name){
        return name.toLowerCase(Locale.ROOT);
    }
}
