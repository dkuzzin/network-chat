package ru.nsu.ccfit.kuzin.message.event;

public record MessageEvent(String srcUser, String text) implements Event{
}
