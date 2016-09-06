package dsv.buecher;

public class VerlagNationalitat {
	String name, desc;

	public VerlagNationalitat(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	@Override
	public String toString() {
		return name + "(" + desc + ")";
	}
}
