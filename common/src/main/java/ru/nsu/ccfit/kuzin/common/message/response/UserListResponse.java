package ru.nsu.ccfit.kuzin.common.message.response;

import java.util.List;

public record UserListResponse(List<UserInfo> users) implements Response {
}
