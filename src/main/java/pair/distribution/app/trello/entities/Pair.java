package pair.distribution.app.trello.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Pair {

	private List<Developer> devs;
	private boolean buildPair;
	private boolean communityPair;
	private boolean opsPair;
	private String track;

    public Pair() {
    		devs = new ArrayList<>(2);
    		buildPair = false;
    		opsPair = false;
    		track = "";
    }

    public Pair(List<Developer> devs) {
    		this();
		this.setDevs(devs);
    }

    public Pair(List<Developer> devs, boolean opsPair) {
		this(devs);
		this.setOpsPair(opsPair);
}
    
	public List<Developer> getDevs() {
		return Arrays.asList(devs.toArray(new Developer[0]));
	}

	public Developer getFirstDev() {
		return devs.get(0);
	}
	
	public Developer getSecondDev() {
		if ( devs.size() == 2){
			return devs.get(1);
		}
		return null;
	}
	
	public void setDevs(List<Developer> devs) {
		this.devs.addAll(devs);
		this.devs.removeIf(Objects::isNull);
		Collections.sort(this.devs);
	}

	public void addDev(Developer dev) {
		if (dev != null){
			devs.add(dev);
			Collections.sort(devs);	
		}
	}

	public boolean hasDev(Developer dev) {
		return devs.contains(dev);
	}
	
	public Developer getOtherDev(Developer dev) {
		return devs.contains(dev) ? devs.stream().filter(developer -> !developer.equals(dev)).findFirst().orElse(null): null;
	}
	
	public boolean isComplete(){
		return devs.size() == 2;
	}
	
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((devs == null) ? 0 : devs.hashCode());
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
		Pair other = (Pair) obj;
		if (devs == null) {
			if (other.devs != null)
				return false;
		} else if (!devs.equals(other.devs)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "Pair [devs=" + devs + ", opsPair=" + opsPair + "]";
	}

	public boolean isSolo() {
		return devs.size() == 1;
	}

	public boolean isBuildPair() {
		return buildPair;
	}

	public void setBuildPair(boolean buildPair) {
		this.buildPair = buildPair;
	}

	public void setCommunityPair(boolean communityPair) {
		this.communityPair = communityPair;
	}

	public boolean isCommunityPair() {
		return communityPair;
	}

	public void setOpsPair(boolean opsPair) {
		this.opsPair = opsPair;
	}

	public boolean isOpsPair() {
		return opsPair;
	}

	public void setTrack(String track) {
		this.track = track;
	}
	
	public String getTrack() {
		return this.track;
	}
}
