package dsv.buecher;

public class HeaderDef {
	int width = 20 * 8;

	public HeaderDef(String name, String field) {
		super();
		this.name = name;
		this.field = field;
	}

	public HeaderDef(String string, String string2, int i) {
		this(string, string2);
		this.width = i * 8;
	}

	public HeaderDef setAlignment(int n) {
		this.alignment = n;
		return this;
	}

	String name;
	String field; // for ordering
	public int alignment = 0;
}
