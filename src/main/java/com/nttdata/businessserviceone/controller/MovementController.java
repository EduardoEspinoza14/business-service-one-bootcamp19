package com.nttdata.businessserviceone.controller;

import com.nttdata.businessserviceone.business.MovementService;
import com.nttdata.businessserviceone.model.Movement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Class MovementController.
 */
@RestController
@RequestMapping("/movement")
@CrossOrigin(origins = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
)
public class MovementController {

  private final Logger log = LoggerFactory.getLogger(MovementController.class);

  @Autowired
  MovementService service;

  @GetMapping(value = "/list-movements/{customerId}/{productId}",
          produces = MediaType.APPLICATION_JSON_VALUE)
  public Flux<Movement> listMovements(@PathVariable String customerId,
                                      @PathVariable String productId) {
    return service.listMovementsCustomerProduct(customerId, productId);
  }

  @PostMapping(value = "/account-opening", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Movement> accountOpening(@RequestBody Movement movementDto) {
    return service.accountOpening(movementDto);
  }

  @PostMapping(value = "/register-withdrawal", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Movement> registerWithdrawal(@RequestBody Movement movementDto) {
    return service.registerWithdrawal(movementDto);
  }

  @PostMapping(value = "/register-deposit", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Movement> registerDeposit(@RequestBody Movement movementDto) {
    return service.registerDeposit(movementDto);
  }

  @PostMapping(value = "/register-payment", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Movement> registerPayment(@RequestBody Movement movementDto) {
    return service.registerPayment(movementDto);
  }

  @PostMapping(value = "/register-spent", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Movement> registerSpent(@RequestBody Movement movementDto) {
    return service.registerSpent(movementDto);
  }

}
