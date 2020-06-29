package com.ying.entiy;

import lombok.Data;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.stereotype.Component;

import javax.xml.ws.spi.Invoker;

/**
 * @author xieyingheng
 * @title: Codes
 * @projectName spring
 * @description: TODO
 * @date 2020-01-2822:29
 */
@Component
@Data
public class Codes implements BeanNameAware, LoadTimeWeaverAware, InitializingBean, SmartInitializingSingleton, SmartLifecycle {

	private String beanName;

	private String name;

	private int age;

	private String[] list;

	private Worker worker1;

	public void init(){
		System.out.println("invoke codes init()");
	}

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
		this.worker1 = worker;
	}

	public String[] getList() {
		return list;
	}

	public void setList(String[] list) {
		this.list = list;
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
		return worker1;
	}

	public void setWorker(Worker worker) {
		this.worker1 = worker;
	}

	@Override
	public void setBeanName(String name) {
		System.out.println("invoke codes setBeanName()");
		this.beanName = name;
	}

	@Override
	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("invoke afterPropertiesSet()");
	}

	@Override
	public void afterSingletonsInstantiated() {
		System.out.println("invoke afterSingletonsInstantiated");
	}

	@Override
	public void start() {
		System.out.println("invoke start()");
	}

	@Override
	public void stop() {
		System.out.println("Invoker stop()");
	}

	@Override
	public boolean isRunning() {
		return false;
	}
}
