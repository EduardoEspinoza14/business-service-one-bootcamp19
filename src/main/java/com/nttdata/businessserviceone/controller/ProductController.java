package com.nttdata.businessserviceone.controller;

import com.nttdata.businessserviceone.business.MovementService;
import com.nttdata.businessserviceone.business.ProductService;
import com.nttdata.businessserviceone.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Class ProductController.
 */
@RestController
@RequestMapping("/products/{customerId}")
@CrossOrigin(origins = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE
})
public class ProductController {

  private final Logger log = LoggerFactory.getLogger(EmployeeController.class);

  @Autowired
  ProductService productService;

  @Autowired
  MovementService movementService;

  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public Flux<Product> getProductsByCustomer(@PathVariable String customerId) {
    return productService.getProductsByCustomer(customerId);
  }

  @GetMapping(value = "/available-balance/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Double> availableBalance(@PathVariable String customerId,
                                       @PathVariable String productId) {
    return movementService.getAvailableBalance(customerId, productId);
  }

}
