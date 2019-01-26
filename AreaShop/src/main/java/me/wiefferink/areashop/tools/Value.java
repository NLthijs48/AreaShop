package me.wiefferink.areashop.tools;

public class Value<T> {

	private T content;

	/**
	 * Create a container with a default content.
	 * @param content The content to set
	 */
	public Value(T content) {
		this.content = content;
	}

	/**
	 * Get the stored content.
	 * @return The stored content
	 */
	public T get() {
		return content;
	}

	/**
	 * Set the content.
	 * @param value The new content
	 */
	public void set(T value) {
		this.content = value;
	}
}
