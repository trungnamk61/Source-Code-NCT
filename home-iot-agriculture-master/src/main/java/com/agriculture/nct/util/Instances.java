package com.agriculture.nct.util;

import java.util.ArrayList;

public class Instances {

	private String name;
	private ArrayList<Double> datas;
	
	Instances(String name) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.datas = new ArrayList<>();
	}
	
	public void addData(double data) {
		this.datas.add(data);
	}
	
	public ArrayList<Double> getDatas() {
		return this.datas;
	}
	
	public String toString() {
		String str = this.name;
		for(int i = 0; i < datas.size(); i ++) {
			str = str + "\n" + datas.get(i);
		}
		return str;
	}
	
	public double get(int index) {
		return datas.get(index);
	}
}
