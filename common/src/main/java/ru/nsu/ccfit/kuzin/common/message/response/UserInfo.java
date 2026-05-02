package ru.nsu.ccfit.kuzin.common.message.response;

import java.io.Serializable;


public record UserInfo(String name, String clientType) implements Serializable {
}
