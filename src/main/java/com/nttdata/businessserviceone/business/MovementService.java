package com.nttdata.businessserviceone.business;

import com.nttdata.businessserviceone.model.Movement;
import com.nttdata.businessserviceone.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface MovementService.
 */
public interface MovementService {

  Mono<Movement> insertMovementDefault(Movement movement);

  //PARA LISTAR LOS MOVIMIENTOS DE UN PRODUCTO POR ID DE PRODUCTO Y CLIENTE
  Flux<Movement> listMovementsCustomerProduct(String customerId, String productId);

  Flux<Movement> listMovementsProduct(String productId);

  //PARA REGISTRAR EL PRIMER MOVIMIENTO DE UN PRODUCTO,
  // ESTE MOVIMIENTO DE APERTURA TAMBIEN REGISTRA EL PRODUCTO Y EL CLIENTE DE SER NECESARIO
  Mono<Movement> accountOpening(Movement movementDto);

  //PARA REGISTRAR UN MOVIMIENTO DE RETIRO SOBRE LOS PRODUCTOS DE TIPO CUENTAS
  Mono<Movement> registerWithdrawal(Movement movementDto);

  //PARA REGISTRAR UN MOVIMIENTO DE DEPOSITO SOBRE LOS PRODUCTOS DE TIPO CUENTAS
  Mono<Movement> registerDeposit(Movement movementDto);

  //PARA REGISTRAR UN PAGO SOBRE UN PRODUCTO DE TIPO CREDITO
  Mono<Movement> registerPayment(Movement movementDto);

  //PARA REGISTRAR UN GASTO O COSUMO SOBRE UN PRODUCTO DE TIPO CREDITO
  Mono<Movement> registerSpent(Movement movementDto);

  //PARA VALIDAR SI EL CLIENTE PUEDE ACCEDER A UN PRODUCTO EN ESPECIFICO,
  // SIGUIENDO LAS REGLAS DEL NEGOCIO
  Mono<Product> validateCustomerCanProduct(Movement movementDto);

  //PARA OBTENER EL SALDO EN CUENTA SI ES EL PRODUCTO DE TIPO CUENTA,
  // Y EL SALDO DE CREDITO DISPONIBLE, SI ES PRODUCTO DEL TIPO CREDITO
  Mono<Double> getAvailableBalance(String customerId, String productId);

}
