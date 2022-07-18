package com.camunda.training;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.community.rest.client.api.MessageApi;
import org.camunda.community.rest.client.dto.CorrelationMessageDto;
import org.camunda.community.rest.client.dto.MessageCorrelationResultWithVariableDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiClient;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ExternalTaskSubscription("paymentCompletion")
public class PaymentCompletionHandler implements ExternalTaskHandler {

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
    log.info("handle topic {} for task id {}", externalTask.getTopicName(), externalTask.getId());

    ApiClient client = new ApiClient();

    Map<String, VariableValueDto> correlationKey = new HashMap<>();
    correlationKey.put("paymentProcessInstanceId", new VariableValueDto().value(externalTask.getProcessInstanceId()));

    try {
      List<MessageCorrelationResultWithVariableDto> correlationResult = new MessageApi(client)
          .deliverMessage(new CorrelationMessageDto().messageName("paymentMessage")
              .correlationKeys(correlationKey).resultEnabled(true));
      log.info("correlation result: {}", correlationResult);

      externalTaskService.complete(externalTask);
    } catch (ApiException e) {

      int retries = 0;
      if (externalTask.getRetries() != null) retries = externalTask.getRetries()-1;
      externalTaskService.handleFailure(externalTask, e.getMessage(), e.getMessage(),  retries, 1000);
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}