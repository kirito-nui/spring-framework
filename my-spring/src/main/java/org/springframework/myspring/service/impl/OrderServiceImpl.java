package org.springframework.myspring.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.myspring.service.OrderService;
import org.springframework.myspring.service.UserService;
import org.springframework.stereotype.Component;

/**
 * @author: yeyingheng
 * @date: 2023/4/17 20:54
 */
@Component
public class OrderServiceImpl implements OrderService {

	@Autowired
	private UserService userService;

}
