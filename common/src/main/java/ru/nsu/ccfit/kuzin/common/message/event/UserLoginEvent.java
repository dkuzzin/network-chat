package ru.nsu.ccfit.kuzin.common.message.event;

import ru.nsu.ccfit.kuzin.common.message.Event;

public record UserLoginEvent(String name) implements Event {
}
