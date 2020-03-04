package com.ying.entiy;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author xieyingheng
 * @title: Codes
 * @projectName spring
 * @description: TODO
 * @date 2020-01-2822:29
 */
@Component
@Data
public class Codes {

	private String name;

	private int age;

	private String[] list;

	private Worker worker;

	public Codes() {
		System.out.println("invoke Codes()");
	}

	public Codes(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public Codes(String name, int age, String[] list) {
		this.name = name;
		this.age = age;
		this.list = list;
	}

	public Codes(String name, int age, Worker worker) {
		this.name = name;
		this.age = age;
		this.worker = worker;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Worker getWorker() {
		return worker;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}
}
