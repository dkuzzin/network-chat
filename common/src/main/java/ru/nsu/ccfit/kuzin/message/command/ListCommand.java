package ru.nsu.ccfit.kuzin.message.command;

import ru.nsu.ccfit.kuzin.message.Command;

public record ListCommand(String sessionId) implements Command {
}
