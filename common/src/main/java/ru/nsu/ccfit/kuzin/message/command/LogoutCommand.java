package ru.nsu.ccfit.kuzin.message.command;

import ru.nsu.ccfit.kuzin.message.Command;

public record LogoutCommand(String sessionId) implements Command {
}
