package dsv.buecher;

public class CsvRegister {
	private StringBuilder sb;
	private String separator;

	public CsvRegister(String separator) {
		this.separator = separator;
	}

	public void add(String field) {
		if (sb == null)
			sb = new StringBuilder(field);
		else
			sb.append(separator).append(field);
	}

	public void add(int id) {
		add(Integer.toString(id));
	}

}
