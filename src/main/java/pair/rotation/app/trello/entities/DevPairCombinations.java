package pair.rotation.app.trello.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DevPairCombinations implements PairCombinations {

    private static final Logger logger = LoggerFactory.getLogger(DevPairCombinations.class);
    
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
                .forEach(pair -> result.add(pair)));
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
	
	@Override
	public boolean isRotationTime(List<String> possibleTracks, final List<Developer> availableDevs) {
		boolean rotation = false;
		for (String track : possibleTracks) {
			Pair trackPairOneDayBack = getPastPairByTrack(0, track);
			Pair trackPairTwoDaysBack = getPastPairByTrack(1, track);
			rotation = rotation || isPairRotationTime(trackPairOneDayBack, trackPairTwoDaysBack, availableDevs);
		}
		return rotation;
	}

	private void sortByDescendDate() {
		Collections.sort(pastPairs);
		Collections.reverse(pastPairs);
	}

		
	private boolean isPairRotationTime(Pair trackPairOneDayBack, Pair trackPairTwoDaysBack, List<Developer> availableDevs) {
		if(trackPairOneDayBack != null){
			boolean pairForTwoDays = isPairForTwoDays(trackPairOneDayBack, trackPairTwoDaysBack);
			boolean pairNewDevConform = isPairConform(trackPairOneDayBack, getFilteredDevs(availableDevs, developer -> developer.getNew()), isPairWithMixedExpirience());
			logger.info("Rotation time for longest dev is : " + pairForTwoDays + " and for NewDevelopers is : " + !pairNewDevConform);
			return pairForTwoDays || !pairNewDevConform;			
		}
		return false;
	}
	
	private boolean isPairForTwoDays(Pair trackPairOneDayBack, Pair trackPairTwoDaysBack) {
		return trackPairOneDayBack.equals(trackPairTwoDaysBack);
	}
	
	private List<Developer> getFilteredDevs(List<Developer> availableDevs, Predicate<? super Developer> predicate) {
		return availableDevs.stream().filter(predicate).collect(Collectors.toList());
	}
	
	private Predicate<? super Pair> isPairWithMixedExpirience() {
		return p -> !(p.getFirstDev().getNew() && p.getOtherDev(p.getFirstDev()).getNew());
	}
	
	private boolean isPairConform(Pair pair, List<Developer> predicateDevelopers, Predicate<? super Pair> predicate){
		switch (pair.getDevs().size()) {
		case 0:
			return true;
		case 1:
			return !predicateDevelopers.contains(pair.getFirstDev());
		default:
			return predicateDevelopers.contains(pair.getFirstDev()) || predicateDevelopers.contains(pair.getSecondDev()) ? predicate.test(pair) : true; 
		}
	}
}
