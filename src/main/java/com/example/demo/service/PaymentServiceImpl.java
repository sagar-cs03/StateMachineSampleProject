package com.example.demo.service;

import com.example.demo.config.StateMachineInterceptor;
import com.example.demo.domain.Payment;
import com.example.demo.domain.PaymentEvent;
import com.example.demo.domain.PaymentState;
import com.example.demo.repository.PaymentRepository;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

  public static final String PAYMENT_ID_HEADER = "payment_id";

  @Autowired
  private final StateMachineInterceptor stateMachineInterceptor;

  @Autowired
  private final PaymentRepository paymentRepository;

  @Autowired
  private final StateMachineFactory stateMachineFactory;

  @Transactional
  @Override
  public Payment newPayment(final Payment payment) {
    payment.setPaymentState(PaymentState.NEW);
    paymentRepository.save(payment);
    System.out.println("saved payment");
    return payment;
  }

  @Transactional
  @Override
  public StateMachine<PaymentState, PaymentEvent> preAuth(final Long paymentId) {
    StateMachine<PaymentState, PaymentEvent> sm = buildStateMachineFromPaymentId(paymentId);
    sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);
    return sm;
  }

  @Transactional
  private void sendEvent(final Long paymentId, final StateMachine<PaymentState, PaymentEvent> sm,
                         final PaymentEvent paymentEvent) {
    Message message = MessageBuilder.withPayload(paymentEvent).setHeader(PAYMENT_ID_HEADER,
        paymentId).build();
    sm.sendEvent(message);
  }

  @Transactional
  @Override
  public StateMachine<PaymentState, PaymentEvent> authorizePay(final Long paymentId) {
    StateMachine<PaymentState, PaymentEvent> sm = buildStateMachineFromPaymentId(paymentId);
    sendEvent(paymentId, sm, PaymentEvent.AUTHORIZE);
    return sm;
  }

  @Transactional
  private StateMachine<PaymentState, PaymentEvent> buildStateMachineFromPaymentId(Long paymentId) {
    Payment payment = paymentRepository.getOne(paymentId);
    StateMachine<PaymentState, PaymentEvent> stateMachine =
        stateMachineFactory.getStateMachine(Long.toString(paymentId));

    stateMachine.stop();

    stateMachine.getStateMachineAccessor().doWithAllRegions(sma -> {
      sma.addStateMachineInterceptor(stateMachineInterceptor);
      sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getPaymentState(), null,
          null, null));
    });

    stateMachine.start();
    return stateMachine;

  }

}
