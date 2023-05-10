package org.springframework;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.myspring.config.UserConfig;
import org.springframework.myspring.service.UserService;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan(value = "org.springframework.myspring.*", excludeFilters = {})
@EnableAsync
@PropertySource(value = "classpath:application.properties")
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
		UserService userService = context.getBean(UserService.class);
		UserService userServiceImpl = (UserService) context.getBean("userServiceImpl");
		UserConfig userConfig = (UserConfig) context.getBean("userConfig");
		userServiceImpl.getThreadPool();
		System.out.println(userConfig.getName());
		System.out.println(321);

	}
}