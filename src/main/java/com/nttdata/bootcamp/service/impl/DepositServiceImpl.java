package com.nttdata.bootcamp.service.impl;

import com.nttdata.bootcamp.entity.Deposit;
import com.nttdata.bootcamp.repository.DepositRepository;
import com.nttdata.bootcamp.service.DepositService;
import com.nttdata.bootcamp.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Service implementation
@Service
public class DepositServiceImpl implements DepositService {
    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private KafkaService kafkaService;

    @Override
    public Flux<Deposit> findAll() {
        Flux<Deposit> transactions = depositRepository.findAll();
        return transactions;
    }

    @Override
    public Flux<Deposit> findByAccountNumber(String accountNumber) {
        Flux<Deposit> transactions = depositRepository
                .findAll()
                .filter(x -> x.getAccountNumber().equals(accountNumber));
        return transactions;
    }

    @Override
    public Mono<Deposit> findByNumber(String Number) {
        Mono<Deposit> transaction = depositRepository
                .findAll()
                .filter(x -> x.getDepositNumber().equals(Number))
                .next();
        return transaction;
    }

    @Override
    public Mono<Deposit> saveDeposit(Deposit dataDeposit ) {
        Mono<Deposit> depositMono = findByNumber(dataDeposit.getDepositNumber())
                .flatMap(__ -> Mono.<Deposit>error(new Error("This deposit number " + dataDeposit.getDepositNumber() + "exists")))
                .switchIfEmpty(saveTopic(dataDeposit));
        return depositMono;


    }

    @Override
    public Mono<Deposit> updateDeposit(Deposit dataDeposit) {

        Mono<Deposit> transactionMono = findByNumber(dataDeposit.getDepositNumber());
        try {
            dataDeposit.setDni(transactionMono.block().getDni());
            dataDeposit.setAmount(transactionMono.block().getAmount());
            dataDeposit.setCreationDate(transactionMono.block().getCreationDate());
            return depositRepository.save(dataDeposit);
        }catch (Exception e){
            return Mono.<Deposit>error(new Error("This deposits " + dataDeposit.getAccountNumber() + " do not exists"));
        }
    }

    @Override
    public Mono<Void> deleteDeposit(String Number) {
        Mono<Deposit> transactionMono = findByNumber(Number);
        try {
            Deposit deposit = transactionMono.block();
            return depositRepository.delete(deposit);
        }
        catch (Exception e){
            return Mono.<Void>error(new Error("This deposits whith number" + Number+ " exists"));
        }
    }

    @Override
    public Flux<Deposit> findByCommission(String accountNumber) {
        Flux<Deposit> transactions = depositRepository
                .findAll()
                .filter(x -> x.getCommission()>0);
        return transactions;
    }

    public Mono<Deposit> saveTopic(Deposit dataDeposit){
        Mono<Deposit> monoDeposit = depositRepository.save(dataDeposit);
        this.kafkaService.publish(monoDeposit.block());
        return monoDeposit;
    }

}
