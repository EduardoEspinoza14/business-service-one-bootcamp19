package com.nttdata.businessserviceone.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Class Customer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

  public static String CUSTOMER_TYPE_1 = "Person";
  public static String CUSTOMER_TYPE_2 = "Company";

  public Customer(String id) {
    this.id = id;
  }

  private String id;
  private String name;
  private String lastName;
  private Date dateBorn;
  private String address;
  private String phone;
  private String type;
  private String ruc;
  private String dni;
  private List<Product> products;

}
