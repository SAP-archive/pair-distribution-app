package pair.rotation.app.trello.entities;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DayPairs implements Comparable<DayPairs>{

	private Map<String, Pair> pairs;
	private Date date;
	private SimpleDateFormat dateFormatter;
	
	public DayPairs(SimpleDateFormat dateFormatter) {
		this.dateFormatter = dateFormatter;
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
	public String toString() {
		return "Pairs [pairs=" + pairs + ", date=" + dateFormatter.format(date) + "]";
	}

	public boolean hasPair(Pair pair) {
		return pairs.containsValue(pair);
	}

	public void replacePairWith(Pair oldPair, Pair newPair) {
		pairs.keySet().stream().filter(key -> pairs.get(key).equals(oldPair) ).findFirst().ifPresent(track -> pairs.put(track, newPair));
	}

	public String getTrackByPair(Pair pair) {
		return pairs.keySet().stream().filter(key -> pairs.get(key).equals(pair) ).findFirst().orElse(null);
	}
	
	public Pair getSoloPair() {
		return pairs.keySet().stream().filter(track -> getPairByTrack(track).isSolo() )
				                      .findFirst()
				                      .map(soloTrack -> getPairByTrack(soloTrack))
				                      .orElse(null);
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
	
	private Date getDateWithoutTime(Date dateToFormat){
		try {
			return dateFormatter.parse(dateFormatter.format(dateToFormat));
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}			
	}
}
