package dsv.buecher;

public interface GenericRequestInterface<T> extends Iterable<T> {
	public int size();

	public T get(int i);

}
