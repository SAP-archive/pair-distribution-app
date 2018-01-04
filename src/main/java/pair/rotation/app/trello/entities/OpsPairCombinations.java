package pair.rotation.app.trello.entities;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class OpsPairCombinations implements PairCombinations {
	
	private List<DayPairs> pastPairs;

	public OpsPairCombinations(List<DayPairs> dayPairs) {
		this.pastPairs = dayPairs;
		sortByDescendDate();
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
		LocalDateTime currentWeekDate = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault());
		LocalDateTime lastPairWeekDate = LocalDateTime.ofInstant(getLastDayPairs().getDate().toInstant(), ZoneId.systemDefault());
		WeekFields weekFields = WeekFields.of(Locale.getDefault());
		return currentWeekDate.get(weekFields.weekOfWeekBasedYear()) != lastPairWeekDate.get(weekFields.weekOfWeekBasedYear());
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
			if(!pairByTrack.isOpsPair()) {
				throw new RuntimeException("Dev Pair should be Ops");
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
}
