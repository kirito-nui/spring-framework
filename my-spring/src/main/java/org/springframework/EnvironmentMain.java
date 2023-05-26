package org.springframework;

import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import java.io.IOException;
import java.util.Properties;

/**
 * @author: yeyingheng
 * @date: 2023/5/22 11:56
 */
public class EnvironmentMain {


	public static void main(String[] args) throws IOException {
//		StandardEnvironment environment = new StandardEnvironment();
//		System.out.println(environment.getProperty("name"));//null
//		Properties properties = PropertiesLoaderUtils
//				.loadProperties(new ClassPathResource("application.properties"));
//		PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}", ":",
//				true);
//		String result = placeholderHelper.replacePlaceholders("${name}", properties);
//		String name = properties.getProperty("name");
//		System.out.println(result);//lisi23
		String location = "classpath*:*.properties";
		PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = patternResolver.getResources(location);
		DefaultPropertySourceFactory factory = new DefaultPropertySourceFactory();
		MutablePropertySources propertySources = new MutablePropertySources();
		PropertySource<?> propertySource;

		for (Resource resource : resources) {
			propertySource = factory.createPropertySource(null, new EncodedResource(resource, (String) null));
			propertySources.addLast(propertySource);
		}
		System.out.println();
	}
}
