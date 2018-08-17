package pair.distribution.app.trello.entities;

import java.util.List;

public interface PairCombinations {


	List<Pair> getPairs();
	
	List<Pair> getPastPairs(int daysBack);
	
	Pair getPastPairByTrack(int daysBack, String track);

	boolean isRotationTime(List<String> possibleTracks, List<Developer> availableDevs);
}
