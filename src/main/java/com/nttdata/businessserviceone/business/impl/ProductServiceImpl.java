package com.nttdata.businessserviceone.business.impl;

import com.nttdata.businessserviceone.business.CustomerService;
import com.nttdata.businessserviceone.business.MovementService;
import com.nttdata.businessserviceone.business.ProductService;
import com.nttdata.businessserviceone.model.Customer;
import com.nttdata.businessserviceone.model.Movement;
import com.nttdata.businessserviceone.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

/**
 * Class ProductServiceImpl.
 */
@Service
public class ProductServiceImpl implements ProductService {

  private static final String CIRCUIT_BREAKER_SERVICE_PRODUCT = "cbServiceProduct";

  private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @Value("${api.product.baseUri}")
  private String baseUri;

  @Value("${api.product.savingsAccountUri}")
  private String savingsAccountUri;

  @Value("${api.product.checkingAccountUri}")
  private String checkingAccountUri;

  @Value("${api.product.fixedTermUri}")
  private String fixedTermUri;

  @Value("${api.product.cardUri}")
  private String cardUri;

  @Value("${api.product.loanUri}")
  private String loanUri;

  @Autowired
  WebClient.Builder webClientBuilder;

  @Autowired
  ReactiveResilience4JCircuitBreakerFactory reactiveCircuitBreakerFactory;

  @Autowired
  CustomerService customerService;

  @Override
  public Flux<Product> getProductsByCustomer(String customerId) {
    return webClientBuilder.build()
            .get()
            .uri(baseUri + "/{customerId}", customerId)
            .retrieve()
            .bodyToFlux(Product.class)
            .transform(it -> reactiveCircuitBreakerFactory
                    .create(CIRCUIT_BREAKER_SERVICE_PRODUCT)
                    .run(it, this::productsFallback)
            );
  }

  @Override
  public Mono<Product> getProductByCustomerAndId(String customerId, String productId) {
    return customerService.getCustomerById(customerId)
            .flatMap(customer -> webClientBuilder.build()
                    .get()
                    .uri(baseUri + "/{customerId}/{productId}", customer.getId(), productId)
                    .retrieve()
                    .bodyToMono(Product.class))
            .transform(it -> reactiveCircuitBreakerFactory
                    .create(CIRCUIT_BREAKER_SERVICE_PRODUCT)
                    .run(it, this::productFallback)
            );
  }

  @Override
  public Mono<Product> insertProduct(Product product) {
    String uri;
    product.setId(null);
    product.setStartDate(new Date());
    if (product.getType() == null) {
      return Mono.empty();
    }
    if (product.getType().equals(Product.PRODUCT_TYPE_1)) {
      uri = savingsAccountUri;
    } else if (product.getType().equals(Product.PRODUCT_TYPE_2)) {
      uri = checkingAccountUri;
    } else if (product.getType().equals(Product.PRODUCT_TYPE_3)) {
      uri = fixedTermUri;
    } else if (product.getType().equals(Product.PRODUCT_TYPE_4)) {
      uri = cardUri;
    } else if (product.getType().equals(Product.PRODUCT_TYPE_5)) {
      uri = loanUri;
    } else {
      return Mono.empty();
    }
    return webClientBuilder.build()
            .post()
            .uri(uri).contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(product))
            .retrieve()
            .bodyToMono(Product.class)
            .transform(it -> reactiveCircuitBreakerFactory
                    .create(CIRCUIT_BREAKER_SERVICE_PRODUCT)
                    .run(it, this::productFallback)
            );
  }

  private Flux<Product> productsFallback(Throwable e) {
    log.info("PRODUCT SERVICE IS BREAKER - FLUX");
    return Flux.empty();
  }

  private Mono<Product> productFallback(Throwable e) {
    log.info("PRODUCT SERVICE IS BREAKER - MONO");
    return Mono.empty();
  }

}
