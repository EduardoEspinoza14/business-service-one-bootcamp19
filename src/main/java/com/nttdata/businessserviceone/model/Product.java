package com.nttdata.businessserviceone.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Class Product.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

  public static String PRODUCT_TYPE_1 = "Savings Account";
  public static String PRODUCT_TYPE_2 = "Checking Account";
  public static String PRODUCT_TYPE_3 = "Fixed Term";
  public static String PRODUCT_TYPE_4 = "Card";
  public static String PRODUCT_TYPE_5 = "Loan";

  public Product(String id) {
    this.id = id;
  }

  private String id;
  private Date startDate;
  private String number;
  private String type;
  private Double creditLimit;
  private Date expirationDate;
  private String securityCode;
  private Double commissionAmount;
  private Integer singleDayMovement;
  private Double creditAmount;
  private Integer paymentDay;
  private Integer maxMovementLimit;
  private String customerId;

}
