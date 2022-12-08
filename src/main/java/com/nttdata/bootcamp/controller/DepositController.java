package com.nttdata.bootcamp.controller;

import com.nttdata.bootcamp.entity.Deposit;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nttdata.bootcamp.service.DepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "/deposit")
public class DepositController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DepositController.class);
	@Autowired
	private DepositService depositService;

	//Deposits search
	@GetMapping("/findAllDeposits")
	public Flux<Deposit> findAllDeposits() {
		Flux<Deposit> deposits = depositService.findAll();
		LOGGER.info("Registered deposits: " + deposits);
		return deposits;
	}

	//Deposits by AccountNumber
	@GetMapping("/findAllDepositsByAccountNumber/{accountNumber}")
	public Flux<Deposit> findAllDepositsByAccountNumber(@PathVariable("accountNumber") String accountNumber) {
		Flux<Deposit> deposits = depositService.findByAccountNumber(accountNumber);
		LOGGER.info("Registered deposits of account number: "+accountNumber +"-" + deposits);
		return deposits;
	}

	//Deposits  by Number
	@CircuitBreaker(name = "deposits", fallbackMethod = "fallBackGetDeposits")
	@GetMapping("/findByDepositNumber/{numberDeposits}")
	public Mono<Deposit> findByDepositNumber(@PathVariable("numberDeposits") String numberDeposits) {
		LOGGER.info("Searching deposits by number: " + numberDeposits);
		return depositService.findByNumber(numberDeposits);
	}

	//Save deposit
	@CircuitBreaker(name = "deposits", fallbackMethod = "fallBackGetDeposits")
	@PostMapping(value = "/saveDeposits/{commission}/{count}")
	public Mono<Deposit> saveDeposits(@RequestBody Deposit dataDeposit,@PathVariable("commission") Double commission,
									  @PathVariable("count") Long count){
		Mono<Long> countMovementsMono = getCountDeposits(dataDeposit.getAccountNumber());
		Long countMovementS =countMovementsMono.block();

		Mono.just(dataDeposit).doOnNext(t -> {
					if(countMovementS>count)
						t.setCommission(commission);
					else
						t.setCommission(new Double("0.00"));
					t.setTypeAccount("passive");
					t.setCreationDate(new Date());
					t.setModificationDate(new Date());

				}).onErrorReturn(dataDeposit).onErrorResume(e -> Mono.just(dataDeposit))
				.onErrorMap(f -> new InterruptedException(f.getMessage())).subscribe(x -> LOGGER.info(x.toString()));

		Mono<Deposit> depositMono = depositService.saveDeposit(dataDeposit);
		return depositMono;
	}

	//Update deposit
	@CircuitBreaker(name = "deposits", fallbackMethod = "fallBackGetDeposits")
	@PutMapping("/updateDeposit/{numberTransaction}")
	public Mono<Deposit> updateDeposit(@PathVariable("numberTransaction") String numberTransaction,
									   @Valid @RequestBody Deposit dataDeposit) {
		Mono.just(dataDeposit).doOnNext(t -> {

					t.setDepositNumber(numberTransaction);
					t.setModificationDate(new Date());

				}).onErrorReturn(dataDeposit).onErrorResume(e -> Mono.just(dataDeposit))
				.onErrorMap(f -> new InterruptedException(f.getMessage())).subscribe(x -> LOGGER.info(x.toString()));

		Mono<Deposit> updateDeposit = depositService.updateDeposit(dataDeposit);
		return updateDeposit;
	}


	//Delete deposit
	@CircuitBreaker(name = "deposits", fallbackMethod = "fallBackGetDeposits")
	@DeleteMapping("/deleteDeposits/{numberTransaction}")
	public Mono<Void> deleteDeposits(@PathVariable("numberTransaction") String numberTransaction) {
		LOGGER.info("Deleting deposit by number: " + numberTransaction);
		Mono<Void> delete = depositService.deleteDeposit(numberTransaction);
		return delete;

	}

	@GetMapping("/getCommissionsDeposit/{accountNumber}")
	public Flux<Deposit> getCommissionsDepositByAccountNumber(@PathVariable("accountNumber") String accountNumber) {
		Flux<Deposit> commissions = depositService.findByCommission(accountNumber);
		LOGGER.info("List commissions of account number: "+accountNumber +"-" + commissions);
		return commissions;
	}


	@GetMapping("/getCountTransaction/{accountNumber}")
	//get count of deposits
	public Mono<Long> getCountDeposits(@PathVariable("accountNumber") String accountNumber){
		Flux<Deposit> transactions= findAllDepositsByAccountNumber(accountNumber);
		return transactions.count();
	}


	private Mono<Deposit> fallBackGetDeposits(Exception e){
		Deposit deposit= new Deposit();
		Mono<Deposit> staffMono= Mono.just(deposit);
		return staffMono;
	}


}
