package com.elmar.corebankapp.services.impl;

import com.elmar.corebankapp.services.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    @Async
    public void sendMessageAsync(String queueName, Object object) {
        rabbitTemplate.convertAndSend(queueName, object);
    }
}
