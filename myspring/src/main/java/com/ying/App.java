package com.ying;

import com.ying.config.AppConfig;
import com.ying.entiy.Codes;
import com.ying.factoryPostProcessor.TestBeanFactoryPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author xieyingheng
 * @title: App
 * @projectName spring
 * @description: TODO
 * @date 2020-01-2822:39
 */
public class App {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext applicationContext
				= new AnnotationConfigApplicationContext(AppConfig.class);
		System.out.println(applicationContext.getBean(Codes.class));
	}
}
