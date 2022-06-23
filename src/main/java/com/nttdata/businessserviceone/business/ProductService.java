package com.nttdata.businessserviceone.business;

import com.nttdata.businessserviceone.model.Movement;
import com.nttdata.businessserviceone.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface ProductService.
 */
public interface ProductService {

  //PARA OBTENER TODOS LOS PRODUCTOS POR ID CLIENTE
  Flux<Product> getProductsByCustomer(String customerId);

  //PARA OBTNER EL PRODUCTO POR ID CLIENTE Y ID PRODUCTO,
  // ESTO VALIDA LA PERTENENCIA Y EXISTENCIA
  Mono<Product> getProductByCustomerAndId(String customerId, String productId);

  //PARA INSERTAR UN PRODUCTO INVOCANDO A LA API
  Mono<Product> insertProduct(Product product);

}
