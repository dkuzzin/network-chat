package ru.nsu.ccfit.kuzin.common.message.command;

import ru.nsu.ccfit.kuzin.common.message.Command;

public record LoginCommand(String name, String clientType) implements Command {
}
