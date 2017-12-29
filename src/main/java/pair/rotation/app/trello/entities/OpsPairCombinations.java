package pair.rotation.app.trello.entities;

import java.util.Collections;
import java.util.List;

public class OpsPairCombinations implements PairCombinations {
	
	private List<DayPairs> pastPairs;

	public OpsPairCombinations(List<DayPairs> dayPairs) {
		this.pastPairs = dayPairs;
		sortByDescendDate();
	}
	
	@Override
	public List<Pair> getPairs() {
		return null;
	}

	private void sortByDescendDate() {
		Collections.sort(pastPairs);
		Collections.reverse(pastPairs);
	}

	@Override
	public boolean isRotationTime(List<String> possibleTracks, List<Developer> availableDevs) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Pair> getPastPairs(int daysBack) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pair getPastPairByTrack(int daysBack, String track) {
		// TODO Auto-generated method stub
		return null;
	}
}
