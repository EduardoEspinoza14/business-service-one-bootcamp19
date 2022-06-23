package com.nttdata.businessserviceone.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class MovementDto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movement {

  public static String MOVEMENT_TYPE_1 = "Income";
  public static String MOVEMENT_TYPE_2 = "Expenses";

  public static String MOVEMENT_CONCEPT_1 = "Account Opening";
  public static String MOVEMENT_CONCEPT_2 = "Withdrawal Account";
  public static String MOVEMENT_CONCEPT_3 = "Deposit Account";
  public static String MOVEMENT_CONCEPT_4 = "Payment Credit";
  public static String MOVEMENT_CONCEPT_5 = "Spent Credit";

  public Movement(String id){
    this.id = id;
  }

  public Movement(Customer customer, Product product){
    this.customer = customer;
    this.product = product;
  }

  private String id;
  private String concept;
  private Date date;
  private Double amount;
  private String type;
  private String customerId;
  private String productId;
  private Customer customer;
  private Product product;

}
