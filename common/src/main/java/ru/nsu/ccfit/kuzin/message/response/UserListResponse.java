package ru.nsu.ccfit.kuzin.message.response;

import java.util.List;

public record UserListResponse(List<UserInfo> users) implements Response {
}
