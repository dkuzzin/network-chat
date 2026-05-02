package ru.nsu.ccfit.kuzin.common.message.command;

import ru.nsu.ccfit.kuzin.common.message.Command;

public record ChatMessageCommand (String sessionId, String text) implements Command {
}
