package solver.constraints.propagators.nary.scheduling.dataStructures;


public class IncArrayEvents {

	public int[] date;
	public int[] t;
	public int[] type;
	public int[] dec;

	private int size; // number of elements in the structure.


	public IncArrayEvents(int _n) {
		this.date = new int[_n];
		this.t = new int[_n];
		this.type = new int[_n];
		this.dec = new int[_n];
		this.size = 0;
	}

	public void add(int _date, int _task, int _type, int _dec) {
		this.date[size] = _date;
		this.t[size] = _task;
		this.type[size] = _type;
		this.dec[size] = _dec;
		size++;
	}

	
	public int size() {
		return this.size;
	}
	
	
	

	public boolean isEmpty() {
		return (size == 0);
	}

	public void sort() {
		iterativeQuicksort();
	}


	/**
	 * Tri rapide itératif
	 */
	public void iterativeQuicksort() {
		int[] range = new int[size + 1]; // if (range[i]<0) then skip[i] =
												// |range[i]|
		range[0] = size - 1;
		int i, j, sortedCount = 0;
		while (sortedCount < size) {
			for (i = 0; i < size; i++)
				if (range[i] >= i) {
					j = range[i];
					if (j - i < 7) {
						// selectionsort the elements from a[i] to a[j]
						// inclusive
						// and set all their ranges to -((j+1)-k)
						for (int m = i; m <= j; m++) {
							for (int n = m; n > i
									&& this.date[n - 1] > this.date[n]; n--)
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
			while (this.date[++i] < this.date[right])
				// find item on left to swap
				; // a[right] acts as sentinel
			while (this.date[right] < this.date[--j])
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


	private void swap(int i, int j) {
		int dateTempo = this.date[i];
		int tTempo = this.t[i];
		int typeTempo = this.type[i];
		int decTempo = this.dec[i];
		this.date[i] = this.date[j];
		this.t[i] = this.t[j];
		this.type[i] = this.type[j];
		this.dec[i] = this.dec[j];
		this.date[j] = dateTempo;
		this.t[j] = tTempo;
		this.type[j] = typeTempo;
		this.dec[j] = decTempo;
	}

	public String toString() {
		String res = "";
		for (int i = 0; i < size; i++)
			res += "<"+date[i] +","+ t[i] +","+type[i]+","+dec[i]+">, ";
		return res;
	}
	
	
}
