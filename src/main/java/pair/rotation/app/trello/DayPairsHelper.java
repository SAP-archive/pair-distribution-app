package pair.rotation.app.trello;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pair.rotation.app.persistence.mongodb.TrelloPairsRepository;

public class DayPairsHelper {
	
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yyyy");
    private static final Logger logger = LoggerFactory.getLogger(DayPairsHelper.class);
	
	private TrelloPairsRepository repository;

	public DayPairsHelper(TrelloPairsRepository repository) {
		this.repository = repository;
	}
	
	public void updateDataBaseWithTrelloContent(List<DayPairs> pairs) {
		for (DayPairs pairsByDate : pairs) {
			List<DayPairs> findByDate = repository.findByDate(pairsByDate.getDate());
			if (findByDate.size() == 1) {
				logger.info("Update by date found");
				logger.info("Repository state: " + findByDate.get(0));
				logger.info("Board state: " + pairsByDate);
				repository.deleteByDate(pairsByDate.getDate());
				repository.save(pairsByDate);
				logger.info("Update finished");
			} else if (findByDate.isEmpty()) {
				logger.info("No update found. Adding to repository");
				repository.save(pairsByDate);
				logger.info("Adding finished");
			} else {
				logger.info("More than one entry found. They are: " + findByDate.toString());
				throw new RuntimeException();
			}
		}
	}
	
	public Map<Pair, Integer> buildPairsWeightFromPastPairing(List<DayPairs> pastPairs, List<Developer> availableDevs) {
		Map<Pair, Integer> result = new HashMap<>(); 
		initPairsInitialWeight(availableDevs, result, false);
		for (DayPairs dayPairs : pastPairs) {
			for (Pair pair : dayPairs.getPairs().values()) {
				if(pair.isComplete()){
					result.put(pair, result.getOrDefault(pair, 0) + 1);
				}
			}
		}
		return result;
	}
	
	public Map<Pair, Integer> buildBuildPairsWeightFromPastPairing(List<DayPairs> pastPairs, List<Developer> availableDevs) {
		Map<Pair, Integer> result = new HashMap<>(); 
		initPairsInitialWeight(availableDevs, result, true);
		for (DayPairs dayPairs : pastPairs) {
			for (Pair pair : dayPairs.getPairs().values()) {
				if(pair.isBuildPair()){
					result.put(pair, result.getOrDefault(pair, 0) + 1);
				}
			}
		}
		return result;
	}

	public void adaptPairsWeight(Map<Pair, Integer> pairsWeight, List<Developer> availableDevs) {
		List<Developer> developersOnDoD = getFilteredDevs(availableDevs, developer -> developer.getDoD());
		List<Developer> newDevelopers = getFilteredDevs(availableDevs, developer -> developer.getNew());
		for (Pair pair : pairsWeight.keySet()) {
			if(!isPairConform(pair, developersOnDoD, isPairFromSameCompany())){
				logger.info("pair needs DoD adaptation. Pair is: " + pair);
				addUnconformWeight(pairsWeight, pair);
			}
			if(!isPairConform(pair, newDevelopers, isPairWithMixedExpirience())){
				logger.info("pair with new Developers and needs adaptation. Pair is: " + pair);
				addUnconformWeight(pairsWeight, pair);
			}
		}
	}

	private void addUnconformWeight(Map<Pair, Integer> pairsWeight, Pair pair) {
		int weight = pairsWeight.get(pair) + 100;
		pairsWeight.put(pair, weight);
	}
	
	private boolean isPairConform(Pair pair, List<Developer> releventDevelopers, Predicate<? super Pair> predicate){
		switch (pair.getDevs().size()) {
		case 0:
			return true;
		case 1:
			return !releventDevelopers.contains(pair.getFirstDev());
		default:
			Developer pairDeveloper = pair.getFirstDev();
			Developer pairOtherDeveloper = pair.getSecondDev();
			boolean isPairOnDoD = releventDevelopers.contains(pairDeveloper) || releventDevelopers.contains(pairOtherDeveloper);
			if(isPairOnDoD){
				return predicate.test(pair);
			}
			return true;
		}
	}
	
	private List<Developer> getFilteredDevs(List<Developer> availableDevs, Predicate<? super Developer> predicate) {
		return availableDevs.stream().filter(predicate).collect(Collectors.toList());
	}
	
	private void initPairsInitialWeight(List<Developer> availableDevs, Map<Pair, Integer> result, boolean addSoloPairs) {
		List<Pair> allPairCombinations = getAllPairCombinations(availableDevs, addSoloPairs);
		for (Pair pair : allPairCombinations) {
			result.put(pair, 0);
		}
	}
	
