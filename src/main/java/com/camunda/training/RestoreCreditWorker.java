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
@ExternalTaskSubscription("restoreCredit")
public class RestoreCreditWorker implements ExternalTaskHandler {

  @Autowired
  public CustomerService service;

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {

    log.info("handle topic {} for task id {}", externalTask.getTopicName(), externalTask.getId());

    externalTaskService.complete(externalTask);
  }
}
