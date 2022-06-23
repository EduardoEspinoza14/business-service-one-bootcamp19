package com.nttdata.businessserviceone.business;

import com.nttdata.businessserviceone.model.Customer;
import reactor.core.publisher.Mono;

/**
 * Interface CustomerService.
 */
public interface CustomerService {

  //PARA OBTENER UN CLIENTE EN ESPECIFICO POR ID
  Mono<Customer> getCustomerById(String id);

  //PARA REGISTRAR UN CLIENTE INVOCANDO AL API
  Mono<Customer> insertCustomer(Customer customer);

  //PARA VALIDAR SI EL CLIENTE EXISTE O EN CASO CONTRARIO CREARLO
  Mono<Customer> checkCustomerExistsElseCreate(Customer customer);

}
