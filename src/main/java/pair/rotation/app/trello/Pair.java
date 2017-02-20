package pair.rotation.app.trello;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Pair {

	private List<String> devs;

    public Pair() {
    	devs = new ArrayList<String>(2);
    }

    public Pair(List<String> devs) {
    	this();
		this.setDevs(devs);
    }

	public List<String> getDevs() {
		return Arrays.asList(devs.toArray(new String[0]));
	}

	public void setDevs(List<String> devs) {
		this.devs.addAll(devs);
		Collections.sort(this.devs);
	}

	public void addDev(String dev) {
		if (dev != null){
			devs.add(dev);
			Collections.sort(devs);	
		}
	}

	public boolean hasDev(String dev) {
		return devs.contains(dev);
	}
	
	public String getOtherDev(String dev) {
		if(devs.contains(dev)){
			for (String pairDev : devs) {
				if(!pairDev.equals(dev)){
					return pairDev;
				}
			}
		}
		return null;
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
		} else if (!devs.equals(other.devs))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Pair [devs=" + devs + "]";
	}

	public boolean isSolo() {
		return devs.size() == 1;
	}
}
