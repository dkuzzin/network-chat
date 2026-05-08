package ru.nsu.ccfit.kuzin.server;

public class ChatResult<T> {
    private final T value;
    private final String errorMessage;

    private ChatResult(T value, String errorMessage){
        this.value = value;
        this.errorMessage = errorMessage;
    }

    public static <T> ChatResult<T> success(T value){
        return new ChatResult<>(value, null);
    }

    public static <T> ChatResult<T> error(String errorMessage){
        return new ChatResult<>(null, errorMessage);
    }

    public boolean isSuccess(){
        return errorMessage == null;
    }

    public T getValue(){
        return value;
    }

    public String getErrorMessage(){
        return errorMessage;
    }
}
