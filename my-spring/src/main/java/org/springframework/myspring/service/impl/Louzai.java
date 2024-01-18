package org.springframework.myspring.service.impl;

import org.springframework.myspring.service.DoEveryDay;
import org.springframework.stereotype.Service;

@Service
public class Louzai implements DoEveryDay {

	public void everyDay() {
		System.out.println("睡觉");
	}
}
