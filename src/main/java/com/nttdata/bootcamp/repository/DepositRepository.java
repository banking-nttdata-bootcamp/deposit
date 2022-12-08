package com.nttdata.bootcamp.repository;

import com.nttdata.bootcamp.entity.Deposit;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

//Mongodb Repository
public interface DepositRepository extends ReactiveCrudRepository<Deposit, String> {
}
