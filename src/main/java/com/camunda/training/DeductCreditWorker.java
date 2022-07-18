package com.camunda.training;

import com.camunda.training.services.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ExternalTaskSubscription("deductCredit")
public class DeductCreditWorker implements ExternalTaskHandler {

  @Autowired
  public CustomerService service;

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {

    String customerId = externalTask.getVariable("customerId");
    log.info("customerId: ", customerId);
    Double orderTotal = externalTask.getVariable("orderTotal");
    log.info("orderTotal: ", orderTotal);

    Double openAmount = service.deductCredit(customerId, orderTotal);
    Double customerCredit = service.getCustomerCredit(customerId);

    log.info("orderTotal is: {}", externalTask.getVariable("orderTotal").toString());


    VariableMap result = Variables.createVariables();
    result.put("openAmount", openAmount);
    result.put("customerCredit", customerCredit);

    externalTaskService.complete(externalTask, result);
  }
}