	private List<Pair> getAllPairCombinations(List<Developer> availableDevs, boolean addSoloPairs) {
		List<Pair> result = new ArrayList<>();
		if(availableDevs.isEmpty() || (availableDevs.size() == 1 && !addSoloPairs)){
			return result;
		} else if (availableDevs.size() == 1 && addSoloPairs) {
			result.add(new Pair(Arrays.asList(availableDevs.get(0))));
			return result;
		}
		
		for(int i = 1; i < availableDevs.size(); i++){
			result.add(new Pair(Arrays.asList(availableDevs.get(0), availableDevs.get(i))));
		}
		result.addAll(getAllPairCombinations(availableDevs.subList(1, availableDevs.size()), addSoloPairs));
		return result;
	}

	public DayPairs generateNewDayPairs(List<String> tracks, List<Developer> devs, List<DayPairs> pastPairs,
			Map<Pair, Integer> pairsWeight, boolean rotate_everyday) {
		DayPairs result = new DayPairs();
		sortByDescendDate(pastPairs);
		List<String> possibleTracks = getPossibleTracks(tracks, devs);
		List<Developer> availableDevs = new ArrayList<Developer>(devs);
		for (String track : possibleTracks) {
			Pair pair = tryToFindPair(track, pastPairs, availableDevs, rotate_everyday);
			availableDevs.removeAll(pair.getDevs());
			if(!pair.isComplete() && availableDevs.size() > 0){
				pair = getPairByWeight(pair, availableDevs, pairsWeight);
			}
			if( pair == null && availableDevs.size() == 1){
				pair = new Pair();
				pair.setDevs(availableDevs);
			}
			if(pair != null){
				availableDevs.removeAll(pair.getDevs());
				result.addPair(track, pair);	
			}
			
		}
		return result;
	}

	public void rotateSoloPairIfAny(DayPairs todayPairs, List<DayPairs> pastPairs, Map<Pair, Integer> pairsWeight) {
		Pair soloPair = todayPairs.getSoloPair();		
		if(soloPair != null && (isSoloPairForTwoDays(pastPairs, soloPair) || soloPair.getFirstDev().getDoD() || soloPair.getFirstDev().getNew())){
			Developer soloDeveloper = soloPair.getFirstDev();
			Pair pairWithHighestWeight = null; 
			Developer newPairForSoloDeveloper = null;
		    Collection<Pair> allPairsOfTheDay = todayPairs.getPairs().values();
			String soloPairCompany = soloDeveloper.getCompany();
			if (soloDeveloper.getDoD()){
		    	pairWithHighestWeight = getPairWithHighestWeightForPredicat(allPairsOfTheDay, pairsWeight, hasPairDevFromCompany(soloPairCompany));
		    	if (pairWithHighestWeight != null){
		    		newPairForSoloDeveloper = pairWithHighestWeight.isPairFromSameCompany() ? getRandomDev(pairWithHighestWeight.getDevs()) : pairWithHighestWeight.getDevFromCompany(soloPairCompany);
		    	}
		    } else if (soloDeveloper.getNew()) { 
		    	pairWithHighestWeight = getPairWithHighestWeightForPredicat(allPairsOfTheDay, pairsWeight, isPairWithNoNewDevelopers());
		    	newPairForSoloDeveloper = pairWithHighestWeight == null ? null : getRandomDev(pairWithHighestWeight.getDevs());
		    } else{
		    	pairWithHighestWeight = getPairWithHighestWeightForPredicat(allPairsOfTheDay, pairsWeight, alwaysTrue());
		    	newPairForSoloDeveloper = getRandomDev(pairWithHighestWeight.getDevs());
		    }
			if (newPairForSoloDeveloper == null){
				//No rotation for Solo developer possible
	    		return;
			}
			Pair newPair = new Pair(Arrays.asList(soloPair.getDevs().get(0), newPairForSoloDeveloper));
			todayPairs.replacePairWith(pairWithHighestWeight, newPair);
			todayPairs.replacePairWith(soloPair, new Pair(Arrays.asList(pairWithHighestWeight.getOtherDev(newPairForSoloDeveloper))));
		}
	}

	private Predicate<? super Pair> isPairFromSameCompany() {
		return p -> p.getFirstDev().getCompany().equals(p.getOtherDev(p.getFirstDev()).getCompany());
	}
	
	private Predicate<? super Pair> isPairWithMixedExpirience() {
		return p -> !(p.getFirstDev().getNew() && p.getOtherDev(p.getFirstDev()).getNew());
	}
	
