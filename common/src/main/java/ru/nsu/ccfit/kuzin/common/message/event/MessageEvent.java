package ru.nsu.ccfit.kuzin.common.message.event;

public record MessageEvent(String srcUser, String text) implements Event{
}
