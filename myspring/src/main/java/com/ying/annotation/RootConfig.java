package com.ying.annotation;

import com.ying.config.AppConfig;
import com.ying.entiy.Codes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Controller;

@ComponentScan(value = "com.fsx", excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Controller.class}),
		//排除掉web容器的配置文件，否则会重复扫描
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {AppConfig.class}),
})
@Configuration
public class RootConfig {

	@Bean
	public Codes parent() {
		return new Codes("555", 555);
	}


	public static void main(String[] args) {
		// 备注：此处只能用RootConfig,而不能AppConfig(启动报错)，因为它要web容器支持，比如Tomcat
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(RootConfig.class);
		System.out.println(applicationContext.getBean(Codes.class).getAge()); //com.fsx.bean.Parent@639c2c1d
	}
}



