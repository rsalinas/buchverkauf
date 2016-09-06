package dsv.buecher;

public class BooleanDifference {

	public BooleanDifference(String key, boolean value) {
		super();
		this.key = key;
		this.value = value;
	}

	String key;
	boolean value;

	@Override
	public String toString() {
		return key + "(" + (value ? 't' : 'f') + ")";
	}
}
