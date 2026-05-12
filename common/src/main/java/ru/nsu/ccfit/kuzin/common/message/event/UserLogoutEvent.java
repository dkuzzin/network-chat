package ru.nsu.ccfit.kuzin.common.message.event;

import ru.nsu.ccfit.kuzin.common.message.Event;

public record UserLogoutEvent(String name) implements Event {
}
