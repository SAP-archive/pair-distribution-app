package pair.distribution.app.trello.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class DevPairCombinations extends PairCombinations {


	private List<DayPairs> pastPairs;

	public DevPairCombinations(List<DayPairs> pastPairs) {
		this.pastPairs = pastPairs;
		sortByDescendDate();
	}
	
	@Override
	public List<Pair> getPairs() {
		List<Pair> result = new ArrayList<>();
		pastPairs.stream().forEach(dayPairs -> dayPairs.getPairs().values().stream()
                .filter(pair -> !pair.isOpsPair())
                .forEach(result::add));
		return result;
	}

	@Override
	public List<Pair> getPastPairs(int daysBack) {
		if(pastPairs.size() > daysBack) {
			return pastPairs.get(daysBack).getPairs().values().stream().filter(pair -> !pair.isOpsPair()).collect(Collectors.toList());
		}
		return null;
	}

	@Override
	public Pair getPastPairByTrack(int daysBack, String track) {
		if(pastPairs.size() > daysBack) {
			Pair pairByTrack = pastPairs.get(daysBack).getPairByTrack(track);
			if(pairByTrack != null && pairByTrack.isOpsPair()) {
				throw new RuntimeException("Dev Pair shouldn't be Ops for track: " + track);
			}
			return pairByTrack;
		}
		return null;
	}	

	private void sortByDescendDate() {
		Collections.sort(pastPairs);
		Collections.reverse(pastPairs);
	}
}
