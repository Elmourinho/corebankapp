package com.elmar.corebankapp.services;

public interface MessageService {

    void sendMessageAsync(String queueName, Object object);
}
