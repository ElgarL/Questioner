package ca.xshade.questionmanager;

/*
 * 
 */

public abstract class OptionTask implements Runnable {
	protected Option option;
	
	public Option getOption() {
		return option;
	}

	void setOption(Option option) {
		this.option = option;
	}
	
	public abstract void run();
}
