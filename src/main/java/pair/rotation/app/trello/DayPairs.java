package pair.rotation.app.trello;


import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DayPairs implements Comparable<DayPairs>{

	private Map<String, Pair> pairs;
	private Date date;
	
	public DayPairs() {
		pairs = new HashMap<String, Pair>();
		date = getDateWithoutTime(new Date());
	}
	
	public void setPairs(Map<String, Pair> pairs){
		this.pairs = pairs;
	}
	
	public Map<String, Pair> getPairs() {
		return pairs;
	}
	public void addPair(String track, Pair pair) {
		this.pairs.put(track, pair);
	}
	
	public Set<String> getTracks() {
		return pairs.keySet();
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = getDateWithoutTime(date);
	}

	public int compareTo(DayPairs otherPair) {
		return getDate().compareTo(otherPair.getDate());
	}

	public Pair getPairByTrack(String track) {
		return pairs.get(track);
	} 
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
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
		DayPairs other = (DayPairs) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Pairs [pairs=" + pairs + ", date=" + DayPairsHelper.DATE_FORMATTER.format(date) + "]";
	}

	public boolean hasPair(Pair pair) {
		return pairs.containsValue(pair);
	}

	public void replacePairWith(Pair oldPair, Pair newPair) {
		String track = null;
		for (String key : pairs.keySet()) {
			Pair pair = pairs.get(key);
			if(pair.equals(oldPair)){
				track = key;
			}
		}
		pairs.put(track, newPair);
	}

	public String getTrackByPair(Pair pair) {
		for (String key : pairs.keySet()) {
			if (pairs.get(key).equals(pair)){
				return key;
			}
		}
		return null;
	}
	
	public Pair getSoloPair() {
		Pair soloPair = null;
		for (String track : getPairs().keySet()) {
			Pair pairByTrack = getPairByTrack(track);
			if(pairByTrack.isSolo()){
				soloPair = pairByTrack;
			}
		}
		return soloPair;
	}
	
	private Date getDateWithoutTime(Date dateToFormat){
		try {
			return DayPairsHelper.DATE_FORMATTER.parse(DayPairsHelper.DATE_FORMATTER.format(dateToFormat));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO this should happen
		return null;
	}
}
