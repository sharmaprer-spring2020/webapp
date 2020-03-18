package com.neu.edu.pojo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

@Configuration
public class MetricsHelper {
	
		@Bean
		public StatsDClient statsDClient(@Value("${metrics.statsd.host:localhost}") String hostname,
										 @Value("${metrics.statsd.port:8125}") int port) {
			return new NonBlockingStatsDClient("csye2020.metrics", hostname, port);
		}
		
		
}
