package com.camunda.training;

import com.camunda.training.services.CreditCardService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ExternalTaskSubscription("chargeCC")
public class ChargeCCHandler implements ExternalTaskHandler {

  @Autowired
  public CreditCardService service;

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {

    String cardNumber = externalTask.getVariable("cardNumber");
    String cvc = externalTask.getVariable("CVC");
    String expiryDate = externalTask.getVariable("expiryDate");
    Double openAmount = externalTask.getVariable("openAmount");

    log.info("Task {} charging {} to {} {} {}", externalTask.getId(), openAmount, cardNumber,cvc, expiryDate);
    service.chargeAmount(cardNumber, cvc, expiryDate, openAmount);

    externalTaskService.complete(externalTask);
  }
}