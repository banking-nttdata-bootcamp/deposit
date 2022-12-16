package com.nttdata.bootcamp.service.impl;

import com.nttdata.bootcamp.entity.Deposit;
import com.nttdata.bootcamp.entity.enums.EventType;
import com.nttdata.bootcamp.events.DepositCreatedEventKafka;
import com.nttdata.bootcamp.events.EventKafka;
import com.nttdata.bootcamp.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class KafkaServiceImpl implements KafkaService {

    @Autowired
    private KafkaTemplate<String, EventKafka<?>> producer;

    @Value("${topic.deposit.name}")
    private String topicDeposit;

    public void publish(Deposit deposit) {

        DepositCreatedEventKafka created = new DepositCreatedEventKafka();
        created.setData(deposit);
        created.setId(UUID.randomUUID().toString());
        created.setType(EventType.CREATED);
        created.setDate(new Date());

        this.producer.send(topicDeposit, created);
    }

}
