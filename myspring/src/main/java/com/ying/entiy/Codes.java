package com.ying.entiy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author xieyingheng
 * @title: Codes
 * @projectName spring
 * @description: TODO
 * @date 2020-01-2822:29
 */
@Component
public class Codes {

	private Worker worker;

	public Codes() {
		System.out.println("invoke Codes()");
	}

	public Worker getWorker() {
		return worker;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}
}
