package solver.constraints.propagators.nary.scheduling.dataStructures;

import choco.kernel.memory.IStateInt;
import choco.kernel.memory.trailing.EnvironmentTrailing;

public class Standby {

	private EnvironmentTrailing env;
	private int[] lid;
	private int[] t;
	private IStateInt top; // the index of the top element. (i.e. the element
							// with the smallest lid.)
	private IStateInt size; // number of elements in the structure.

	private boolean needToBeSorted;

	public Standby(EnvironmentTrailing _env, int _n) {
		this.env = _env;
		this.lid = new int[_n];
		this.t = new int[_n];
		this.top = this.env.makeInt(0);
		this.size = this.env.makeInt(0);
		this.needToBeSorted = false;
	}

	public void add(int _lid, int _task) {
		if ((size.get() - 1 >= 0) && (this.lid[size.get() - 1] != _lid))
			this.needToBeSorted = true;
		this.lid[size.get()] = _lid;
		this.t[size.get()] = _task;
		size.add(1);
	}

	public int topLid() {
		return this.lid[top.get()];
	}

	public int topTask() {
		return this.t[top.get()];
	}

	public void removeTop() {
		top.add(1); // top element is now into the next cell.
		size.add(-1);
	}

	public boolean isEmpty() {
		return (size.get() == 0);
	}

	public void sort() {
		if (needToBeSorted) {
			iterativeQuicksort6();
		}
	}

	/**
	 * Tri rapide itératif
	 */
	public void iterativeQuicksort6() {
		int[] range = new int[size.get() + 1]; // if (range[i]<0) then skip[i] =
												// |range[i]|
		range[0] = size.get() - 1;
		int i, j, sortedCount = 0;
		while (sortedCount < size.get()) {
			for (i = 0; i < size.get(); i++)
				if (range[i] >= i) {
					j = range[i];
					if (j - i < 7) {
						// selectionsort the elements from a[i] to a[j]
						// inclusive
						// and set all their ranges to -((j+1)-k)
						for (int m = i; m <= j; m++) {
							for (int n = m; n > i
									&& this.lid[n - 1] > this.lid[n]; n--)
								swap(n, n - 1);
							range[m] = -((j + 1) - m);
							sortedCount++;
						}
						i = j;
					} else {
						for (; i <= j; i++) {
							int p = partition(i, j);
							sortedCount++;
							if (p > i)
								range[i] = p - 1;
							if (p < j)
								range[p + 1] = j;
							range[i = p] = -1; // sorted
						}
					}
				} else {
					// skip[i] += skip[i + skip[i]];
					while ((j = range[i - range[i]]) < 0)
						range[i] += j;
					i += -range[i] - 1;
				}
		}
	}

	public int partition(int left, int right) {
		// DK: added check if (left == right):
		if (left == right)
			return left;
		int i = left - 1;
		int j = right;
		while (true) {
			while (this.lid[++i] < this.lid[right])
				// find item on left to swap
				; // a[right] acts as sentinel
			while (this.lid[right] < this.lid[--j])
				// find item on right to swap
				if (j == left)
					break; // don't go out-of-bounds
			if (i >= j)
				break; // check if pointers cross
			swap(i, j); // swap two elements into place
		}
		swap(i, right); // swap with partition element
		return i;
	}

	/**
	 * Tri rapide récursif
	 */
	/*
	 * public void triRapideRec() { triRapideRec(0, size.get() - 1); }
	 * 
	 * private void triRapideRec(int deb, int fin) { if (deb < fin) { int
	 * positionPivot = partition(deb, fin); triRapideRec(deb, positionPivot -
	 * 1); triRapideRec(positionPivot + 1, fin); } }
	 * 
	 * private int partition(int deb, int fin) { int compt = deb; int pivot =
	 * this.lid[deb]; for (int i = deb + 1; i <= fin; i++) { if (this.lid[i] <
	 * pivot) { compt++; swap(compt, i); } } swap(deb, compt); return compt; }
	 */

	/*
	 * private int partition(int deb, int fin) { int compt = deb; int pivot =
	 * this.lid[deb]; for (int i = deb + 1; i <= fin; i++) { if (this.lid[i] <
	 * pivot) { compt++; swap(compt, i); } } swap(deb, compt); return compt; }
	 */

	private void swap(int i, int j) {
		int lidTempo = this.lid[i];
		int tTempo = this.t[i];
		this.lid[i] = this.lid[j];
		this.t[i] = this.t[j];
		this.lid[j] = lidTempo;
		this.t[j] = tTempo;
	}

	public String toString() {
		String res = "";
		for (int i = top.get(); i < size.get() + top.get(); i++)
			res += "<" + lid[i] + "," + t[i] + ">, ";
		return res;
	}

}
