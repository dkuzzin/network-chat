package ru.nsu.ccfit.kuzin.client.core;

import ru.nsu.ccfit.kuzin.common.message.response.UserInfo;

import java.util.List;

public interface ChatClientListener {
    void onLoginSuccess(String sessionId);
    void onError(String message);
    void onUserList(List<UserInfo> users);
    void onMessageReceived(String from, String text);
    void onUserLogin(String name);
    void onUserLogout(String name);
    void onDisconnected();
    void onConnectionError(String message);
}
