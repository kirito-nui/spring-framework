package org.springframework.myspring.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.myspring.service.UserService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author: yeyingheng
 * @date: 2023/4/11 17:04
 */
@Service
public class UserServiceImpl implements UserService, EnvironmentAware {


	@Value("${name}")
	private String name;
	private Environment environment;

	@Async
	public void getThreadPool() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.println("getThreadPool");
	}

	public String getName() {
		return environment.getProperty("name");
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
