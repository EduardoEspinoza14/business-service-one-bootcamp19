package com.nttdata.businessserviceone.business;

import com.nttdata.businessserviceone.model.Employee;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface EmployeeService.
 */
public interface EmployeeService {

  //PARA OBTENER LOS EMPLEADOS O TRABAJADORES DE UN CLIENTE POR ID
  Flux<Employee> listEmployees(String customerId);

  //PARA OBTENER UN EMPLEADO POR SU ID
  Mono<Employee> getEmployee(String customerId, String employeeId);

  //PARA REGISTRAR UN FIRMANTE POR ID CLIENTE
  Mono<Employee> registerSigner(String customerId, Employee employee);

  //PARA REGISTRAR UN TITULAR POR ID CLIENTE
  Mono<Employee> registerHolder(String customerId, Employee employee);

  //PARA ACTUALIZAR UN EMPLEADO CUALQUIERA POR ID
  Mono<Employee> updateEmployee(String customerId, Employee employee);

  //PARA ELIMINAR UN EMPLEADO CUALQUIER POR ID
  Mono<Void> deregisterEmployee(String customerId, String employeeId);

}
