package ru.nsu.ccfit.kuzin.message.response;

import java.io.Serializable;


public record UserInfo(String name, String clientType) implements Serializable {
}
