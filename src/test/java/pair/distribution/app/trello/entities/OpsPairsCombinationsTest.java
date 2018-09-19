package pair.distribution.app.trello.entities;

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

import pair.distribution.app.trello.entities.DayPairs;
import pair.distribution.app.trello.entities.Developer;
import pair.distribution.app.trello.entities.OpsPairCombinations;
import pair.distribution.app.trello.entities.Pair;

public class OpsPairsCombinationsTest {

	@Test
	public void testGetPairsReturnOnlyDevPairs() {
		List<DayPairs> pairsListFromDevs = getPairsListFromDevs(getStandardDevs(), false);
		Pair opsPair = pairsListFromDevs.get(0).getPairByTrack("track1");
		opsPair.setOpsPair(true);
		
		List<Pair> pairs = new OpsPairCombinations(pairsListFromDevs).getPairs();
		
		assertThat(pairs.contains(opsPair), is(true));
		assertThat(pairs.size(), is(1));
	}
	
	@Test
	public void testGetPastPairs() {
		List<Developer> standardDevs = getStandardDevs();
		OpsPairCombinations devPairCombinations = new OpsPairCombinations(getPairsListFromDevs(standardDevs));
		
		
		assertThat(devPairCombinations.getPastPairs(0), is(getPairsListFromDevs(standardDevs).get(0).getPairs().values().stream().collect(Collectors.toList())));
		assertThat(devPairCombinations.getPastPairs(1), is(getPairsListFromDevs(standardDevs).get(1).getPairs().values().stream().collect(Collectors.toList())));
		assertThat(devPairCombinations.getPastPairs(2), is(getPairsListFromDevs(standardDevs).get(2).getPairs().values().stream().collect(Collectors.toList())));
	}
	
	@Test
	public void testGetPastPairsFiltersOps() {
		List<DayPairs> pairsListFromDevs = getPairsListFromDevs(getStandardDevs(), false);
		Pair opsPair = pairsListFromDevs.get(0).getPairByTrack("track1");
		opsPair.setOpsPair(true);
		OpsPairCombinations devPairCombinations = new OpsPairCombinations(pairsListFromDevs);
		
		
		assertThat(devPairCombinations.getPastPairs(0), is(Arrays.asList(pairsListFromDevs.get(0).getPairByTrack("track1"))));
	}
	
	@Test
	public void testGetPastPairsForMissingHistory() {
		OpsPairCombinations devPairCombinations = new OpsPairCombinations(getPairsListFromDevs(getStandardDevs()));
		
		
		assertThat(devPairCombinations.getPastPairs(3), is(nullValue()));
	}
	
	@Test
	public void testGetPastPairByTrack() {
		List<Developer> standardDevs = getStandardDevs();
		OpsPairCombinations devPairCombinations = new OpsPairCombinations(getPairsListFromDevs(standardDevs));
		
		
		assertThat(devPairCombinations.getPastPairByTrack(0, "track1"), is(getPairsListFromDevs(standardDevs).get(0).getPairByTrack("track1")));
		assertThat(devPairCombinations.getPastPairByTrack(1, "track2"), is(getPairsListFromDevs(standardDevs).get(1).getPairByTrack("track2")));
		assertThat(devPairCombinations.getPastPairByTrack(2, "track1"), is(getPairsListFromDevs(standardDevs).get(2).getPairByTrack("track1")));
	}
	
	@Test(expected =  RuntimeException.class)
	public void testGetPastPairByTrackThrowsRuntimeErrorForOpsPair() {
		List<DayPairs> pairsListFromDevs = getPairsListFromDevs(getStandardDevs(), false);
		Pair opsPair = pairsListFromDevs.get(0).getPairByTrack("track1");
		opsPair.setOpsPair(true);
		OpsPairCombinations devPairCombinations = new OpsPairCombinations(pairsListFromDevs);
		
		
		devPairCombinations.getPastPairByTrack(0, "track2");
	}
	
	@Test
	public void testGetPastPairByTrackForMissingHistory() {
		OpsPairCombinations devPairCombinations = new OpsPairCombinations(getPairsListFromDevs(getStandardDevs()));
		
		
		assertThat(devPairCombinations.getPastPairByTrack(3, "track1"), is(nullValue()));
	}
	
	@Test
	public void testGetPastPairByTrackForMissingTrack() {
		OpsPairCombinations devPairCombinations = new OpsPairCombinations(getPairsListFromDevs(getStandardDevs()));
		
		
		assertThat(devPairCombinations.getPastPairByTrack(1, "track5"), is(nullValue()));
	}
	
	@Test
	public void testIsRotationTimeForEmptyHistory() {
		OpsPairCombinations devPairCombinations = new OpsPairCombinations(new ArrayList<>());
		
		
		assertThat(devPairCombinations.isRotationTime(Arrays.asList("track1"), getStandardDevs()), is(false));
	}
	
	@Test
	public void testIsRotationTimeForSameWeek() {
		List<Developer> standardDevs = getStandardDevs();
		DayPairs pairs = new DayPairs();
		pairs.setDate(new Date());
		pairs.addPair("track1", new Pair(Arrays.asList(standardDevs.get(0), standardDevs.get(1)), true, "track1"));
		
		OpsPairCombinations devPairCombinations = new OpsPairCombinations(Arrays.asList(pairs));
		
		
		assertThat(devPairCombinations.isRotationTime(Arrays.asList("track1"), standardDevs), is(false));
	}
	
	@Test
	public void testIsRotationForDifferentWeekPairs() {
		List<Developer> standardDevs = getStandardDevs();
		DayPairs pairs = new DayPairs();
		pairs.setDate(getDateWeeksBefore(1));
		pairs.addPair("track1", new Pair(Arrays.asList(standardDevs.get(0), standardDevs.get(1)), true, "track1"));
		
		OpsPairCombinations devPairCombinations = new OpsPairCombinations(Arrays.asList(pairs));
		
		
		assertThat(devPairCombinations.isRotationTime(Arrays.asList("track1"), standardDevs), is(true));
	}

	private Date getDateWeeksBefore(int daysBack) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.WEEK_OF_YEAR, -daysBack);
		return calendar.getTime();
	}
	
	private List<DayPairs> getPairsListFromDevs(List<Developer> devs) {
		return getPairsListFromDevs(devs, true);
	}
	
	private List<DayPairs> getPairsListFromDevs(List<Developer> devs, boolean ops) {
		ArrayList<DayPairs> result = new ArrayList<DayPairs>();
		for(int i = 1; i < 3 ; i++){
			DayPairs pairs = new DayPairs();
			pairs.setDate(getPastDate(i));
			pairs.addPair("track1", new Pair(Arrays.asList(devs.get(0), devs.get(1)), ops, "track1"));
			pairs.addPair("track2", new Pair(Arrays.asList(devs.get(2), devs.get(3)), ops, "track2"));
			result.add(pairs);
		}
		DayPairs pairs = new DayPairs();
		pairs.setDate(getPastDate(3));
		pairs.addPair("track1", new Pair(Arrays.asList(devs.get(0), devs.get(3)), ops, "track1"));
		pairs.addPair("track2", new Pair(Arrays.asList(devs.get(2), devs.get(1)), ops, "track2"));
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
