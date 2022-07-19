package com.camunda.training;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.camunda.bpm.extension.process_test_coverage.junit5.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

//@ExtendWith(ProcessEngineExtension.class)  // StandaloneInMemProcessEngineConfiguration
@ExtendWith(ProcessEngineCoverageExtension.class)
public class PaymentUnitTest {

  @Test
  @Deployment(resources = {"payment-technical.bpmn"})
  void testBPMNErroroOnChargeCC() {
    ProcessInstance processInstance = runtimeService().createProcessInstanceByKey("PaymentProcess")
        .startBeforeActivity("ChargeCreditCardTask").execute();
    assertThat(processInstance).isWaitingAt("ChargeCreditCardTask");
    fetchAndLock("chargeCC", "junit-test-worker", 1);
    externalTaskService().handleBpmnError(externalTask().getId(), "junit-test-worker", "creditCardChargeError");
   assertThat(processInstance).isWaitingAt("FixCardInformationTask");

  }

  @Test
  @Deployment(resources = {"payment-technical.bpmn"})
  public void chargeCard_test() {

    ProcessInstance pi = runtimeService().startProcessInstanceByKey("PaymentProcess",
        withVariables("orderTotal", 220, "customerCredit", 100));

    BpmnAwareTests.assertThat(pi)
        .isWaitingAt("DeductExistingCreditTask")
        .variables().containsEntry("orderTotal", 220)
        .containsEntry("customerCredit", 100);
    assertThat(pi).externalTask().hasTopicName("deductCredit");
    complete(externalTask());

    assertThat(pi).externalTask().hasTopicName("chargeCC");
    complete(externalTask());

    assertThat(pi).externalTask().hasTopicName("paymentCompletion");
    complete(externalTask());

    BpmnAwareTests.assertThat(pi).isEnded();
  }

  @Test
  @Deployment(resources = {"payment-technical.bpmn"})
  public void sufficentCredit_test() {

    ProcessInstance pi = runtimeService().startProcessInstanceByKey("PaymentProcess",
        withVariables("orderTotal", 220, "customerCredit", 300));

    BpmnAwareTests.assertThat(pi)
        .isWaitingAt("DeductExistingCreditTask")
        .variables().containsEntry("orderTotal", 220)
        .containsEntry("customerCredit", 300);
    assertThat(pi).externalTask().hasTopicName("deductCredit");
    complete(externalTask());



    BpmnAwareTests.assertThat(pi)
        .isEnded()
        .hasNotPassed("ChargeCreditCardTask");
  }

}
