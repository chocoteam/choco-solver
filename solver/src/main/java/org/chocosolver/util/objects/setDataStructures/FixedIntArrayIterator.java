package org.chocosolver.util.objects.setDataStructures;

public class FixedIntArrayIterator implements ISetIterator {

	private final int[] array;
	private int index = 0;

	public FixedIntArrayIterator(int[] array) {
		this.array = array;
	}

	@Override
	public void reset() {
		index = 0;
	}

	@Override
	public int nextInt() {
		return array[index++];
	}

	@Override
	public boolean hasNext() {
		return index < array.length;
	}
}
