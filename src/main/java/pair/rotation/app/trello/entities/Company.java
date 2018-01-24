package pair.rotation.app.trello.entities;

import java.util.List;
import java.util.stream.Collectors;

public class Company {

	private String name;

	public Company(String name) {
		this.name = name.trim();
	}

	public String getName() {
		return name.toLowerCase();
	}

	public String getOriginalName() {
		return name;
	}
		
	public List<Developer> getCompanyDevs(List<Developer> todayDevs) {
		return todayDevs.stream().filter(developer -> developer.getCompany().getName().equals(this.getName())).collect(Collectors.toList());
	}

	public String getTrack() {
		return name.toUpperCase() + "-internal";
	}
}
