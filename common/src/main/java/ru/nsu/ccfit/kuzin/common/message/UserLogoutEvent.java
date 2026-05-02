package ru.nsu.ccfit.kuzin.common.message;

import ru.nsu.ccfit.kuzin.common.message.event.Event;

public record UserLogoutEvent(String name) implements Event {
}
