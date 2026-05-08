package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.protocol.ProtocolWriter;
import ru.nsu.ccfit.kuzin.exception.ChatException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatRoom {
    private final Map<String, ClientSession> sessionsById = new HashMap<>();
    private final Map<String, ClientSession> sessionsByName = new HashMap<>();

    public synchronized ChatResult<ClientSession> login(String name, String clientType,
                                            ProtocolWriter writer) throws ChatException {

        if (name == null || name.isBlank()){ //todo проверка длины имени
            return ChatResult.error("Name cannot be empty");
        }

        if (sessionsByName.containsKey(name)){
            return ChatResult.error("Name is already used");
        }

        String sessionId = UUID.randomUUID().toString(); //todo
        ClientSession session = new ClientSession(sessionId, name, clientType, writer);

        sessionsById.put(sessionId, session);
        sessionsByName.put(name, session);

        return ChatResult.success(session);
    }
}
