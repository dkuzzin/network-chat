package ru.nsu.ccfit.kuzin.common.message.event;

import ru.nsu.ccfit.kuzin.common.message.Event;

public record MessageEvent(String from, String text) implements Event {
}
