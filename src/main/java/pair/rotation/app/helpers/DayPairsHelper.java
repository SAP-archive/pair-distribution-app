package pair.rotation.app.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pair.rotation.app.persistence.mongodb.TrelloPairsRepository;
import pair.rotation.app.trello.entities.Company;
import pair.rotation.app.trello.entities.DayPairs;
import pair.rotation.app.trello.entities.Developer;
import pair.rotation.app.trello.entities.Pair;
import pair.rotation.app.trello.entities.PairCombinations;

public class DayPairsHelper {
	
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
				logger.info("Repository state: {}", findByDate.get(0));
				logger.info("Board state: {}", pairsByDate);
				repository.deleteByDate(pairsByDate.getDate());
				repository.save(pairsByDate);
				logger.info("Update finished");
			} else if (findByDate.isEmpty()) {
				logger.info("No update found. Adding to repository");
				repository.save(pairsByDate);
				logger.info("Adding finished");
			} else {
				logger.info("More than one entry found. They are: {}",  findByDate);
				throw new RuntimeException();
			}
		}
	}
	
	public Map<Pair, Integer> buildPairsWeightFromPastPairing(PairCombinations pastPairs, List<Developer> availableDevs) {
		return buildPairsWeghtFromPredicate(pastPairs, availableDevs, Pair::isComplete);
	}

	public Map<Pair, Integer> buildBuildPairsWeightFromPastPairing(PairCombinations pastPairs, List<Developer> availableDevs) {
		return buildPairsWeghtFromPredicate(pastPairs, availableDevs, Pair::isBuildPair);
	}

	public Map<Pair, Integer> buildCommunityPairsWeightFromPastPairing(PairCombinations pastPairs, List<Developer> availableDevs) {
		return buildPairsWeghtFromPredicate(pastPairs, availableDevs, Pair::isCommunityPair);
	}
	
	private Map<Pair, Integer> buildPairsWeghtFromPredicate(PairCombinations pairCombinations, List<Developer> availableDevs, Predicate<? super Pair> filter) {
		final Map<Pair, Integer> result = new HashMap<>(); 
		initPairsInitialWeight(availableDevs, result, false);
		pairCombinations.getPairs().stream().filter(filter::test).forEach(pair -> result.put(pair, Integer.valueOf(result.getOrDefault(pair, Integer.valueOf(0)).intValue() + 1)));
		return result;
	}

	
	public void adaptPairsWeight(Map<Pair, Integer> pairsWeight, List<Developer> availableDevs) {
		List<Developer> newDevelopers = getFilteredDevs(availableDevs, Developer::getNew);
		pairsWeight.keySet().stream().filter(pair -> !isPairConform(pair, newDevelopers, isPairWithMixedExpirience()))		
                                     .forEach(pair -> { logger.info("pair with new Developers and needs adaptation. Pair is: {}", pair); addUnconformWeight(pairsWeight, pair); });
	}

	private void addUnconformWeight(Map<Pair, Integer> pairsWeight, Pair pair) {
		pairsWeight.put(pair, Integer.valueOf(pairsWeight.get(pair).intValue() + 100));
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
	
	private List<Developer> getFilteredDevs(List<Developer> availableDevs, Predicate<? super Developer> predicate) {
		return availableDevs.stream().filter(predicate).collect(Collectors.toList());
	}
	
	private void initPairsInitialWeight(List<Developer> availableDevs, Map<Pair, Integer> result, boolean addSoloPairs) {
		getAllPairCombinations(availableDevs, addSoloPairs).stream().forEach(pair -> result.put(pair, Integer.valueOf(0)));
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

	public DayPairs generateNewDayPairs(List<String> tracks, List<Developer> devs, PairCombinations pairCombination,
			Map<Pair, Integer> pairsWeight, boolean rotateEveryday, List<Company> companies) {
		DayPairs result = new DayPairs();
		List<String> possibleTracks = getPossibleTracks(tracks, devs, companies);
		List<Developer> availableDevs = new ArrayList<>(devs);
		boolean rotationTime = pairCombination.isRotationTime(possibleTracks, availableDevs);

		for (String track : possibleTracks) {
			List<Developer> developersForTrack = getDevelopersForTrack(companies, availableDevs, track);
			Pair pair = tryToFindPairFirstDeveloper(track, pairCombination, developersForTrack, rotateEveryday, rotationTime);
			availableDevs.removeAll(pair.getDevs());
			result.addPair(track, pair);
			
			// Try to fill company specific tracks here, because they have higher priority
			if(getCompanyForTrack(companies, track) != null) {
				Pair fullPair = tryToFindPairSecondDeveloper(pairsWeight, result, developersForTrack, track);
				if(fullPair != null){
					availableDevs.removeAll(fullPair.getDevs());
					result.addPair(track, fullPair);					
				}				
			}
		}
		
		for (String track : possibleTracks) {
			// Fill non company tracks 
			if(getCompanyForTrack(companies, track) == null) {
				Pair fullPair = tryToFindPairSecondDeveloper(pairsWeight, result, availableDevs, track);
				if(fullPair != null){
					availableDevs.removeAll(fullPair.getDevs());
					result.addPair(track, fullPair);					
				}				
			}
		}
		
		return result;
	}

	private List<Developer> getDevelopersForTrack(List<Company> companies, List<Developer> availableDevs, String track) {
		Company companyWithTrack = getCompanyForTrack(companies, track);
		return companyWithTrack == null ? availableDevs : companyWithTrack.getDevs(availableDevs);
	}

	private Company getCompanyForTrack(List<Company> companies, String track) {
		return companies.stream().filter(company -> company.isCompanyTrack(track)).findFirst().orElse(null);
	}

	private Pair tryToFindPairSecondDeveloper(Map<Pair, Integer> pairsWeight, DayPairs result, List<Developer> availableDevs,
			String track) {
		Pair pair = result.getPairs().get(track);
		if(!pair.isComplete() && !availableDevs.isEmpty()){
			pair = getPairByWeight(pair, availableDevs, pairsWeight);
			if( pair == null && availableDevs.size() == 1){
				pair = new Pair(availableDevs);
			}
			
			return pair;
		}
		return null;
	}

	public void rotateSoloPairIfAny(DayPairs todayPairs, PairCombinations pairCombination, Map<Pair, Integer> pairsWeight) {
		Pair soloPair = todayPairs.getSoloPair();
		if (soloPair != null && (isSoloPairForTwoDays(pairCombination, soloPair) || soloPair.getFirstDev().getNew())) {
			Developer soloDeveloper = soloPair.getFirstDev();
			Pair pairWithHighestWeight = null;
			Developer newPairForSoloDeveloper = null;
			Collection<Pair> allPairsOfTheDay = todayPairs.getPairs().values();
			if (soloDeveloper.getNew()) {
				pairWithHighestWeight = getPairWithHighestWeightForPredicat(allPairsOfTheDay, pairsWeight, isPairWithNoNewDevelopers());
				newPairForSoloDeveloper = rotateSoloNewDeveloper(pairWithHighestWeight);
			} else {
				pairWithHighestWeight = getPairWithHighestWeightForPredicat(allPairsOfTheDay, pairsWeight, alwaysTrue());
				newPairForSoloDeveloper = getRandomDev(pairWithHighestWeight.getDevs());
			}
			if (newPairForSoloDeveloper == null) {
				// No rotation for Solo developer possible
				return;
			}
			Pair newPair = new Pair(Arrays.asList(soloPair.getDevs().get(0), newPairForSoloDeveloper));
			todayPairs.replacePairWith(pairWithHighestWeight, newPair);
			todayPairs.replacePairWith(soloPair, new Pair(Arrays.asList(pairWithHighestWeight.getOtherDev(newPairForSoloDeveloper))));
		}
	}

	public List<String> getPossibleTracks(List<String> todaysTracks, List<Developer> todaysDevs, List<Company> companies){
		int possibleTracksCount = (int) Math.ceil(todaysDevs.size() / 2.0);
		List<String> possibleTracks = todaysTracks.size() > possibleTracksCount ? todaysTracks.subList(0, possibleTracksCount) : todaysTracks;
		for (Company company : companies) {
			String companyTrack = company.getCompanyTrack(possibleTracks);
			List<Developer> companyDevs = company.getDevs(todaysDevs);
			if (companyDevs.size() <= 1 && companyTrack != null) {
				throw new RuntimeException("Company '" + company.getName() + "' has no devs for its tracks");
			}
		}
		
		return possibleTracks;
	}
	
	private Developer rotateSoloNewDeveloper(Pair pairWithHighestWeight) {
		Developer newPairForSoloDeveloper;
		newPairForSoloDeveloper = pairWithHighestWeight == null ? null : getRandomDev(pairWithHighestWeight.getDevs());
		return newPairForSoloDeveloper;
	}
	private Predicate<? super Pair> isPairWithMixedExpirience() {
		return p -> !(p.getFirstDev().getNew() && p.getOtherDev(p.getFirstDev()).getNew());
	}
	
	private Predicate<? super Pair> isPairWithNoNewDevelopers() {
		return p -> !(p.getFirstDev().getNew() || p.getOtherDev(p.getFirstDev()).getNew());
	}
	
	private boolean isSoloPairForTwoDays(PairCombinations pairCombination, Pair soloPair) {
		List<Pair> firstDayPair = pairCombination.getPastPairs(0);
		List<Pair> secondDayPair = pairCombination.getPastPairs(1);
		return firstDayPair!= null && secondDayPair != null && firstDayPair.contains(soloPair) && secondDayPair.contains(soloPair);
	}
	
	
	private Predicate<? super Pair> alwaysTrue() {
		return p -> true;
	}
	
	private Pair getPairWithHighestWeightForPredicat(Collection<Pair> values, Map<Pair, Integer> pairsWeight, Predicate<? super Pair> predicate) {
		return values.stream().filter(pairsWeight::containsKey)
				              .filter(predicate::test)
				              .max(Comparator.comparing(pairsWeight::get)).orElse(null);
	}
	
	private Pair tryToFindPairFirstDeveloper(String track, PairCombinations pairCombination, final List<Developer> availableDevs, boolean rotateEveryday, boolean rotationRequired) {
		Pair trackPairToday = new Pair();
		Pair trackPairOneDayBack = pairCombination.getPastPairByTrack(0, track);
		Pair trackPairTwoDaysBack = pairCombination.getPastPairByTrack(1, track);
		Pair trackPairThreeDaysBack = pairCombination.getPastPairByTrack(2, track);
		logger.info("Track is: {}\nPair one day back: {}\nPair two days back: {}\nPair three days back: {}", track, trackPairOneDayBack, trackPairTwoDaysBack, trackPairThreeDaysBack);		
		if(rotateEveryday || rotationRequired) {
			findFirstDeveloper(availableDevs, trackPairToday, trackPairOneDayBack, trackPairThreeDaysBack);
		}else if(trackPairOneDayBack != null){
			logger.info("No rotation required");
			trackPairToday.setDevs(getAvailableDevs(availableDevs, trackPairOneDayBack.getDevs()));	
		}
		return trackPairToday;
	}

	private void findFirstDeveloper(final List<Developer> availableDevs, Pair trackPairToday, Pair trackPairOneDayBack,
			Pair trackPairThreeDaysBack) {
		logger.info("time to rotate");
		if(trackPairOneDayBack ==  null){
			logger.info("No history. Add one dev random from available devs");
			trackPairToday.addDev(getRandomDev(availableDevs));
		}else if(trackPairOneDayBack.isSolo()){
			logger.info("Solo dev should stay on track. Don't do anything");
		}else if(hasHistoryForLongestDev(trackPairThreeDaysBack)){
			tryToRotateLongestDev(availableDevs, trackPairToday, trackPairOneDayBack, trackPairThreeDaysBack);
		}else{
			logger.info("No older history. Add one dev random");
			trackPairToday.addDev(getRandomDev(getAvailableDevs(availableDevs, trackPairOneDayBack.getDevs())));
		}
	}

	private void tryToRotateLongestDev(final List<Developer> availableDevs, Pair trackPairToday, Pair trackPairOneDayBack,
			Pair trackPairThreeDaysBack) {
		logger.info("There is history to find longest dev");
		Developer longestDevOnStory = getLongestDevOnStory(trackPairOneDayBack, trackPairThreeDaysBack);
		if (longestDevOnStory != null) {
			Developer longestDev = getDeveloperById(availableDevs, longestDevOnStory);
			logger.info("Longest dev is {}", longestDev);
			Developer devToStay = trackPairOneDayBack.getOtherDev(longestDevOnStory);
			if( devToStay != null && availableDevs.contains(getDeveloperById(availableDevs, devToStay))) {
				logger.info("Dev to stay is {}", getDeveloperById(availableDevs, devToStay));
				trackPairToday.addDev(getDeveloperById(availableDevs, devToStay));
			}else {
				trackPairToday.addDev(longestDev);
			}
		}
	}

	private boolean hasHistoryForLongestDev(Pair trackPairThreeDaysBack) {
		return trackPairThreeDaysBack != null;
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
		return pairsWeight.keySet().stream().filter(pair -> availableDevs.containsAll(pair.getDevs()))
				                            .min(Comparator.comparing(pairsWeight::get)).orElse(null);
	}
	
	private Pair findPairForDevByPairingWeight(Developer dev, List<Developer> availableDevs, Map<Pair,Integer> pairsWeight) {
		Developer otherDev = pairsWeight.keySet().stream().filter(pair -> pair.hasDev(dev))
				                                          .filter(pair -> availableDevs.contains(pair.getOtherDev(dev)))
		                                                  .min(Comparator.comparing(pairsWeight::get))
		                                                  .map(pair -> pair.getOtherDev(dev))
		                                                  .orElse(null);
		return new Pair(Arrays.asList(dev, otherDev));
	}
	
	private Developer getLongestDevOnStory(Pair firstDayPair, Pair thirdDayPair) {
		ArrayList<Developer> devsOnTrack = new ArrayList<>();
		devsOnTrack.addAll(firstDayPair.getDevs());
		devsOnTrack.retainAll(thirdDayPair.getDevs());
		return devsOnTrack.isEmpty() || devsOnTrack.size() == 2 ? getRandomDev(firstDayPair.getDevs()) : devsOnTrack.get(0);
	}

	private List<Developer> getAvailableDevs(final List<Developer> availableDevs, List<Developer> pairDevs) {
		ArrayList<Developer> possibleDevs = new ArrayList<>(pairDevs);
		possibleDevs.retainAll(availableDevs);
		return possibleDevs;
	}
	
	private Developer getRandomDev(List<Developer> devs) {
		Collections.shuffle(devs);
		return devs.isEmpty() ? null : devs.get(0);
	}
	
	private Developer getDeveloperById(List<Developer> devs, Developer developerToCompare){
		return devs.stream().filter(developer -> developer.equals(developerToCompare)).findFirst().orElse(null);
	}

	public void setBuildPair(Collection<Pair> pairs, Map<Pair, Integer> buildPairsWeight) {
		List<Pair> pairsAsList = pairs.stream().collect(Collectors.toList());
		getPairWithMinWeightValue(pairsAsList, buildPairsWeight, pair -> true).setBuildPair(true);
	}

	public void setCommunityPair(Collection<Pair> pairs, Map<Pair, Integer> communityPairsWeight) {
		List<Pair> pairsAsList = pairs.stream().collect(Collectors.toList());
		getPairWithMinWeightValue(pairsAsList, communityPairsWeight, pair -> !pair.isBuildPair()).setCommunityPair(true);
		
	}

	private Pair getPairWithMinWeightValue(List<Pair> pairs, Map<Pair, Integer> pairsWeight, Predicate<? super Pair> skipPair) {
		return pairs.stream().filter(pair -> pairsWeight.get(pair) != null)
		                     .filter(skipPair)
				             .min(Comparator.comparing(pairsWeight::get))
				             .orElseGet(() -> pairs.get(new Random().nextInt(pairs.size())));
	}
}

