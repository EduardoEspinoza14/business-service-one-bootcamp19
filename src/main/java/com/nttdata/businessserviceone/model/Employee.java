package com.nttdata.businessserviceone.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class Employee.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

  public static String EMPLOYEE_TYPE_1 = "Holder";
  public static String EMPLOYEE_TYPE_2 = "Signer";

  public Employee(String id) {
    this.id = id;
  }

  private String id;
  private String name;
  private String lastName;
  private String type;
  private Customer company;


}
