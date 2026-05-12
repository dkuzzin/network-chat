package ru.nsu.ccfit.kuzin.common.message.event;

public record MessageEvent(String from, String text) implements Event{
}
