package com.nttdata.businessserviceone.business.impl;

import com.nttdata.businessserviceone.business.CustomerService;
import com.nttdata.businessserviceone.business.EmployeeService;
import com.nttdata.businessserviceone.model.Customer;
import com.nttdata.businessserviceone.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Class EmployeeServiceImpl.
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

  @Value("${api.customer.employeeUri}")
  private String employeeUri;

  @Value("${api.customer.signerUri}")
  private String signerUri;

  @Value("${api.customer.holderUri}")
  private String holderUri;

  @Autowired
  WebClient.Builder webClientBuilder;

  @Autowired
  CustomerService customerService;

  @Override
  public Flux<Employee> listEmployees(String customerId) {
    return customerService.getCustomerById(customerId)
            .defaultIfEmpty(new Customer(""))
            .flatMapMany(customer -> {
              if (customer.getId() == null
                      || customer.getId().isEmpty()
                      || customer.getId().equals("")
              ) {
                return Flux.empty();
              } else {
                return webClientBuilder.build()
                        .get()
                        .uri(employeeUri + "/{customerId}", customer.getId())
                        .retrieve()
                        .bodyToFlux(Employee.class);
              }
            });
  }

  @Override
  public Mono<Employee> getEmployee(String customerId, String employeeId) {
    return webClientBuilder.build()
            .get()
            .uri(employeeUri + "/{customerId}/{employeeId}", customerId, employeeId)
            .retrieve()
            .bodyToMono(Employee.class);
  }

  @Override
  public Mono<Employee> registerSigner(String customerId, Employee employee) {
    return customerService.getCustomerById(customerId)
            .defaultIfEmpty(new Customer(""))
            .flatMap(customer -> webClientBuilder.build()
                    .post()
                    .uri(signerUri + "/{customerId}", customer.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(employee))
                    .retrieve()
                    .bodyToMono(Employee.class)
            );
  }

  @Override
  public Mono<Employee> registerHolder(String customerId, Employee employee) {
    return customerService.getCustomerById(customerId)
            .defaultIfEmpty(new Customer(""))
            .flatMap(customer -> webClientBuilder.build()
                    .post()
                    .uri(holderUri + "/{customerId}", customer.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(employee))
                    .retrieve()
                    .bodyToMono(Employee.class)
            );
  }

  @Override
  public Mono<Employee> updateEmployee(String customerId, Employee employee) {
    return customerService.getCustomerById(customerId)
            .defaultIfEmpty(new Customer(""))
            .flatMap(customer -> this.getEmployee(customer.getId(), employee.getId()))
            .doOnNext(emp -> employee.setCompany(new Customer(customerId)))
            .flatMap(emp -> {
              String uri;
              if (emp.getType().equals(Employee.EMPLOYEE_TYPE_1)) {
                uri = holderUri;
              } else if (emp.getType().equals(Employee.EMPLOYEE_TYPE_2)) {
                uri = signerUri;
              } else {
                return Mono.empty();
              }
              return webClientBuilder.build()
                      .post()
                      .uri(uri + "/{customerId}" + "/update/{id}", customerId, emp.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(BodyInserters.fromValue(employee))
                      .retrieve()
                      .bodyToMono(Employee.class);
            });
  }

  @Override
  public Mono<Void> deregisterEmployee(String customerId, String employeeId) {
    return customerService.getCustomerById(customerId)
            .defaultIfEmpty(new Customer(""))
            .flatMap(customer -> this.getEmployee(customer.getId(), employeeId))
            .flatMap(emp -> {
              String uri;
              //VALIDA SI SE QUEDA SIN TITULARES - HOLDERS
              if (emp.getType().equals(Employee.EMPLOYEE_TYPE_1)) {
                uri = holderUri;
              } else if (emp.getType().equals(Employee.EMPLOYEE_TYPE_2)) {
                uri = signerUri;
              } else {
                return Mono.empty();
              }
              return webClientBuilder.build()
                      .post()
                      .uri(uri + "/{customerId}" + "/delete/{id}", customerId, emp.getId())
                      .retrieve()
                      .bodyToMono(Void.class);
            });
  }
}