	private Predicate<? super Pair> isPairWithNoNewDevelopers() {
		return p -> !(p.getFirstDev().getNew() || p.getOtherDev(p.getFirstDev()).getNew());
	}
	
	private boolean isSoloPairForTwoDays(List<DayPairs> pastPairs, Pair soloPair) {
		DayPairs firstDayPair = pastPairs.size() > 0 ? pastPairs.get(0) : null;
		DayPairs secondDayPair = pastPairs.size() > 1 ? pastPairs.get(1) : null;
		return firstDayPair!= null && secondDayPair != null && firstDayPair.hasPair(soloPair) && secondDayPair.hasPair(soloPair);
	}
	
	private Predicate<? super Pair> hasPairDevFromCompany(String company) {
		return p -> p.getDevFromCompany(company) != null;
	}
	
	private Predicate<? super Pair> alwaysTrue() {
		return p -> true;
	}
	
	private Pair getPairWithHighestWeightForPredicat(Collection<Pair> values, Map<Pair, Integer> pairsWeight, Predicate<? super Pair> predicate) {
		int highesttWeight = 0;
		Pair result = null;
		for (Pair pair : values) {
			if(pairsWeight.containsKey(pair)){
				Integer weight = pairsWeight.get(pair);
				if((highesttWeight < weight || result == null) && (predicate.test(pair))){
					result = pair;
					highesttWeight = weight;
				}				
			}
		}
		return result;
	}
	
	private void sortByDescendDate(List<DayPairs> pastPairs) {
		Collections.sort(pastPairs);
		Collections.reverse(pastPairs);
	}
		
	private List<String> getPossibleTracks(List<String> todaysTracks, List<Developer> todaysDevs){
		int possibleTracksCount = (int) Math.ceil(todaysDevs.size() / 2.0);
		List<String> possibleTracks;
		if (possibleTracksCount < todaysTracks.size()){
			possibleTracks = todaysTracks.subList(0, possibleTracksCount);
		} else {
			possibleTracks = todaysTracks;
		}
		return possibleTracks;
	}
	
	private Pair tryToFindPair(String track, List<DayPairs> pastPairs, final List<Developer> availableDevs, boolean rotate_everyday) {
		Pair trackPairToday = new Pair();
		Pair trackPairOneDayBack = getPastPairByTrack(pastPairs, track, 0);
		Pair trackPairTwoDaysBack = getPastPairByTrack(pastPairs, track, 1);
		Pair trackPairThreeDaysBack = getPastPairByTrack(pastPairs, track, 2);

		logger.info("Track is: " + track);
		logger.info("Pair one day back: " + trackPairOneDayBack);
		logger.info("Pair two days back: " + trackPairTwoDaysBack);
		logger.info("Pair three days back: " + trackPairThreeDaysBack);
		
		if(rotate_everyday || isRotationTime(trackPairOneDayBack, trackPairTwoDaysBack, availableDevs)) {
			logger.info("time to rotate");
			if(trackPairOneDayBack.isSolo()){
				logger.info("Solo don't do anything");
				// dev should stay on track
//				trackPairToday.setDevs(trackPairOneDayBack.getDevs());
			}else if(hasHistoryForLongestDev(trackPairThreeDaysBack)){
				logger.info("There is history to find longest dev");
				Developer longestDevOnStory = getLongestDevOnStory(trackPairOneDayBack, trackPairThreeDaysBack);
				if (longestDevOnStory != null && availableDevs.contains(longestDevOnStory)) {
					Developer longestDev = getDeveloperById(availableDevs, longestDevOnStory);
					logger.info("Longest dev is" + longestDev);
					trackPairToday.addDev(longestDev);
				}
			}else{
				//there is no older history. Remove random one
				logger.info("No older history. Add one dev random");
				trackPairToday.addDev(getRandomDev(getAvailableDevs(availableDevs, trackPairOneDayBack.getDevs())));
			}
		}else if(trackPairOneDayBack != null){
			logger.info("No rotation required");
			trackPairToday.setDevs(getAvailableDevs(availableDevs, trackPairOneDayBack.getDevs()));	
		}
		return trackPairToday;
	}

	private boolean hasHistoryForLongestDev(Pair trackPairThreeDaysBack) {
		return trackPairThreeDaysBack != null;
	}

