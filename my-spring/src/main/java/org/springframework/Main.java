package org.springframework;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.myspring.config.UserConfig;
import org.springframework.myspring.service.UserService;
import org.springframework.myspring.service.impl.Louzai;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan(value = "org.springframework.myspring.*", excludeFilters = {})
@EnableAsync
@PropertySource(value = "classpath:application.properties")
@EnableAspectJAutoProxy
public class Main{


	private Environment environment;
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
		String name = userServiceImpl.getName();
		System.out.println("threadID：" + Thread.currentThread().getId());
		userServiceImpl.getThreadPool();
		System.out.println(userConfig.getName());
		userServiceImpl.everyDay();
		System.out.println(name);
		System.out.println(321);

	}

}