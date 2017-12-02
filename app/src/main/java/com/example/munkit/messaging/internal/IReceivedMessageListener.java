package com.example.munkit.messaging.internal;

import com.example.munkit.messaging.model.ReceivedMessage;

public interface IReceivedMessageListener {

    void onMessageReceived(ReceivedMessage message);
}