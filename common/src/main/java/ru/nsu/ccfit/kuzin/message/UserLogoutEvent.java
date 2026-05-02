package ru.nsu.ccfit.kuzin.message;

import ru.nsu.ccfit.kuzin.message.event.Event;

public record UserLogoutEvent(String name) implements Event {
}
