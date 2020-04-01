package com.neu.edu.thread.runner;

import java.util.concurrent.Executor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.neu.edu.thread.ProcessQueueMessage;

@Component
public class ThreadRunner implements CommandLineRunner {

	private final ProcessQueueMessage pqm;
	
	public ThreadRunner(ProcessQueueMessage pqm) {
	    this.pqm = pqm;
	  }


	@Override
	public void run(String... args) throws Exception {
		pqm.processMessages();
		
	}

}
