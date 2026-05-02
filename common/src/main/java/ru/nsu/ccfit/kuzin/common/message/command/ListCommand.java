package ru.nsu.ccfit.kuzin.common.message.command;

import ru.nsu.ccfit.kuzin.common.message.Command;

public record ListCommand(String sessionId) implements Command {
}
