package com.nttdata.bootcamp.service;

import com.nttdata.bootcamp.entity.Deposit;

public interface KafkaService {
    void publish(Deposit customer);
}
