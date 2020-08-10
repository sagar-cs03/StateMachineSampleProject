package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.domain.Payment;
import com.example.demo.domain.PaymentEvent;
import com.example.demo.domain.PaymentState;
import com.example.demo.repository.PaymentRepository;
import javax.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

@SpringBootTest
class PaymentServiceImplTest {

  @Autowired
  private PaymentService paymentService;

  @Autowired
  private PaymentRepository paymentRepository;

  @Transactional
  @Test
  public void testPreAuthorize() {
    Payment payment = paymentService.newPayment(new Payment());

    System.out.println("SHOULD BE NEW");
    System.out.println(payment.getPaymentState());

    StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(payment.getId());
    System.out.println("SHOULD BE PRE_AUTH");

    payment = paymentRepository.getOne(payment.getId());
    System.out.println(sm.getState().getId() + ": db :" + payment.getPaymentState());

    sm  = paymentService.authorizePay(payment.getId());
    payment = paymentRepository.getOne(payment.getId());

    System.out.println("SHOULD BE AUTH!");

    System.out.println(sm.getState().getId() + ": db :" + payment.getPaymentState());



  }
}