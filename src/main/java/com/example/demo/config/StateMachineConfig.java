package com.example.demo.config;

import com.example.demo.domain.PaymentEvent;
import com.example.demo.domain.PaymentState;
import com.example.demo.service.PaymentServiceImpl;
import java.util.EnumSet;
import java.util.Random;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.AbstractStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends AbstractStateMachineConfigurerAdapter<PaymentState,
    PaymentEvent> {

  @Override
  public void configure(final StateMachineStateConfigurer<PaymentState, PaymentEvent> states)
      throws Exception {
    states
        .withStates()
        .initial(PaymentState.NEW)
        .states(EnumSet.allOf(PaymentState.class))
        .end(PaymentState.AUTH)
        .end(PaymentState.AUTH_ERROR)
        .end(PaymentState.PRE_AUTH_ERROR);
  }

  @Override
  public void configure(
      final StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions)
      throws Exception {
    transitions
        //action runs before changing to state
        .withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(
        PaymentEvent.PRE_AUTHORIZE)
        .action(preAuthorizeAction())
        .and()
        .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(
        PaymentEvent.PRE_AUTH_APPROVED)
        .and()
        .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(
        PaymentEvent.PRE_AUTH_DECLINE)
        .and()
        .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(
        PaymentEvent.AUTHORIZE)
        .action(authorizeAction())
        .and()
        .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(
        PaymentEvent.AUTHORIZE_APPROVED)
        .and()
        .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(
        PaymentEvent.AUTHORIZE_DECLINED);
  }

  private Action<PaymentState, PaymentEvent> authorizeAction() {
    return context -> {
      System.out.println("authorize action triggered!");
      if (new Random().nextInt(10) < 9) {
        System.out.println("authorized!");
        context.getStateMachine().sendEvent(MessageBuilder.withPayload(
            PaymentEvent.AUTHORIZE_APPROVED).setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
            context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER)).build());
      } else {
        System.out.println("authorize error");
        context.getStateMachine().sendEvent(MessageBuilder.withPayload(
            PaymentEvent.AUTHORIZE_DECLINED).setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
            context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER)).build());
      }
    };
  }

  private Action<PaymentState, PaymentEvent> preAuthorizeAction() {
    return context -> {
      System.out.println("preauthorize action triggered!");
      if (new Random().nextInt(10) < 9) {
        System.out.println("preauthorize alright!");
        context.getStateMachine().sendEvent(MessageBuilder.withPayload(
            PaymentEvent.PRE_AUTH_APPROVED).setHeader(
            PaymentServiceImpl.PAYMENT_ID_HEADER,
            context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER)).build());

      } else {
        System.out.println("preauthorize error");
        context.getStateMachine().sendEvent(MessageBuilder.withPayload(
            PaymentEvent.PRE_AUTH_DECLINE).setHeader(
            PaymentServiceImpl.PAYMENT_ID_HEADER,
            context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER)).build());
      }
    };
  }

  @Override
  public void configure(
      final StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config)
      throws Exception {
    StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter =
        new StateMachineListenerAdapter<>() {
          @Override
          public void stateChanged(final State<PaymentState, PaymentEvent> from,
                                   final State<PaymentState, PaymentEvent> to) {
            System.out.println(from.toString() + "changed to " + to.toString());
          }
        };
    config.withConfiguration().listener(adapter);
  }

}
