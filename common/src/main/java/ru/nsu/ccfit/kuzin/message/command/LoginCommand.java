package ru.nsu.ccfit.kuzin.message.command;

import ru.nsu.ccfit.kuzin.message.Command;

public record LoginCommand(String name, String clientType) implements Command {
}
