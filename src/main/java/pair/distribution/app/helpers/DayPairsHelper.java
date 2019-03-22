package pair.distribution.app.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pair.distribution.app.persistence.mongodb.TrelloPairsRepository;
import pair.distribution.app.trello.entities.Company;
import pair.distribution.app.trello.entities.DayPairs;
import pair.distribution.app.trello.entities.Developer;
import pair.distribution.app.trello.entities.Pair;
import pair.distribution.app.trello.entities.PairCombinations;

public class DayPairsHelper {

	private static final int THRE_DAYS_BACK = 2;

	private static final int TWO_DAYS_BACK = 1;

	private static final int ONE_DAYS_BACK = 0;

	private static final Logger logger = LoggerFactory.getLogger(DayPairsHelper.class);

	private TrelloPairsRepository repository;
	private boolean everydayRotationMode;

	public DayPairsHelper(TrelloPairsRepository repository, boolean everydayRotationMode) {
		this.repository = repository;
		this.everydayRotationMode = everydayRotationMode;
	}

	public void updateDataBaseWithTrelloContent(List<DayPairs> pairs) {
		removeTodayFromDatabase();
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
				logger.info("More than one entry found. They are: {}", findByDate);
				throw new RuntimeException();
			}
		}
	}

	private void removeTodayFromDatabase() {
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.clear(Calendar.MINUTE);
		today.clear(Calendar.SECOND);
		today.clear(Calendar.MILLISECOND);
		Date todayDate = today.getTime();
		List<DayPairs> findByDate = repository.findByDate(todayDate);
		if (findByDate.size() > 0) {
			logger.info("Found pairs for today, removing");
			repository.deleteByDate(todayDate);
		} else {
			logger.info("No pairs found for today.");
		}
	}

	public Map<Pair, Integer> buildPairsWeightFromPastPairing(PairCombinations pastPairs,
			List<Developer> availableDevs) {
		return buildPairsWeghtFromPredicate(pastPairs, availableDevs, Pair::isComplete);
	}

	public Map<Pair, Integer> buildBuildPairsWeightFromPastPairing(PairCombinations pastPairs,
			List<Developer> availableDevs) {
		return buildPairsWeghtFromPredicate(pastPairs, availableDevs, Pair::isBuildPair);
	}

	public Map<Pair, Integer> buildCommunityPairsWeightFromPastPairing(PairCombinations pastPairs,
			List<Developer> availableDevs) {
		return buildPairsWeghtFromPredicate(pastPairs, availableDevs, Pair::isCommunityPair);
	}

	private Map<Pair, Integer> buildPairsWeghtFromPredicate(PairCombinations pairCombinations,
			List<Developer> availableDevs, Predicate<? super Pair> filter) {
		final Map<Pair, Integer> result = new HashMap<>();
		initPairsInitialWeight(availableDevs, result, false);
		pairCombinations.getPairs().stream().filter(filter::test).forEach(pair -> result.put(pair,
				Integer.valueOf(result.getOrDefault(pair, Integer.valueOf(0)).intValue() + 1)));
		return result;
	}

	public void buildDevelopersPairingDays(PairCombinations pastPairs, List<Developer> todayDevs) {
		todayDevs.stream().forEach(developer -> pastPairs.getPairs().stream().filter(pair -> pair.hasDev(developer))
				.forEach(pair -> developer.udpatePairingDays()));
	}

	public void buildDevelopersTracksWeightFromPastPairing(PairCombinations pastPairs,
			List<Developer> availableDevs) {
		availableDevs.stream()
				.forEach(developer -> pastPairs.getPairs().stream().filter(pair -> pair.hasDev(developer))
						.forEach(pair -> developer.updateTrackWeight(pair.getTrack())));
	}
	
	public void adaptPairsWeight(Map<Pair, Integer> pairsWeight, List<Developer> availableDevs) {
		List<Developer> newDevelopers = getFilteredDevs(availableDevs, Developer::getNew);
		pairsWeight.keySet().stream().filter(pair -> !isPairConform(pair, newDevelopers, isPairWithMixedExpirience()))
				.forEach(pair -> {
					logger.info("pair with new Developers and needs adaptation. Pair is: {}", pair);
					addUnconformWeight(pairsWeight, pair);
				});
	}

	private void addUnconformWeight(Map<Pair, Integer> pairsWeight, Pair pair) {
		pairsWeight.put(pair, Integer.valueOf(pairsWeight.get(pair).intValue() + 100));
	}

	private boolean isPairConform(Pair pair, List<Developer> predicateDevelopers, Predicate<? super Pair> predicate) {
		switch (pair.getDevs().size()) {
		case 0:
			return true;
		case 1:
			return !predicateDevelopers.contains(pair.getFirstDev());
		default:
			return predicateDevelopers.contains(pair.getFirstDev()) || predicateDevelopers.contains(pair.getSecondDev())
					? predicate.test(pair)
					: true;
		}
	}

	private List<Developer> getFilteredDevs(List<Developer> availableDevs, Predicate<? super Developer> predicate) {
		return availableDevs.stream().filter(predicate).collect(Collectors.toList());
	}

	private void initPairsInitialWeight(List<Developer> availableDevs, Map<Pair, Integer> result,
			boolean addSoloPairs) {
		getAllPairCombinations(availableDevs, addSoloPairs).stream()
				.forEach(pair -> result.put(pair, Integer.valueOf(0)));
	}

	private List<Pair> getAllPairCombinations(List<Developer> availableDevs, boolean addSoloPairs) {
		List<Pair> result = new ArrayList<>();
		if (availableDevs.isEmpty() || (availableDevs.size() == 1 && !addSoloPairs)) {
			return result;
		} else if (availableDevs.size() == 1 && addSoloPairs) {
			result.add(new Pair(Arrays.asList(availableDevs.get(0))));
			return result;
		}

		for (int i = 1; i < availableDevs.size(); i++) {
			result.add(new Pair(Arrays.asList(availableDevs.get(0), availableDevs.get(i))));
		}
		result.addAll(getAllPairCombinations(availableDevs.subList(1, availableDevs.size()), addSoloPairs));
		return result;
	}

	public DayPairs generateNewDayPairs(List<String> tracks, List<Developer> devs, PairCombinations pairCombination,
			Map<Pair, Integer> pairsWeight, List<Company> companies) {
		DayPairs result = new DayPairs();
		List<String> possibleTracks = getPossibleTracks(tracks, devs, companies);
		List<Developer> availableDevs = new ArrayList<>(devs);
		boolean rotationTime = pairCombination.isRotationTime(possibleTracks, availableDevs, everydayRotationMode);

		for (String track : possibleTracks) {
			List<Developer> developersForTrack = getDevelopersForTrack(companies, availableDevs, track);
			Pair pair = tryToFindPairFirstDeveloper(track, pairCombination, developersForTrack, rotationTime);
			availableDevs.removeAll(pair.getDevs());
			result.addPair(track, pair);

			// Try to fill company specific tracks here, because they have higher priority
			if (getCompanyForTrack(companies, track) != null) {
				Pair fullPair = tryToFindPairSecondDeveloper(pairsWeight, result, developersForTrack, track);
				if (fullPair != null) {
					availableDevs.removeAll(fullPair.getDevs());
					result.addPair(track, fullPair);
				}
			}
		}

		for (String track : possibleTracks) {
			// Fill non company tracks
			if (getCompanyForTrack(companies, track) == null) {
				Pair fullPair = tryToFindPairSecondDeveloper(pairsWeight, result, availableDevs, track);
				if (fullPair != null) {
					availableDevs.removeAll(fullPair.getDevs());
					result.addPair(track, fullPair);
				}
			}
		}

		return result;
	}

	private List<Developer> getDevelopersForTrack(List<Company> companies, List<Developer> availableDevs,
			String track) {
		Company companyWithTrack = getCompanyForTrack(companies, track);
		return companyWithTrack == null ? availableDevs : companyWithTrack.getDevs(availableDevs);
	}

	private Company getCompanyForTrack(List<Company> companies, String track) {
		return companies.stream().filter(company -> company.isCompanyTrack(track)).findFirst().orElse(null);
	}

	private Pair tryToFindPairSecondDeveloper(Map<Pair, Integer> pairsWeight, DayPairs result,
			List<Developer> availableDevs, String track) {
		Pair pair = result.getPairs().get(track);
		if (!pair.isComplete() && !availableDevs.isEmpty()) {
			pair = getPairByWeight(pair, availableDevs, pairsWeight, track);
			if (pair == null && availableDevs.size() == 1) {
				pair = new Pair(availableDevs);
			}

			return pair;
		}
		return null;
	}

	public void rotateSoloPairIfAny(DayPairs todayPairs, PairCombinations pairCombination,
			Map<Pair, Integer> pairsWeight) {
		Pair soloPair = todayPairs.getSoloPair();
		if (soloPair != null && (isSoloPairForTwoDays(pairCombination, soloPair) || soloPair.getFirstDev().getNew())) {
			Developer soloDeveloper = soloPair.getFirstDev();
			Pair pairWithHighestWeight = null;
			Developer newSoloDeveloper = null;
			Collection<Pair> allPairsOfTheDay = todayPairs.getPairs().values();
			if (soloDeveloper.getNew()) {
				pairWithHighestWeight = getPairWithHighestWeightForPredicat(allPairsOfTheDay, pairsWeight,
						isPairWithNoNewDevelopers());
				newSoloDeveloper = getNewSoloDeveloper(pairWithHighestWeight);
			} else {
				pairWithHighestWeight = getPairWithHighestWeightForPredicat(allPairsOfTheDay, pairsWeight,
						alwaysTrue());
				newSoloDeveloper = getDevWithContextOrRandom(pairWithHighestWeight.getDevs());
			}
			if (newSoloDeveloper == null) {
				// No rotation for Solo developer possible
				return;
			}
			Pair newPair = new Pair(Arrays.asList(soloPair.getDevs().get(0), newSoloDeveloper));
			todayPairs.replacePairWith(pairWithHighestWeight, newPair);
			todayPairs.replacePairWith(soloPair,
					new Pair(Arrays.asList(pairWithHighestWeight.getOtherDev(newSoloDeveloper))));
		}
	}

	public List<String> getPossibleTracks(List<String> todaysTracks, List<Developer> todaysDevs,
			List<Company> companies) {
		int possibleTracksCount = (int) Math.ceil(todaysDevs.size() / 2.0);
		List<String> possibleTracks = todaysTracks.size() > possibleTracksCount
				? todaysTracks.subList(0, possibleTracksCount)
				: todaysTracks;
		for (Company company : companies) {
			String companyTrack = company.getCompanyTrack(possibleTracks);
			List<Developer> companyDevs = company.getDevs(todaysDevs);
			if (companyDevs.size() <= 1 && companyTrack != null) {
				throw new RuntimeException("Company '" + company.getName() + "' has no devs for its tracks");
			}
		}

		return possibleTracks;
	}

	private Developer getNewSoloDeveloper(Pair pairWithHighestWeight) {
		return pairWithHighestWeight == null ? null : getDevWithContextOrRandom(pairWithHighestWeight.getDevs());
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
		return firstDayPair != null && secondDayPair != null && firstDayPair.contains(soloPair)
				&& secondDayPair.contains(soloPair);
	}

	private Predicate<? super Pair> alwaysTrue() {
		return p -> true;
	}

	private Pair getPairWithHighestWeightForPredicat(Collection<Pair> values, Map<Pair, Integer> pairsWeight,
			Predicate<? super Pair> predicate) {
		return values.stream().filter(pairsWeight::containsKey).filter(predicate::test)
				.max(Comparator.comparing(pairsWeight::get)).orElse(null);
	}

	private Pair tryToFindPairFirstDeveloper(String track, PairCombinations pairCombination,
			final List<Developer> availableDevs, boolean rotationRequired) {
		Pair trackPairToday = new Pair();
		Pair trackPairOneDayBack = pairCombination.getPastPairByTrack(ONE_DAYS_BACK, track);
		logger.info("Track is: {}\nPair one day back: {}\nPair two days back: {}\nPair three days back: {}", track,
				trackPairOneDayBack, pairCombination.getPastPairByTrack(TWO_DAYS_BACK, track), pairCombination.getPastPairByTrack(THRE_DAYS_BACK, track));
		if (rotationRequired) {
			findFirstDeveloper(availableDevs, trackPairToday, pairCombination, track);
		} else if (trackPairOneDayBack != null) {
			logger.info("No rotation required");
			trackPairToday.setDevs(getAvailableDevs(availableDevs, trackPairOneDayBack.getDevs()));
		}
		return trackPairToday;
	}

	private void findFirstDeveloper(final List<Developer> availableDevs, Pair trackPairToday, PairCombinations pairCombination, String track) {
		logger.info("time to rotate");
		Pair trackPairOneDayBack = pairCombination.getPastPairByTrack(ONE_DAYS_BACK, track);
		if (trackPairOneDayBack == null || getAvailablePastDevsForTrack(availableDevs, trackPairOneDayBack).isEmpty()) {
			logger.info("No history or both past Devs are unavailable. Add one dev random from available devs");
			trackPairToday.addDev(getDevWithContextOrRandom(availableDevs));
		} else if (trackPairOneDayBack.isSolo()) {
			logger.info("Solo dev should stay on track. Don't do anything");
		} else if (getAvailablePastDevsForTrack(availableDevs, trackPairOneDayBack).size() == 2) {
			if (trackPairOneDayBack.isLockedPair()) {
				logger.info("Pair is locked: {}", trackPairOneDayBack);
				trackPairToday.setDevs(getAvailableDevs(availableDevs, trackPairOneDayBack.getDevs()));
			} else if (hasHistoryForLongestDev(pairCombination, track) && getLongestDevOnStory(pairCombination, track) != null) {
				logger.info("There is history to find longest dev");
				rotateLongestDev(availableDevs, trackPairToday, pairCombination, track);
			} else {
				logger.info("No enough history for longest dev. Add one dev random");
				Developer devWithContextOrRandom = getDevWithContextOrRandom(getAvailableDevs(availableDevs, trackPairOneDayBack.getDevs()));
				devWithContextOrRandom.setHasContext(true);
				trackPairToday.addDev(devWithContextOrRandom);
			}
		}else {
			Developer availablePastDev = getAvailablePastDevsForTrack(availableDevs, trackPairOneDayBack).get(0);
			logger.info("Only one past Dev available {}", availablePastDev);
			availablePastDev.setHasContext(true);
			trackPairToday.addDev(availablePastDev);
		}
	}

	private List<Developer> getAvailablePastDevsForTrack(List<Developer> availableDevs, Pair trackPairOneDayBack) {
		List<Developer> availablePastDevsForTrack = new ArrayList<Developer>();
		if (getDeveloperById(availableDevs, trackPairOneDayBack.getFirstDev()) != null) {
			availablePastDevsForTrack.add(getDeveloperById(availableDevs, trackPairOneDayBack.getFirstDev()));
		}
		if (getDeveloperById(availableDevs, trackPairOneDayBack.getSecondDev()) != null) {
			availablePastDevsForTrack.add(getDeveloperById(availableDevs, trackPairOneDayBack.getSecondDev()));
		}
		return availablePastDevsForTrack;
	}

	private void rotateLongestDev(final List<Developer> availableDevs, Pair trackPairToday, PairCombinations pairCombination, String track) {
		Developer longestDevOnStory = getLongestDevOnStory(pairCombination, track);
		Developer devToRotate = getDeveloperById(availableDevs, longestDevOnStory);
		logger.info("Longest dev is {}", devToRotate);
		Developer devToStay = getDeveloperById(availableDevs, pairCombination.getPastPairByTrack(ONE_DAYS_BACK, track).getOtherDev(longestDevOnStory));
		logger.info("Dev with context to stay is {}", devToStay);
		devToStay.setHasContext(true);
		trackPairToday.addDev(devToStay);
	}

	private boolean hasHistoryForLongestDev(PairCombinations pairCombination, String track) {
		Pair trackPairOneDaysBack = pairCombination.getPastPairByTrack(ONE_DAYS_BACK, track);
		Pair trackPairTwoDaysBack = pairCombination.getPastPairByTrack(TWO_DAYS_BACK, track);
		if (everydayRotationMode) {
			return trackPairOneDaysBack != null && trackPairTwoDaysBack != null;
		}
			
		return trackPairOneDaysBack != null && trackPairTwoDaysBack != null && pairCombination.getPastPairByTrack(THRE_DAYS_BACK, track) != null;
	}

	private Pair getPairByWeight(Pair pairCandidate, List<Developer> availableDevs, Map<Pair, Integer> pairsWeight, String track) {
		Pair result = null;
		if (pairCandidate.getDevs().isEmpty()) {
			result = getPairWithSmallestWeight(availableDevs, pairsWeight);
		} else if (pairCandidate.getDevs().size() == 1) {
			result = findPairForDevByPairingWeight(pairCandidate.getDevs().get(0), availableDevs, pairsWeight, track);
		}
		return result;
	}

	private Pair getPairWithSmallestWeight(List<Developer> availableDevs, Map<Pair, Integer> pairsWeight) {
		return pairsWeight.keySet().stream().filter(pair -> availableDevs.containsAll(pair.getDevs()))
				.min(Comparator.comparing(pairsWeight::get)).orElse(null);
	}

	private Pair findPairForDevByPairingWeight(Developer pairFirstDeveloper, List<Developer> availableDevs,
			Map<Pair, Integer> pairsWeight, String track) {
		Developer otherDev = pairsWeight.keySet().stream().filter(pair -> pair.hasDev(pairFirstDeveloper))
				.filter(pair -> availableDevs.contains(pair.getOtherDev(pairFirstDeveloper)))
				.min(Comparator.comparing(pair -> getWeightRelativeToPairingDays(pair, pairFirstDeveloper,
						pairsWeight, availableDevs, track)))
				.map(pair -> pair.getOtherDev(pairFirstDeveloper)).orElse(null);
		return new Pair(Arrays.asList(pairFirstDeveloper, otherDev));
	}

	private float getWeightRelativeToPairingDays(Pair pair, Developer pairFirstDeveloper,
			Map<Pair, Integer> pairsWeight, List<Developer> availableDevs, String track) {
		Developer pairOtherDev = getDeveloperById(availableDevs, pair.getOtherDev(pairFirstDeveloper));
		if (pairOtherDev != null) {
			float devPairWeightRelativeToPairingDays =  pairOtherDev.getPairingDays() > 0 ? (float) pairsWeight.get(pair) / pairOtherDev.getPairingDays() : 0;
			float devTrackWeightRelativeToPairingDays =  pairOtherDev.getTrackWeight(track) > 0 ? (float)  pairOtherDev.getTrackWeight(track) / pairOtherDev.getPairingDays() : 0;
			
			return devPairWeightRelativeToPairingDays + devTrackWeightRelativeToPairingDays;
		} else {
			return pairsWeight.get(pair);
		}
	}

	private Developer getLongestDevOnStory(PairCombinations pairCombination, String track) {
		ArrayList<Developer> devsOnTrack = new ArrayList<>();
		Pair lastDayPair = pairCombination.getPastPairByTrack(ONE_DAYS_BACK, track);
		devsOnTrack.addAll(lastDayPair.getDevs());
		int daysBackToConsider = this.everydayRotationMode ? 2 : 3;
		for(int daysBack = 1; daysBack <= daysBackToConsider; daysBack++) {
			Pair pastPairByTrack = pairCombination.getPastPairByTrack(daysBack, track);
			if(pastPairByTrack != null) {				
				devsOnTrack.retainAll(pastPairByTrack.getDevs());
			}
		}

		return devsOnTrack.isEmpty() || devsOnTrack.size() == 2 ? getDevWithContextOrRandom(lastDayPair.getDevs())
				: devsOnTrack.get(0);
	}

	private List<Developer> getAvailableDevs(final List<Developer> availableDevs, List<Developer> pairDevs) {
		ArrayList<Developer> possibleDevs = new ArrayList<>(pairDevs);
		possibleDevs.retainAll(availableDevs);
		return possibleDevs;
	}

	private Developer getDevWithContextOrRandom(List<Developer> devs) {
		if (devs.isEmpty()) {
			return null;
		} else {
			if (devs.size() == 1) {
				return devs.get(0);
			}else if (devs.get(0).hasContext()) {
				return devs.get(0);
			} else if(devs.size() > 1 && devs.get(1).hasContext()) {
				return devs.get(1);
			} else {
				Collections.shuffle(devs);
				return devs.get(0);
			}
		}
	}

	private Developer getDeveloperById(List<Developer> devs, Developer developerToCompare) {
		return devs.stream().filter(developer -> developer.equals(developerToCompare)).findFirst().orElse(null);
	}

	public void setBuildPair(Collection<Pair> pairs, Map<Pair, Integer> buildPairsWeight) {
		List<Pair> pairsAsList = pairs.stream().collect(Collectors.toList());
		getPairWithMinWeightValue(pairsAsList, buildPairsWeight, pair -> true).setBuildPair(true);
	}

	public void setCommunityPair(Collection<Pair> pairs, Map<Pair, Integer> communityPairsWeight) {
		List<Pair> pairsAsList = pairs.stream().collect(Collectors.toList());
		getPairWithMinWeightValue(pairsAsList, communityPairsWeight, pair -> !pair.isBuildPair())
				.setCommunityPair(true);

	}

	private Pair getPairWithMinWeightValue(List<Pair> pairs, Map<Pair, Integer> pairsWeight,
			Predicate<? super Pair> skipPair) {
		return pairs.stream().filter(pair -> pairsWeight.get(pair) != null).filter(skipPair)
				.min(Comparator.comparing(pairsWeight::get))
				.orElseGet(() -> pairs.get(new Random().nextInt(pairs.size())));
	}
}
