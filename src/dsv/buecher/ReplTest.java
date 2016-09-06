package dsv.buecher;

public class ReplTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.err.println("12-3".replaceAll("^(\\d*)\\D?.*$", "$1"));

	}
}
