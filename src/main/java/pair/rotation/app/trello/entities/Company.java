package pair.rotation.app.trello.entities;

import java.util.List;
import java.util.function.Predicate;
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
		
	public List<Developer> getCompanyExperiencedDevs(List<Developer> todayDevs) {
		return todayDevs.stream().filter(developer -> !developer.getNew()).filter(isSameCompanry()).collect(Collectors.toList());
	}

	public String getTrack() {
		return name.toUpperCase() + "-ops/interrupt";
	}

	public String getCompanyTrack(List<String> possibleTracks) {
		return possibleTracks.stream().filter(track -> isCompanyTrack(track)).findFirst().orElse(null);
	}

	public boolean isCompanyTrack(String track) {
		return track.split("-")[0].toLowerCase().equals(getName());
	}

	public List<Developer> getDevs(List<Developer> todayDevs) {
		return todayDevs.stream().filter(isSameCompanry()).collect(Collectors.toList());
	}

	private Predicate<? super Developer> isSameCompanry() {
		return developer -> developer.getCompany().getName().equals(this.getName());
	}
}
