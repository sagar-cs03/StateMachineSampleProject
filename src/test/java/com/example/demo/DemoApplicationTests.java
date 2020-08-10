package com.example.demo;

import com.example.demo.domain.PaymentEvent;
import com.example.demo.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private StateMachineFactory<PaymentState, PaymentEvent> factory;

	@Test
	void testStateMachine() {
		StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine();

		stateMachine.start();

		System.out.println(stateMachine.getState().getId() + " is state");

		stateMachine.sendEvent(PaymentEvent.PRE_AUTHORIZE);

		System.out.println(stateMachine.getState().getId() + " is state");

		stateMachine.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);

		System.out.println(stateMachine.getState().getId() + " is state");


	}

}
