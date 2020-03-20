package com.ying.BeanFactoryPostProcessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;

public class BeanFactoryPostProcessor_1 implements BeanFactoryPostProcessor, Ordered {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("invoke postProcessBeanFactory()");
		BeanDefinition codes = beanFactory.getBeanDefinition("codes");
		MutablePropertyValues propertyValues = codes.getPropertyValues();
//		propertyValues.addPropertyValue("name", "修改后的");
	}

	@Override
	public int getOrder() {
		return 1;
	}
}
