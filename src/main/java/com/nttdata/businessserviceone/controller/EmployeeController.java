package com.nttdata.businessserviceone.controller;

import com.nttdata.businessserviceone.business.EmployeeService;
import com.nttdata.businessserviceone.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Class EmployeeController.
 */
@RestController
@RequestMapping("/employees/{customerId}")
@CrossOrigin(origins = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE
})
public class EmployeeController {

  private final Logger log = LoggerFactory.getLogger(EmployeeController.class);

  @Autowired
  EmployeeService service;

  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public Flux<Employee> listEmployee(@PathVariable String customerId) {
    return service.listEmployees(customerId);
  }

  @PostMapping(value = "/register-signer", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Employee> registerSigner(@PathVariable String customerId,
                                       @RequestBody Employee employee) {
    return service.registerSigner(customerId, employee);
  }

  @PostMapping(value = "/register-holder", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Employee> registerHolder(@PathVariable String customerId,
                                       @RequestBody Employee employee) {
    return service.registerHolder(customerId, employee);
  }

  @PostMapping(value = "/update/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Employee> update(@PathVariable String customerId, @RequestBody Employee employee) {
    return service.updateEmployee(customerId, employee);
  }

  @PostMapping(value = "/deregister/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Void> deregister(@PathVariable String customerId, @PathVariable String id) {
    return service.deregisterEmployee(customerId, id);
  }

}
