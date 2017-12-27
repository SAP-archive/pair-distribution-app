package pair.rotation.app.trello.entities;

public class Company {

	private String name;
	private boolean devOps;

	public Company(String name) {
		this.name = name.trim();
	}

	public String getName() {
		return name.toLowerCase();
	}

	public String getOriginalName() {
		return name;
	}
	
	public void setDevOps(boolean devOps) {
		this.devOps = devOps;
	}
	
	public boolean getDevOps() {
		return devOps;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Company other = (Company) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
