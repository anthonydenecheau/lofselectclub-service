package com.scc.lofselectclub.template.parent;

public class ParentFrequency {

	int time;
	int qtity;

	public int getTime() { return time; }
	public void setTime(int time) { this.time = time; }
	
	public int getQtity() { return qtity; }
	public void setQtity(int qtity) { this.qtity = qtity; }

	public ParentFrequency withTime(int time){ this.setTime(time); return this; }
	public ParentFrequency withQtity(int qtity){ this.setQtity(qtity); return this; }
	
}
