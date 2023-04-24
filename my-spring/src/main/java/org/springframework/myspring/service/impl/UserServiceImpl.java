package org.springframework.myspring.service.impl;

import org.springframework.myspring.service.UserService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author: yeyingheng
 * @date: 2023/4/11 17:04
 */
@Service
public class UserServiceImpl implements UserService {


	@Async
	public void getThreadPool() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.println("getThreadPool");
	}
}
