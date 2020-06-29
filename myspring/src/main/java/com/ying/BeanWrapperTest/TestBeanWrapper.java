package com.ying.BeanWrapperTest;

import com.ying.entiy.Company;
import org.springframework.beans.BeanWrapperImpl;

public class TestBeanWrapper {


	public static void main(String[] args) {
		BeanWrapperImpl rootBeanWrapper = new BeanWrapperImpl(Company.class);
		rootBeanWrapper.setAutoGrowNestedPaths(true);

		rootBeanWrapper.setPropertyValue("total", "222");
		rootBeanWrapper.setPropertyValue("name", "company");
		rootBeanWrapper.setPropertyValue("department.name", "company");
//		rootBeanWrapper.setPropertyValue("director.info.name", "info...");
		rootBeanWrapper.setPropertyValue("employees[0].attrs['a']", "a");
	}
}
