package ru.nsu.ccfit.kuzin.message.command;

import ru.nsu.ccfit.kuzin.message.Command;

public record ChatMessageCommand (String sessionId, String text) implements Command {
}
