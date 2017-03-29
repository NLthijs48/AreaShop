package me.wiefferink.areashop.tools;

public class Value<T> {

	private T value;

	/**
	 * Create a container with a default value.
	 * @param value The value to set
	 */
	public Value(T value) {
		this.value = value;
	}

	/**
	 * Get the stored value.
	 * @return The stored value
	 */
	public T get() {
		return value;
	}

	/**
	 * Set the value.
	 * @param value The new value
	 */
	public void set(T value) {
		this.value = value;
	}
}