	private boolean isRotationTime(Pair trackPairOneDayBack, Pair trackPairTwoDaysBack, List<Developer> availableDevs) {
		if(trackPairOneDayBack != null){
			boolean pairForTwoDays = isPairForTwoDays(trackPairOneDayBack, trackPairTwoDaysBack);
			boolean pairDoDConform = isPairConform(trackPairOneDayBack, getFilteredDevs(availableDevs, developer -> developer.getDoD()), isPairFromSameCompany());
			boolean pairNewDevConform = isPairConform(trackPairOneDayBack, getFilteredDevs(availableDevs, developer -> developer.getNew()), isPairWithMixedExpirience());
			logger.info("Rotation time for longest dev is : " + pairForTwoDays);
			logger.info("Rotation time for DoDConform is : " + !pairDoDConform);
			return trackPairOneDayBack != null && (pairForTwoDays || !pairDoDConform || !pairNewDevConform);			
		}
		return false;
	}

	private boolean isPairForTwoDays(Pair trackPairOneDayBack, Pair trackPairTwoDaysBack) {
		return trackPairOneDayBack.equals(trackPairTwoDaysBack);
	}
	
	private Pair getPastPairByTrack(List<DayPairs> pastPairs, String track, int numberOfDaysBack){
		if (numberOfDaysBack < pastPairs.size()) {
			return pastPairs.get(numberOfDaysBack).getPairByTrack(track);
		}
		return null;
	}
	
	private Pair getPairByWeight(Pair pairCandidate, List<Developer> availableDevs, Map<Pair, Integer> pairsWeight) {
		Pair result = null;
		if(pairCandidate.getDevs().isEmpty()){
			result = getPairWithSmallestWeight(availableDevs, pairsWeight);
		}else if(pairCandidate.getDevs().size() == 1){
			result = findPairForDevByPairingWeight(pairCandidate.getDevs().get(0), availableDevs, pairsWeight);
		}
		return result;
	}
	
	private Pair getPairWithSmallestWeight(List<Developer> availableDevs, Map<Pair, Integer> pairsWeight) {
		int smallestWeight = 0;
		Pair result = null;
		for (Pair pair : pairsWeight.keySet()) {
			if(availableDevs.containsAll(pair.getDevs())){
				Integer weight = pairsWeight.get(pair);
				if(weight < smallestWeight || result == null){
					result = pair;
					smallestWeight = weight;
				}				
			}
		}
		return result;
	}
	
	private Pair findPairForDevByPairingWeight(Developer dev, List<Developer> availableDevs, Map<Pair,Integer> pairsWeight) {
		int initialWeight = 0;
		Developer otherDev = null;
		for (Pair pair : pairsWeight.keySet()) {
			Developer ohterPair = pair.getOtherDev(dev);
			if(pair.hasDev(dev) && availableDevs.contains(ohterPair)){
				Integer weight = pairsWeight.get(pair);
				if(weight < initialWeight || otherDev == null){
					otherDev = ohterPair; 
					initialWeight = weight;
				}	
			}
		}
		return new Pair(Arrays.asList(dev, otherDev));
	}
	
	private Developer getLongestDevOnStory(Pair firstDayPair, Pair thirdDayPair) {
		ArrayList<Developer> devsOnTrack = new ArrayList<Developer>();
		devsOnTrack.addAll(firstDayPair.getDevs());
		devsOnTrack.removeAll(thirdDayPair.getDevs());
		return devsOnTrack.isEmpty() || devsOnTrack.size() == 2 ? getRandomDev(firstDayPair.getDevs()) : devsOnTrack.get(0);
	}

	private List<Developer> getAvailableDevs(final List<Developer> availableDevs, List<Developer> pairDevs) {
		ArrayList<Developer> possibleDevs = new ArrayList<Developer>(pairDevs);
		possibleDevs.retainAll(availableDevs);
		return possibleDevs;
	}
	
	private Developer getRandomDev(List<Developer> devs) {
		Collections.shuffle(devs);
		Developer dev = devs.isEmpty() ? null : devs.get(0);
		return dev;
	}
	
	private Developer getDeveloperById(List<Developer> devs, Developer developerToCompare){
		for (Developer developer : devs) {
			if(developer.equals(developerToCompare)){
				return developer;
			}
		}
		return null;
	}

	public void setBuildPair(Collection<Pair> pairs, Map<Pair, Integer> buildPairsWeight) {
		Pair buildPairCandidate = null;
		int smallestWeight = 0;
		for (Pair pair : pairs) {
			Integer weight = buildPairsWeight.get(pair);
			if(weight == null){
				continue;
			}
			
			if(weight < smallestWeight || buildPairCandidate == null){
				buildPairCandidate = pair;
				smallestWeight = weight;
			}
		}
		buildPairCandidate.setBuildPair(true);
	}
}

