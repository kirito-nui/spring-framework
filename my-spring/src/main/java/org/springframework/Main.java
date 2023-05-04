package org.springframework;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.myspring.service.UserService;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan(value = "org.springframework.myspring.*", excludeFilters = {})
@EnableAsync
@PropertySource(value = "classpath:application.properties")
@Data
public class Main {

	@Value("${name}")
	String name;

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
		userServiceImpl.getThreadPool();
		Main main = new Main();
		System.out.println(main.name);
		System.out.println(321);

	}
}