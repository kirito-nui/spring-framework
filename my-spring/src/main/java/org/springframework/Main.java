package org.springframework;

import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.myspring.bean.User;
import org.springframework.myspring.service.UserService;
import org.springframework.myspring.service.impl.OrderServiceImpl;
import org.springframework.myspring.service.impl.UserServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan(value = "org.springframework.myspring.*", excludeFilters = {})
@EnableAsync
public class Main {
	public static void main(String[] args) {

		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(Main.class);
//		AnnotationConfigApplicationContext context =
//				new AnnotationConfigApplicationContext("org.springframework.myspring");
//		GenericApplicationContext context = new GenericApplicationContext();
//		AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(context);
//		reader.registerBean(UserServiceImpl.class);
//		reader.registerBean(OrderServiceImpl.class);
		for (String beanDefinitionName : context.getBeanDefinitionNames()) {
			System.out.println(beanDefinitionName);
		}
		UserService userServiceImpl = (UserService) context.getBean("userServiceImpl");
		userServiceImpl.getThreadPool();
		System.out.println(321);
	}
}