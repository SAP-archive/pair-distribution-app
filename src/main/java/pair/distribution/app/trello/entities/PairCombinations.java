package pair.distribution.app.trello.entities;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PairCombinations {

    private static final Logger logger = LoggerFactory.getLogger(DevPairCombinations.class);

	public abstract List<Pair> getPairs();
	
	public abstract List<Pair> getPastPairs(int daysBack);
	
	public abstract Pair getPastPairByTrack(int daysBack, String track);

	public boolean isRotationTime(List<String> possibleTracks, final List<Developer> availableDevs, boolean rotateEveryday) {
		if (rotateEveryday) {
			return true;
		}

		boolean rotation = false;
		for (String track : possibleTracks) {
			Pair trackPairOneDayBack = getPastPairByTrack(0, track);
			Pair trackPairTwoDaysBack = getPastPairByTrack(1, track);
			rotation = rotation || isPairRotationTime(trackPairOneDayBack, trackPairTwoDaysBack, availableDevs);
		}
		return rotation;
	}

	private boolean isPairRotationTime(Pair trackPairOneDayBack, Pair trackPairTwoDaysBack, List<Developer> availableDevs) {
		if(trackPairOneDayBack != null){
			boolean pairForTwoDays = isPairForTwoDays(trackPairOneDayBack, trackPairTwoDaysBack);
			boolean pairNewDevConform = isPairConform(trackPairOneDayBack, getFilteredDevs(availableDevs, developer -> developer.getNew()), isPairWithMixedExpirience());
			logger.info("Rotation time for longest dev is : {} and for NewDevelopers is : {}", pairForTwoDays, !pairNewDevConform);
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
