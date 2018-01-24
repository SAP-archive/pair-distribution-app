package pair.rotation.app.trello.entities;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class OpsPairCombinations implements PairCombinations {
	
	private List<DayPairs> pastPairs;
	private int daysIntoFuture;

	public OpsPairCombinations(List<DayPairs> dayPairs, int daysIntoFuture) {
		this.pastPairs = dayPairs;
		this.daysIntoFuture = daysIntoFuture;
		sortByDescendDate();
	}

	public OpsPairCombinations(List<DayPairs> dayPairs) {
		this(dayPairs, 0);
	}
	
	@Override
	public List<Pair> getPairs() {
		List<Pair> result = new ArrayList<>();
		pastPairs.stream().forEach(dayPairs -> dayPairs.getPairs().values().stream()
                .filter(pair -> pair.isOpsPair())
                .forEach(pair -> result.add(pair)));
		return result;
	}

	@Override
	public boolean isRotationTime(List<String> possibleTracks, List<Developer> availableDevs) {
		LocalDateTime currentWeekDate = LocalDateTime.ofInstant(getStartDate().toInstant(), ZoneId.systemDefault());
		DayPairs lastDayPairs = getLastDayPairs();
		if (lastDayPairs != null) {
			LocalDateTime lastPairWeekDate = LocalDateTime.ofInstant(lastDayPairs.getDate().toInstant(), ZoneId.systemDefault());
			WeekFields weekFields = WeekFields.of(Locale.getDefault());
			return currentWeekDate.get(weekFields.weekOfWeekBasedYear()) != lastPairWeekDate.get(weekFields.weekOfWeekBasedYear());
		}
		return false;
	}

	@Override
	public List<Pair> getPastPairs(int daysBack) {
		if(pastPairs.size() > daysBack) {
			return pastPairs.get(daysBack).getPairs().values().stream().filter(pair -> pair.isOpsPair()).collect(Collectors.toList());
		}
		return null;
	}

	@Override
	public Pair getPastPairByTrack(int daysBack, String track) {
		if(pastPairs.size() > daysBack) {
			Pair pairByTrack = pastPairs.get(daysBack).getPairByTrack(track);
			if(pairByTrack != null && !pairByTrack.isOpsPair()) {
				throw new RuntimeException("Dev Pair should be Ops for track: " + track);
			}
			return pairByTrack;
		}
		return null;
	}
	
	private void sortByDescendDate() {
		Collections.sort(pastPairs);
		Collections.reverse(pastPairs);
	}
	
	private DayPairs getLastDayPairs() {
		if(pastPairs.size() > 0) {
			return pastPairs.get(0);
		}
		return null;
	}
	
	private Date getStartDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, daysIntoFuture);
		return cal.getTime();
	}
}
