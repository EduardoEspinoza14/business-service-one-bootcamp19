package com.nttdata.businessserviceone.business.impl;

import com.nttdata.businessserviceone.business.CustomerService;
import com.nttdata.businessserviceone.business.MovementService;
import com.nttdata.businessserviceone.business.ProductService;
import com.nttdata.businessserviceone.model.Customer;
import com.nttdata.businessserviceone.model.Movement;
import com.nttdata.businessserviceone.model.Product;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Class MovementServiceImpl.
 */
@Service
public class MovementServiceImpl implements MovementService {

  private static final String CIRCUIT_BREAKER_SERVICE_MOVEMENT = "cbServiceMovement";

  private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @Value("${api.movement.baseUri}")
  private String baseUri;

  @Autowired
  CustomerService customerService;

  @Autowired
  ProductService productService;

  @Autowired
  WebClient.Builder webClientBuilder;

  @Autowired
  ReactiveResilience4JCircuitBreakerFactory reactiveCircuitBreakerFactory;

  @Override
  public Mono<Movement> insertMovementDefault(Movement movement) {
    movement.setId(null);
    movement.setDate(new Date());
    return webClientBuilder.build()
            .post()
            .uri(baseUri).contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(movement))
            .retrieve()
            .bodyToMono(Movement.class)
            .transform(it ->
                    reactiveCircuitBreakerFactory.create(CIRCUIT_BREAKER_SERVICE_MOVEMENT)
                            .run(it, this::movementFallback)
            );
  }

  @Override
  public Flux<Movement> listMovementsCustomerProduct(String customerId, String productId) {
    return productService.getProductByCustomerAndId(customerId, productId)
            .flatMapMany(product ->
              (product.getId() == null
                      || product.getId().isEmpty()
                      || product.getId().equals("")
              ) ? Flux.empty() : this.listMovementsProduct(product.getId()));
  }

  @Override
  public Flux<Movement> listMovementsProduct(String productId){
    return webClientBuilder.build()
            .get()
            .uri(baseUri + "/product/{productId}", productId)
            .retrieve()
            .bodyToFlux(Movement.class)
            .transform(it -> reactiveCircuitBreakerFactory.create(CIRCUIT_BREAKER_SERVICE_MOVEMENT)
                    .run(it, this::movementsFallback));
  }

  @Override
  public Mono<Movement> accountOpening(Movement movementDto) {
    return Mono.justOrEmpty(movementDto)
            .defaultIfEmpty(new Movement(new Customer(""), new Product("")))
            .map(Movement::getCustomer)
            .flatMap(customerService::checkCustomerExistsElseCreate)
            .doOnNext(cus1 -> movementDto.getCustomer().setId(cus1.getId()))
            .doOnNext(cus1 -> movementDto.getProduct().setCustomerId(cus1.getId()))
            .flatMap(cus1 -> this.validateCustomerCanProduct(movementDto))
            .defaultIfEmpty(new Product(""))
            .flatMap(pro1 ->
                    (pro1.getId() == null || pro1.getId().isEmpty())
                    ? productService.insertProduct(pro1) : Mono.just(pro1))
            .flatMap(product -> {
              Customer customer = movementDto.getCustomer();
              if (product.getId() == null
                      || product.getId().isEmpty()
                      || product.getId().equals("")
                      || customer.getId() == null
                      || customer.getId().isEmpty()
                      || customer.getId().equals("")
              ) {
                return Mono.empty();
              } else {
                movementDto.setConcept(Movement.MOVEMENT_CONCEPT_1);
                movementDto.setType(Movement.MOVEMENT_TYPE_1);
                movementDto.setProduct(product);
                return this.insertMovementDefault(movementDto);
              }
            })
            .map(movement -> {
              BeanUtils.copyProperties(movementDto.getProduct(), movement.getProduct(), "id");
              BeanUtils.copyProperties(movementDto.getCustomer(), movement.getCustomer(), "id");
              return movement;
            });
    //SI EL OJB MOVEMENTMONGO ES NULL O SU ID ES NULL
    // DEBERIA REVERTIRSE EL REGISTRO DEL CLIENTE Y DEL PRODUCTO
  }

  @Override
  public Mono<Movement> registerWithdrawal(Movement movementDto) {
    movementDto.setConcept(Movement.MOVEMENT_CONCEPT_2);
    movementDto.setType(Movement.MOVEMENT_TYPE_2);
    return this.registerMovementOfAccounts(movementDto);
  }

  @Override
  public Mono<Movement> registerDeposit(Movement movementDto) {
    movementDto.setConcept(Movement.MOVEMENT_CONCEPT_3);
    movementDto.setType(Movement.MOVEMENT_TYPE_1);
    return this.registerMovementOfAccounts(movementDto);
  }

  private Mono<Long> countByProductIdAndDateBetween(String productId, LocalDateTime start, LocalDateTime end){
    return webClientBuilder.build()
            .get()
            .uri(uriBuilder -> uriBuilder
                    .path(baseUri + "/product/{productId}/count")
                    .queryParam("start", "{start}")
                    .queryParam("end", "{end}")
                    .build(productId, start, end))
            .retrieve()
            .bodyToMono(Long.class)
            .transform(it -> reactiveCircuitBreakerFactory.create(CIRCUIT_BREAKER_SERVICE_MOVEMENT)
                    .run(it, this::countMovementFallback));
  }

  private Mono<Movement> registerMovementOfAccounts(Movement movementDto) {
    return Mono.justOrEmpty(movementDto)
            .defaultIfEmpty(new Movement(new Customer(""), new Product("")))
            .map(Movement::getCustomer)
            .flatMap(cus -> customerService.getCustomerById(cus.getId()))
            .doOnNext(movementDto::setCustomer)
            .flatMap(cus -> productService
                    .getProductByCustomerAndId(cus.getId(), movementDto.getProduct().getId())
            )
            .doOnNext(movementDto::setProduct)
            .flatMap(product -> {
              Customer customer = movementDto.getCustomer();
              if (product.getId() == null
                      || product.getId().isEmpty()
                      || product.getId().equals("")
                      || customer.getId() == null
                      || customer.getId().isEmpty()
                      || customer.getId().equals("")) {
                return Mono.empty();
              } else if (product.getType().equals(Product.PRODUCT_TYPE_1)) {
                LocalDate today = LocalDate.now();
                LocalDateTime firstOfMonth = today.withDayOfMonth(1)
                        .atStartOfDay();
                LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
                        .atStartOfDay();
                return this.countByProductIdAndDateBetween(product.getId(), firstOfMonth, endOfMonth)
                        .flatMap(count -> {
                          if (product.getMaxMovementLimit() == null) {
                            return Mono.empty();
                          } else if (count < product.getMaxMovementLimit()) {
                            return this.insertMovementDefault(movementDto);
                          } else {
                            return Mono.empty();
                          }
                        });
              } else if (product.getType().equals(Product.PRODUCT_TYPE_2)) {
                return this.insertMovementDefault(movementDto);
              } else if (product.getType().equals(Product.PRODUCT_TYPE_3)) {
                LocalDate today = LocalDate.now();
                if (product.getSingleDayMovement().equals(today.getMonthValue())) {
                  LocalDateTime startOfDay = LocalDateTime
                          .of(today, LocalTime.MIN);
                  LocalDateTime endOfDay = LocalDateTime
                          .of(today, LocalTime.MIN);
                  return this.countByProductIdAndDateBetween(product.getId(), startOfDay, endOfDay)
                          .flatMap(count -> {
                            if (count == 0) {
                              return this.insertMovementDefault(movementDto);
                            } else {
                              return Mono.empty();
                            }
                          });
                } else {
                  return Mono.empty();
                }
              } else {
                return Mono.empty();
              }
            })
            .map(movement -> {
              BeanUtils.copyProperties(movementDto.getProduct(), movement.getProduct(), "id");
              BeanUtils.copyProperties(movementDto.getCustomer(), movement.getCustomer(), "id");
              return movement;
            });
  }

  @Override
  public Mono<Movement> registerPayment(Movement movementDto) {
    movementDto.setConcept(Movement.MOVEMENT_CONCEPT_4);
    movementDto.setType(Movement.MOVEMENT_TYPE_1);
    return this.registerMovementOfCredits(movementDto);
  }

  @Override
  public Mono<Movement> registerSpent(Movement movementDto) {
    movementDto.setConcept(Movement.MOVEMENT_CONCEPT_5);
    movementDto.setType(Movement.MOVEMENT_TYPE_2);
    return this.registerMovementOfCredits(movementDto);
  }

  private Mono<Movement> registerMovementOfCredits(Movement movementDto) {
    return Mono.justOrEmpty(movementDto)
            .defaultIfEmpty(new Movement(new Customer(""), new Product("")))
            .map(Movement::getCustomer)
            .flatMap(cus -> customerService.getCustomerById(cus.getId()))
            .doOnNext(movementDto::setCustomer)
            .flatMap(cus -> productService
                    .getProductByCustomerAndId(cus.getId(), movementDto.getProduct().getId())
            )
            .doOnNext(movementDto::setProduct)
            .flatMap(product -> {
              Customer customer = movementDto.getCustomer();
              if (product.getId() == null
                      || product.getId().isEmpty()
                      || product.getId().equals("")
                      || customer.getId() == null
                      || customer.getId().isEmpty()
                      || customer.getId().equals("")
              ) {
                return Mono.empty();
              } else if (movementDto.getType().equals(Movement.MOVEMENT_TYPE_2)
                      && product.getType().equals(Product.PRODUCT_TYPE_5)
              ) {
                return Mono.empty();
              } else if (movementDto.getType().equals(Movement.MOVEMENT_TYPE_2)
                      && product.getType().equals(Product.PRODUCT_TYPE_4)
                      && movementDto.getAmount() > product.getCreditLimit()
              ) {
                return Mono.empty();
              } else {
                return this.insertMovementDefault(movementDto);
              }
            })
            .map(movement -> {
              BeanUtils.copyProperties(movementDto.getProduct(), movement.getProduct(), "id");
              BeanUtils.copyProperties(movementDto.getCustomer(), movement.getCustomer(), "id");
              return movement;
            });
  }

  @Override
  public Mono<Product> validateCustomerCanProduct(Movement movementDto) {
    return Mono.just(movementDto)
            .map(Movement::getCustomer)
            .flatMap(customer -> {
              if (movementDto.getProduct() == null
                      || movementDto.getProduct().getType() == null
                      || movementDto.getProduct().getType().isEmpty()
              ) {
                return Mono.empty();
              } else if (customer.getType().equals(Customer.CUSTOMER_TYPE_1)) {
                if (movementDto.getProduct().getType().equals(Product.PRODUCT_TYPE_4)) {
                  return Mono.just(movementDto.getProduct());
                } else {
                  return productService.getProductsByCustomer(customer.getId()).collectList()
                          .flatMap(products -> {
                            boolean repeated = products.stream()
                                    .anyMatch(product ->
                                            product.getType()
                                                    .equals(movementDto.getProduct().getType())
                                    );
                            if (repeated) {
                              return Mono.empty();
                            } else {
                              return Mono.just(movementDto.getProduct());
                            }
                          });
                }
              } else if (customer.getType().equals(Customer.CUSTOMER_TYPE_2)) {
                if (movementDto.getProduct().getType().equals(Product.PRODUCT_TYPE_1)
                        || movementDto.getProduct().getType().equals(Product.PRODUCT_TYPE_3)) {
                  return Mono.empty();
                } else {
                  return Mono.just(movementDto.getProduct());
                }
              } else {
                return Mono.empty();
              }
            });
  }

  @Override
  public Mono<Double> getAvailableBalance(String customerId, String productId) {
    return productService.getProductByCustomerAndId(customerId, productId)
            .flatMap(product -> {
              if (product.getId() == null
                      || product.getId().isEmpty()
                      || product.getId().equals("")
              ) {
                return Mono.empty();
              } else {
                return this.listMovementsProduct(product.getId())
                        .collectList()
                        .flatMap(movements -> {
                          double income = movements
                                  .stream()
                                  .filter(mov -> mov.getType()
                                          .equals(Movement.MOVEMENT_TYPE_1)
                                  ).mapToDouble(Movement::getAmount).sum();
                          double expenses = movements
                                  .stream()
                                  .filter(mov -> mov.getType()
                                          .equals(Movement.MOVEMENT_TYPE_2)
                                  ).mapToDouble(Movement::getAmount).sum();
                          double initial = (product.getType().equals(Product.PRODUCT_TYPE_4))
                                  ? product.getCreditLimit() : 0.0;
                          if (product.getType().equals(Product.PRODUCT_TYPE_1)
                                  || product.getType().equals(Product.PRODUCT_TYPE_2)
                                  || product.getType().equals(Product.PRODUCT_TYPE_3)
                                  || product.getType().equals(Product.PRODUCT_TYPE_4)
                          ) {
                            return Mono.just(initial - expenses + income);
                          }
                          return Mono.empty();
                        });
              }
            });
  }

  private Mono<Movement> movementFallback(Throwable e) {
    log.info("MOVEMENT SERVICE IS BREAKER - MONO");
    return Mono.empty();
  }

  private Mono<Long> countMovementFallback(Throwable e) {
    log.info("MOVEMENT SERVICE IS BREAKER - LONG");
    return Mono.empty();
  }

  private Flux<Movement> movementsFallback(Throwable e) {
    log.info("MOVEMENT SERVICE IS BREAKER - FLUX");
    return Flux.empty();
  }

}
