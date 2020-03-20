package com.ying.entiy;

import org.springframework.stereotype.Component;

/**
 * @author xieyingheng
 * @title: Worker
 * @projectName spring
 * @description: TODO
 * @date 2020-02-0709:43
 */
@Component
public class Worker {

	private Codes codes;

	public Worker(Codes codes) {
		this.codes = codes;
	}

	public Codes getCodes() {
		return codes;
	}

	public void setCodes(Codes codes) {
		this.codes = codes;
	}

	public Worker() {
		System.out.println("init Worker()");
	}
}
