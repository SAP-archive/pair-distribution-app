package pair.rotation.app.trello.entities;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Transient;

public class DayPairs implements Comparable<DayPairs>{

	private Map<String, Pair> pairs;
	private Date date;
	@Transient
	private SimpleDateFormat dateFormatter;
	
	public DayPairs() {
		dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
		pairs = new HashMap<>();
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

	@Override
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
				                      .map(this::getPairByTrack)
				                      .orElse(null);
	}
	
	public void addPiars(Map<String, Pair> pairsToAdd) {
		pairs.putAll(pairsToAdd);
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
		} else if (!date.equals(other.date)) {
			return false;
		}
		return true;
	}
	
	private Date getDateWithoutTime(Date dateToFormat){
		try {
			return dateFormatter.parse(dateFormatter.format(dateToFormat));
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}			
	}

	public String format(Date dateToFormat) {
		return dateFormatter.format(dateToFormat);
	}

	public Date parse(String dateToParse) throws ParseException {
		return dateFormatter.parse(dateToParse);
	}
}
