package io.output;

public interface ControllerOutput {
	public void setValue(int index, double value);

	public int getNumberOfOutputValues();

	public boolean isAvailable();
}
