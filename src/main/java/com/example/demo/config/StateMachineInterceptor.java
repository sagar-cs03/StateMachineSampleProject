package com.example.demo.config;

import com.example.demo.domain.Payment;
import com.example.demo.domain.PaymentEvent;
import com.example.demo.domain.PaymentState;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.PaymentServiceImpl;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StateMachineInterceptor
    extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

  @Autowired
  private PaymentRepository paymentRepository;

  @Override
  public void preStateChange(final State<PaymentState, PaymentEvent> state,
                             final Message<PaymentEvent> message,
                             final Transition<PaymentState, PaymentEvent> transition,
                             final StateMachine<PaymentState, PaymentEvent> stateMachine) {
    Optional.ofNullable(message).ifPresent(msg -> {
      Optional.ofNullable(
          Long.class.cast(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L)))
          .ifPresent(paymentId -> {
            Payment payment = paymentRepository.getOne(paymentId);
            payment.setPaymentState(state.getId());
            paymentRepository.save(payment);
          });
    });
  }
}
