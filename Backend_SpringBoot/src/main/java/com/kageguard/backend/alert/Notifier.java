package com.kageguard.backend.alert;

public interface Notifier {

    String name();

    void send(String subject, String message) throws Exception;
}