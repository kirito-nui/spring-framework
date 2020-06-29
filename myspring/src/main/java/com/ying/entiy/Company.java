package com.ying.entiy;

import lombok.Data;

import java.util.List;
import java.util.Map;

public class Company {
	private String name;
	private int total;

	private Department department;
	private Employee director;
	private Employee[] employees;
	private Map<Department, List<Employee>> departmentEmployees;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Employee getDirector() {
		return director;
	}

	public void setDirector(Employee director) {
		this.director = director;
	}

	public Employee[] getEmployees() {
		return employees;
	}

	public void setEmployees(Employee[] employees) {
		this.employees = employees;
	}

	public Map<Department, List<Employee>> getDepartmentEmployees() {
		return departmentEmployees;
	}

	public void setDepartmentEmployees(Map<Department, List<Employee>> departmentEmployees) {
		this.departmentEmployees = departmentEmployees;
	}

	public static class Employee {
		private String name;
		private double salary;
		private Map<String, String> attrs;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public double getSalary() {
			return salary;
		}

		public void setSalary(double salary) {
			this.salary = salary;
		}

		public Map<String, String> getAttrs() {
			return attrs;
		}

		public void setAttrs(Map<String, String> attrs) {
			this.attrs = attrs;
		}
	}

	public static class Department {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
