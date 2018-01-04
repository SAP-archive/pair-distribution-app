package pair.rotation.app.trello.entities;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class DevPairsCombinationsTest {

	@Test
	public void testGetPairsReturnOnlyDevPairs() {
		List<DayPairs> pairsListFromDevs = getPairsListFromDevs(getStandardDevs());
		Pair opsPair = pairsListFromDevs.get(0).getPairByTrack("track1");
		opsPair.setOpsPair(true);
		
		List<Pair> pairs = new DevPairCombinations(pairsListFromDevs).getPairs();
		
		assertThat(pairs.contains(opsPair), is(false));
		assertThat(pairs.size(), is(5));
	}
	
	@Test
	public void testGetPastPairs() {
		List<Developer> standardDevs = getStandardDevs();
		DevPairCombinations devPairCombinations = new DevPairCombinations(getPairsListFromDevs(standardDevs));
		
		
		assertThat(devPairCombinations.getPastPairs(0), is(getPairsListFromDevs(standardDevs).get(0).getPairs().values().stream().collect(Collectors.toList())));
		assertThat(devPairCombinations.getPastPairs(1), is(getPairsListFromDevs(standardDevs).get(1).getPairs().values().stream().collect(Collectors.toList())));
		assertThat(devPairCombinations.getPastPairs(2), is(getPairsListFromDevs(standardDevs).get(2).getPairs().values().stream().collect(Collectors.toList())));
	}
	
	@Test
	public void testGetPastPairsFiltersOps() {
		List<DayPairs> pairsListFromDevs = getPairsListFromDevs(getStandardDevs());
		Pair opsPair = pairsListFromDevs.get(0).getPairByTrack("track1");
		opsPair.setOpsPair(true);
		DevPairCombinations devPairCombinations = new DevPairCombinations(pairsListFromDevs);
		
		
		assertThat(devPairCombinations.getPastPairs(0), is(Arrays.asList(pairsListFromDevs.get(0).getPairByTrack("track2"))));
	}
	
	@Test
	public void testGetPastPairsForMissingHistory() {
		DevPairCombinations devPairCombinations = new DevPairCombinations(getPairsListFromDevs(getStandardDevs()));
		
		
		assertThat(devPairCombinations.getPastPairs(3), is(nullValue()));
	}
	
	@Test
	public void testGetPastPairByTrack() {
		List<Developer> standardDevs = getStandardDevs();
		DevPairCombinations devPairCombinations = new DevPairCombinations(getPairsListFromDevs(standardDevs));
		
		
		assertThat(devPairCombinations.getPastPairByTrack(0, "track1"), is(getPairsListFromDevs(standardDevs).get(0).getPairByTrack("track1")));
		assertThat(devPairCombinations.getPastPairByTrack(1, "track2"), is(getPairsListFromDevs(standardDevs).get(1).getPairByTrack("track2")));
		assertThat(devPairCombinations.getPastPairByTrack(2, "track1"), is(getPairsListFromDevs(standardDevs).get(2).getPairByTrack("track1")));
	}
	
	@Test(expected =  RuntimeException.class)
	public void testGetPastPairByTrackThrowsRuntimeErrorForOpsPair() {
		List<DayPairs> pairsListFromDevs = getPairsListFromDevs(getStandardDevs());
		Pair opsPair = pairsListFromDevs.get(0).getPairByTrack("track1");
		opsPair.setOpsPair(true);
		DevPairCombinations devPairCombinations = new DevPairCombinations(pairsListFromDevs);
		
		
		assertThat(devPairCombinations.getPastPairByTrack(0, "track1"), is(Arrays.asList(getPairsListFromDevs(getStandardDevs()).get(0).getPairByTrack("track1"))));
	}
	
	@Test
	public void testGetPastPairByTrackForMissingHistory() {
		DevPairCombinations devPairCombinations = new DevPairCombinations(getPairsListFromDevs(getStandardDevs()));
		
		
		assertThat(devPairCombinations.getPastPairByTrack(3, "track1"), is(nullValue()));
	}
	
	@Test
	public void testIsRotationTimeForTwoDayPair() {
		List<Developer> standardDevs = getStandardDevs();
		DevPairCombinations devPairCombinations = new DevPairCombinations(getPairsListFromDevs(standardDevs));
		
		
		assertThat(devPairCombinations.isRotationTime(Arrays.asList("track1", "track2"), standardDevs), is(true));
	}
	
	@Test
	public void testIsRotationTimeForNewDevUnconformPair() {
		List<Developer> standardDevs = getStandardDevs();
		standardDevs.stream().forEach(developer -> developer.setNew(true));
		List<DayPairs> pastPairs = getPairsListFromDevs(standardDevs);
		pastPairs.remove(2);
		pastPairs.remove(1);		
		DevPairCombinations devPairCombinations = new DevPairCombinations(pastPairs);
		
		
		assertThat(devPairCombinations.isRotationTime(Arrays.asList("track1", "track2"), standardDevs), is(true));
	}
	
	@Test
	public void testIsRotationForOneDayPair() {
		List<Developer> standardDevs = getStandardDevs();
		List<DayPairs> pastPairs = getPairsListFromDevs(standardDevs);
		pastPairs.remove(2);
		pastPairs.remove(1);
		DevPairCombinations devPairCombinations = new DevPairCombinations(pastPairs);
		
		
		assertThat(devPairCombinations.isRotationTime(Arrays.asList("track1", "track2"), standardDevs), is(false));
	}
	
	private List<DayPairs> getPairsListFromDevs(List<Developer> devs) {
		ArrayList<DayPairs> result = new ArrayList<DayPairs>();
		for(int i = 1; i < 3 ; i++){
			DayPairs pairs = new DayPairs();
			pairs.setDate(getPastDate(i));
			pairs.addPair("track1", new Pair(Arrays.asList(devs.get(0), devs.get(1))));
			pairs.addPair("track2", new Pair(Arrays.asList(devs.get(2), devs.get(3))));
			result.add(pairs);
		}
		DayPairs pairs = new DayPairs();
		pairs.setDate(getPastDate(3));
		pairs.addPair("track1", new Pair(Arrays.asList(devs.get(0), devs.get(3))));
		pairs.addPair("track2", new Pair(Arrays.asList(devs.get(2), devs.get(1))));
		result.add(pairs);
		
		return result;
	}

	private List<Developer> getStandardDevs() {
		return Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3"), new Developer("dev4"));
	}
	
	private Date getPastDate(int daysCountToPast) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -(daysCountToPast));
		return cal.getTime();
	}
}
