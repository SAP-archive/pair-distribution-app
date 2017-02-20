package pair.rotation.app.trello;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.TList;

import pair.rotation.app.persistence.mongodb.TrelloPairsRepository;

public class DayPairsHelper {
	
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM-dd-yyyy");
    private static final Logger logger = LoggerFactory.getLogger(DayPairsHelper.class);
	
	private PairingBoard pairingBoardTrello;

	private TrelloPairsRepository repository;

	public DayPairsHelper(TrelloPairsRepository repository, PairingBoard paringBoardTrello) {
		this.repository = repository;
		this.pairingBoardTrello = paringBoardTrello;
	}

	public List<String> getTracks() {
		return getListCardNames(getListByTitle("tracks"));
	}

	public List<String> getDevs() {
		return getListCardNames(getListByTitle("devs"));
	}

	public List<DayPairs> getPairs() {
		List<DayPairs> result = new ArrayList<>();
		for (TList tList : getPairingLists()) {
			try {
				result.add(getPairsFromList(tList));
			} catch (ParseException e) {
				//TODO
				e.printStackTrace();
			}
		}
		return result;
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
	
	public Map<Pair, Integer> buildPairsWeight(List<DayPairs> pastPairs, List<String> availableDevs) {
		Map<Pair, Integer> result = new HashMap<>(); 
		List<Pair> possiblePairs = getPossiblePairs(availableDevs);
		for (Pair pair : possiblePairs) {
			result.put(pair, 0);
		}
		for (DayPairs dayPairs : pastPairs) {
			for (Pair pair : dayPairs.getPairs().values()) {
				if(pair.isComplete()){
					int weight = result.get(pair) == null ? 1 : result.get(pair) + 1; 
					result.put(pair, weight);
				}
			}
		}
		return result;
	}
	
	private List<Pair> getPossiblePairs(List<String> availableDevs) {
		List<Pair> result = new ArrayList<>();
		if(availableDevs.isEmpty() || availableDevs.size() == 1){
			return result;
		}
		for(int i = 1; i < availableDevs.size(); i++){
			result.add(new Pair(Arrays.asList(availableDevs.get(0), availableDevs.get(i))));
		}
		result.addAll(getPossiblePairs(availableDevs.subList(1, availableDevs.size())));
		return result;
	}

	public DayPairs generateNewDayPairs(List<String> tracks, List<String> devs, List<DayPairs> pastPairs,
			Map<Pair, Integer> pairsWeight) {
		DayPairs result = new DayPairs();
		sortByDescendDate(pastPairs);
		List<String> possibleTracks = getPossibleTracks(tracks, devs);
		List<String> availableDevs = new ArrayList<String>(devs);
		for (String track : possibleTracks) {
			Pair pair = tryToFindPair(track, pastPairs, availableDevs);
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
		if(soloPair != null){
			DayPairs firstDayPair = pastPairs.size() > 0 ? pastPairs.get(0) : null;
			DayPairs secondDayPair = pastPairs.size() > 1 ? pastPairs.get(1) : null;
			if (firstDayPair!= null && secondDayPair != null && firstDayPair.hasPair(soloPair) && secondDayPair.hasPair(soloPair)){
				Pair pairWithHighestWeight = getPairWithHighestPairWeight(todayPairs.getPairs().values(), pairsWeight);
				String track = todayPairs.getTrackByPair(pairWithHighestWeight);
				String longestDevOnStory = getLongestDevOnStory(firstDayPair.getPairByTrack(track), secondDayPair.getPairByTrack(track));
				String newSoloPair = pairWithHighestWeight.getOtherDev(longestDevOnStory);
				Pair newPair = new Pair(Arrays.asList(soloPair.getDevs().get(0), longestDevOnStory));
				todayPairs.replacePairWith(pairWithHighestWeight, newPair);
				todayPairs.replacePairWith(soloPair, new Pair(Arrays.asList(newSoloPair)));
			}
				
		}
	}
	
	private Pair getPairWithHighestPairWeight(Collection<Pair> values, Map<Pair, Integer> pairsWeight) {
		int highesttWeight = 0;
		Pair result = null;
		for (Pair pair : values) {
			if(pairsWeight.containsKey(pair)){
				Integer weight = pairsWeight.get(pair);
				if(highesttWeight < weight || result == null){
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
	
	private ArrayList<String> getListCardNames(TList tracksList) {
		ArrayList<String> result = new ArrayList<String>();
		for (Card card : tracksList.getCards()) {
			result.add(card.getName());
		}
		return result;
	}
	
	private List<String> getPossibleTracks(List<String> todaysTracks, List<String> todaysDevs){
		int possibleTracksCount = (int) Math.ceil(todaysDevs.size() / 2.0);
		List<String> possibleTracks;
		if (possibleTracksCount < todaysTracks.size()){
			possibleTracks = todaysTracks.subList(0, possibleTracksCount);
		} else {
			possibleTracks = todaysTracks;
		}
		return possibleTracks;
	}
	
	private Pair tryToFindPair(String track, List<DayPairs> pastPairs, final List<String> availableDevs) {
		Pair trackPairToday = new Pair();
		Pair trackPairOneDayBack = getPastPairByTrack(pastPairs, track, 0);
		Pair trackPairTwoDaysBack = getPastPairByTrack(pastPairs, track, 1);
		Pair trackPairThreeDaysBack = getPastPairByTrack(pastPairs, track, 2);

		logger.info("Track is: " + track);
		logger.info("Pair one day back: " + trackPairOneDayBack);
		logger.info("Pair two days back: " + trackPairTwoDaysBack);
		logger.info("Pair three days back: " + trackPairThreeDaysBack);
		
		if(isRotationTime(trackPairOneDayBack, trackPairTwoDaysBack)) {
			logger.info("time to rotate");
			if(trackPairOneDayBack.isSolo()){
				logger.info("Solo don't do anything");
				// dev should stay on track
//				trackPairToday.setDevs(trackPairOneDayBack.getDevs());
			}else if(hasHistoryForLongestDev(trackPairThreeDaysBack)){
				logger.info("There is history to find longest dev");
				String longestDevOnStory = getLongestDevOnStory(trackPairOneDayBack, trackPairThreeDaysBack);
				if (longestDevOnStory != null && availableDevs.contains(longestDevOnStory)) {
					logger.info("Longest dev is" + longestDevOnStory);
					trackPairToday.addDev(longestDevOnStory);
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

	private boolean isRotationTime(Pair trackPairOneDayBack, Pair trackPairTwoDaysBack) {
		return trackPairOneDayBack != null && trackPairOneDayBack.equals(trackPairTwoDaysBack);
	}
	
	private Pair getPastPairByTrack(List<DayPairs> pastPairs, String track, int numberOfDaysBack){
		if (numberOfDaysBack < pastPairs.size()) {
			return pastPairs.get(numberOfDaysBack).getPairByTrack(track);
		}
		return null;
	}
	
	private Pair getPairByWeight(Pair pairCandidate, List<String> availableDevs, Map<Pair, Integer> pairsWeight) {
		Pair result = null;
		if(pairCandidate.getDevs().isEmpty()){
			result = getPairWithSmallestWeight(availableDevs, pairsWeight);
		}else if(pairCandidate.getDevs().size() == 1){
			result = findPairForDevByPairingWeight(pairCandidate.getDevs().get(0), availableDevs, pairsWeight);
		}
		return result;
	}
	
	private Pair getPairWithSmallestWeight(List<String> availableDevs, Map<Pair, Integer> pairsWeight) {
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
	
	private Pair findPairForDevByPairingWeight(String dev, List<String> availableDevs, Map<Pair,Integer> pairsWeight) {
		int smallestWeight = 0;
		String otherDev = null;
		for (Pair pair : pairsWeight.keySet()) {
			String ohterPair = pair.getOtherDev(dev);
			if(pair.hasDev(dev) && availableDevs.contains(ohterPair)){
				Integer weight = pairsWeight.get(pair);
				if(weight < smallestWeight || otherDev == null){
					otherDev = ohterPair; 
					smallestWeight = weight;
				}	
			}
		}
		return new Pair(Arrays.asList(dev, otherDev));
	}
	
	private String getLongestDevOnStory(Pair firstDayPair, Pair thirdDayPair) {
		ArrayList<String> devsOnTrack = new ArrayList<String>();
		devsOnTrack.addAll(firstDayPair.getDevs());
		devsOnTrack.removeAll(thirdDayPair.getDevs());
		return devsOnTrack.isEmpty() || devsOnTrack.size() == 2 ? getRandomDev(firstDayPair.getDevs()) : devsOnTrack.get(0);
	}

	private List<String> getAvailableDevs(final List<String> availableDevs, List<String> pairDevs) {
		ArrayList<String> possibleDevs = new ArrayList<String>(pairDevs);
		possibleDevs.retainAll(availableDevs);
		return possibleDevs;
	}
	
	private String getRandomDev(List<String> devs) {
		Collections.shuffle(devs);
		String dev = devs.isEmpty() ? null : devs.get(0);
		return dev;
	}
	
    private TList getListByTitle(String title){
    	for (TList list : pairingBoardTrello.getLits()) {
    		if (title.equals(list.getName().toLowerCase())){
    			return list;
    		}
		}
    	return null;
    }
    
    private List<TList> getPairingLists(){
    	List<TList> result = new ArrayList<TList>();
    	for (TList list : pairingBoardTrello.getLits()) {
    		if (list.getName().toLowerCase().startsWith("pairing")){
    			result.add(list);
    		}
		}
    	return result;
    }
    
	private Date getDateFromCradName(String name) throws ParseException{
		String date = name.substring(name.indexOf("(") + 1, name.lastIndexOf(")"));
		return DATE_FORMATTER.parse(date);
	}
	
   private DayPairs getPairsFromList(TList tList) throws ParseException{
		DayPairs pairs = new DayPairs();
		pairs.setDate(getDateFromCradName(tList.getName()));
		for (Card card : tList.getCards()) {
			Pair pair = new Pair();
			pair.setDevs(card.getIdMembers());
			pairs.addPair(card.getName(), pair);
			System.out.println(card.getName());
			System.out.println(card.getDesc());
		}
		return pairs;
   }
}
