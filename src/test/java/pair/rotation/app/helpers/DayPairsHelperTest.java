package pair.rotation.app.helpers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import pair.rotation.app.persistence.mongodb.TrelloPairsRepository;
import pair.rotation.app.trello.entities.Company;
import pair.rotation.app.trello.entities.DayPairs;
import pair.rotation.app.trello.entities.DevPairCombinations;
import pair.rotation.app.trello.entities.Developer;
import pair.rotation.app.trello.entities.Pair;
import pair.rotation.app.trello.entities.PairCombinations;

public class DayPairsHelperTest {

	private DayPairsHelper subject;
	private TrelloPairsRepository trelloPairsRepository;


	@Before
	public void setUp() {
		trelloPairsRepository = mock(TrelloPairsRepository.class);
		subject = new DayPairsHelper(trelloPairsRepository);
	}
	
	
	@Test(expected=RuntimeException.class)
	public void testUpdateDataBaseWithTrelloContentWithException() {
		List<DayPairs> pairsList = getPairsListFromDevs(getStandardDevs());
		when(trelloPairsRepository.findByDate(pairsList.get(2).getDate())).thenReturn(pairsList);
		
		subject.updateDataBaseWithTrelloContent(pairsList);
	}
	
	@Test
	public void testUpdateDataBaseWithTrelloContent() {
		List<DayPairs> pairsList = getPairsListFromDevs(getStandardDevs());
		DayPairs oldPairs = new DayPairs();
		oldPairs.setDate(pairsList.get(0).getDate());
		oldPairs.addPair("oldTrack", new Pair());
		when(trelloPairsRepository.findByDate(pairsList.get(0).getDate())).thenReturn(Arrays.asList(oldPairs));
		when(trelloPairsRepository.findByDate(pairsList.get(1).getDate())).thenReturn(Arrays.asList());
		
		subject.updateDataBaseWithTrelloContent(pairsList);
		
		verify(trelloPairsRepository, atLeast(1)).save(pairsList.get(0));
		verify(trelloPairsRepository, atLeast(1)).save(pairsList.get(1));
	}
	
	@Test
	public void testBuildPairsWeightFromPastPairing() {
		PairCombinations pairs = getPairsList();
		List<Developer> devs = getStandardDevs();
		
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")))), is(2));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev4")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")))), is(2));
	}

	@Test
	public void testAdaptPairsWeightForDoD() {
		
		Developer developer1 = new Developer("dev1");
		developer1.setCompany(new Company("someCompany"));
		Developer developer2 = new Developer("dev2");
		developer2.setCompany(new Company("someCompany"));
		developer2.setDoD(true);
		Developer developer3 = new Developer("dev3");
		developer3.setCompany(new Company("someCompany"));
		Developer developer4 = new Developer("dev4");
		developer4.setCompany(new Company("someOtherCompany"));
		List<Developer> devs = Arrays.asList(developer1, developer2, developer3, developer4);
		PairCombinations pairs = new DevPairCombinations(getPairsListFromDevs(devs));
		
		
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		subject.adaptPairsWeight(pairsWeight, devs);
		
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")))), is(2));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev4")))), is(+100));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")))), is(2));
	}
	
	@Test
	public void testAdaptPairsWeightForNewDevelopers() {
		
		Developer developer1 = new Developer("dev1");
		developer1.setNew(true);
		Developer developer2 = new Developer("dev2");
		developer2.setNew(true);
		Developer developer3 = new Developer("dev3");
		Developer developer4 = new Developer("dev4");
		List<Developer> devs = Arrays.asList(developer1, developer2, developer3, developer4);
		PairCombinations pairs = new DevPairCombinations(getPairsListFromDevs(devs));
		
		
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		subject.adaptPairsWeight(pairsWeight, devs);
		
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")))), is(102));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev4")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")))), is(2));
	}
	
	@Test
	public void testGenerateNewDayPairs() {
		PairCombinations pairs = getPairsList();
		List<Developer> devs = getStandardDevs();
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
		assertThat(dayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2"))))));
		assertThat(dayPairs.getPairByTrack("track2"), is(not(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4"))))));
	}
	
	@Test
	public void testGenerateNewDayPairsWithSmallestWeight() {
		PairCombinations pairs = getLongPairsList();
		List<Developer> devs = Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3"), new Developer("dev4"), new Developer("dev5"), new Developer("dev6"));
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(dayPairs.getTracks().size(), is(3));
		assertThat(dayPairs.getTracks(), contains("track1", "track2", "track3"));
		System.out.println(dayPairs.getPairs());
		assertThat(dayPairs.hasPair(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev6")))), is(true));
		assertThat(dayPairs.hasPair(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev2")))), is(true));
		assertThat(dayPairs.hasPair(new Pair(Arrays.asList(new Developer("dev5"), new Developer("dev4")))), is(true));
	}
	
	@Test
	public void testGenerateNewDayPairsSoloRequired() {
		PairCombinations pairs = getPairsList();
		List<Developer> devs = Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3"));
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
		assertThat(dayPairs.getPairByTrack("track1"), is((new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2"))))));
	}

	@Test
	public void testGenerateNewDayPairsNoOldDevAvailable() {
		PairCombinations pairs = getPairsList();
		pairs.getPairs().remove(2);
		List<Developer> devs = Arrays.asList(new Developer("dev5"), new Developer("dev6"));
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(dayPairs.getTracks().size(), is(1));
		assertThat(dayPairs.getTracks(), contains("track1"));
		assertThat(dayPairs.getPairByTrack("track1"), is(new Pair(Arrays.asList(new Developer("dev5"), new Developer("dev6")))));
	}

	@Test
	public void testGenerateNewDayPairsOnlyOldDevAvailableForStory() {
		PairCombinations pairs = getLongPairsList();
		List<Developer> devs = Arrays.asList(new Developer("dev1"), new Developer("dev3"), new Developer("dev4"));
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
		System.out.println(dayPairs.getPairs());
		assertThat(dayPairs.hasPair(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(true));
		assertThat(dayPairs.hasPair(new Pair(Arrays.asList(new Developer("dev3")))), is(true));
	}
	
	@Test
	public void testGenerateNewDayPairsDoDAvailable() {
		Developer developer1 = new Developer("dev1");
		developer1.setCompany(new Company("someCompany"));
		developer1.setDoD(true);
		Developer developer2 = new Developer("dev2");
		developer2.setCompany(new Company("someCompany"));
		Developer developer3 = new Developer("dev3");
		developer3.setCompany(new Company("someCompany"));
		Developer developer4 = new Developer("dev4");
		developer4.setCompany(new Company("someOtherCompany"));
		DayPairs dayPairs = new DayPairs();
		dayPairs.addPair("track1", new Pair(Arrays.asList(developer1, developer4)));
		dayPairs.addPair("track2", new Pair(Arrays.asList(developer2, developer3)));
		dayPairs.setDate(getPastDate(1));
		DevPairCombinations pairs = new DevPairCombinations(Arrays.asList(dayPairs));
		List<Developer> devs = Arrays.asList(developer1, developer2, developer3, developer4);
		List<String> tracks = Arrays.asList("track1", "track2");
		
		
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		subject.adaptPairsWeight(pairsWeight, devs);
		
		DayPairs todayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(todayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4"))))));
	}
	
	@Test
	public void testGenerateNewDayPairsDoDAvailableAndSolo() {
		Developer developer1 = new Developer("dev1");
		developer1.setCompany(new Company("someCompany"));
		developer1.setDoD(true);
		Developer developer2 = new Developer("dev2");
		developer2.setCompany(new Company("someCompany"));
		Developer developer3 = new Developer("dev3");
		developer3.setCompany(new Company("someCompany"));
		DayPairs dayPairs = new DayPairs();
		dayPairs.addPair("track1", new Pair(Arrays.asList(developer1)));
		dayPairs.addPair("track2", new Pair(Arrays.asList(developer2, developer3)));
		dayPairs.setDate(getPastDate(1));
		DevPairCombinations pairs = new DevPairCombinations(Arrays.asList(dayPairs));
		List<Developer> devs = Arrays.asList(developer1, developer2, developer3);
		List<String> tracks = Arrays.asList("track1", "track2");
		
		
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		subject.adaptPairsWeight(pairsWeight, devs);
		
		DayPairs todayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		subject.rotateSoloPairIfAny(todayPairs, pairs, pairsWeight);
		
		assertThat(todayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(new Developer("dev1"))))));
		assertThat(todayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(developer2, developer3)))));
	}
	
	@Test
	public void testGenerateNewDayPairsNewDevsAvailable() {
		
		PairCombinations pairs = getPairsList();
		Developer developer2 = new Developer("dev2");
		developer2.setNew(true);
		Developer developer3 = new Developer("dev3");
		developer3.setNew(true);
		List<Developer> devs = Arrays.asList(new Developer("dev1"), developer2, developer3, new Developer("dev4"));
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		subject.adaptPairsWeight(pairsWeight, devs);
		
		DayPairs todayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(todayPairs.getPairByTrack("track1"), is(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")))));
		assertThat(todayPairs.getPairByTrack("track2"), is(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")))));
	}
	
	@Test
	public void testGenerateNewDayPairsNewDeveloperSolo() {
		Developer developer1 = new Developer("dev1");
		developer1.setNew(true);
		Developer developer2 = new Developer("dev2");
		Developer developer3 = new Developer("dev3");
		DayPairs dayPairs = new DayPairs();
		dayPairs.addPair("track1", new Pair(Arrays.asList(developer1)));
		dayPairs.addPair("track2", new Pair(Arrays.asList(developer2, developer3)));
		dayPairs.setDate(getPastDate(1));
		DevPairCombinations pairs = new DevPairCombinations(Arrays.asList(dayPairs));
		List<Developer> devs = Arrays.asList(developer1, developer2, developer3);
		List<String> tracks = Arrays.asList("track1", "track2");
		
		
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		subject.adaptPairsWeight(pairsWeight, devs);
		
		DayPairs todayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		subject.rotateSoloPairIfAny(todayPairs, pairs, pairsWeight);
		
		assertThat(todayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(new Developer("dev1"))))));
		assertThat(todayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(developer2, developer3)))));
	}
	
	@Test
	public void testGenerateNewDayPairsNoPastState() {
		DevPairCombinations pairs = new DevPairCombinations(new ArrayList<>());
		List<Developer> devs = getStandardDevs();
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
	}
	
	@Test
	public void testGenerateNewDayPairsNoPastStateAndRotationNeeded() {
		DevPairCombinations pairs = new DevPairCombinations(new ArrayList<>());
		List<Developer> devs = getStandardDevs();
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, true);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
	}
	
	private PairCombinations getPairsList() {
		List<Developer> devs = getStandardDevs();
		return new DevPairCombinations(getPairsListFromDevs(devs));
	}


	private List<Developer> getStandardDevs() {
		return Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3"), new Developer("dev4"));
	}
	
	private PairCombinations getLongPairsList() {
		List<Developer> devs = Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3"), new Developer("dev4"), new Developer("dev5"), new Developer("dev6"));
		return new DevPairCombinations(getPairsListWithLongestDevFromDevs(devs));
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
	
	private List<DayPairs> getPairsListWithLongestDevFromDevs(List<Developer> devs) {
		ArrayList<DayPairs> result = new ArrayList<DayPairs>();
		for(int i = 1; i < 3 ; i++){
			DayPairs pairs = new DayPairs();
			pairs.setDate(getPastDate(i));
			pairs.addPair("track1", new Pair(Arrays.asList(devs.get(0), devs.get(3))));
			pairs.addPair("track2", new Pair(Arrays.asList(devs.get(2), devs.get(5))));
			pairs.addPair("track3", new Pair(Arrays.asList(devs.get(4), devs.get(1))));
			result.add(pairs);
		}
		
		for(int i = 3; i < 5 ; i++){
			DayPairs pairs = new DayPairs();
			pairs.setDate(getPastDate(i));
			pairs.addPair("track1", new Pair(Arrays.asList(devs.get(0), devs.get(1))));
			pairs.addPair("track2", new Pair(Arrays.asList(devs.get(2), devs.get(3))));
			pairs.addPair("track3", new Pair(Arrays.asList(devs.get(4), devs.get(5))));
			result.add(pairs);
		}
		return result;
	}
	
	@Test
	public void testRotateSoloPair() {
		Pair soloPair = new Pair(Arrays.asList(new Developer("dev3")));
		List<DayPairs> pairs = getPairsListFromDevs(getStandardDevs());
		for (DayPairs dayPairs : pairs) {
			dayPairs.addPair("track2", soloPair);
		}
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(new DevPairCombinations(pairs), Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3")));
		DayPairs todayPairs = pairs.get(0);
	
		assertThat(todayPairs.getPairByTrack("track2"), is(soloPair));
		
		DevPairCombinations newCombinations = new DevPairCombinations(pairs.subList(1, pairs.size()));
		subject.rotateSoloPairIfAny(todayPairs, newCombinations, pairsWeight);
		
		assertThat(todayPairs.getPairByTrack("track2"), is(not(soloPair)));
	}
	
	@Test
	public void testRotateSoloPairOnDoD() {
		Developer soloDeveloper = new Developer("dev3");
		soloDeveloper.setDoD(true);
		soloDeveloper.setCompany(new Company("company"));
		Developer developer1 = new Developer("dev1");
		developer1.setCompany(new Company("company"));
		Developer developer2 = new Developer("dev2");
		developer2.setCompany(new Company("someOtherCompany"));
		List<Developer> availableDevs = Arrays.asList(developer1, developer2, soloDeveloper);
		List<DayPairs> pairs = new ArrayList<>();
		DayPairs todayPairs = new DayPairs();
		todayPairs.addPair("track1", new Pair(Arrays.asList(developer1, developer2)));
		Pair soloPair = new Pair(Arrays.asList(soloDeveloper));
		todayPairs.addPair("track2", soloPair);
		pairs.add(todayPairs);	
		DevPairCombinations pairCombinations = new DevPairCombinations(pairs);
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairCombinations, availableDevs);
		subject.adaptPairsWeight(pairsWeight, availableDevs);
	
		
		subject.rotateSoloPairIfAny(todayPairs, new DevPairCombinations(pairs.subList(1, pairs.size())), pairsWeight);
		
		assertThat(todayPairs.getPairByTrack("track2"), is(not(soloPair)));
		assertThat(todayPairs.getPairByTrack("track1"), is(new Pair(Arrays.asList(developer1, soloDeveloper))));
	}
	
	@Test
	public void testRotateSoloPairOnDoDAllDevFromSameCompany() {
		Developer soloDeveloper = new Developer("dev3");
		soloDeveloper.setDoD(true);
		soloDeveloper.setCompany(new Company("company"));
		Developer developer1 = new Developer("dev1");
		developer1.setCompany(new Company("company"));
		Developer developer2 = new Developer("dev2");
		developer2.setCompany(new Company("company"));
		List<Developer> availableDevs = Arrays.asList(developer1, developer2, soloDeveloper);
		List<DayPairs> pairs = new ArrayList<>();
		DayPairs todayPairs = new DayPairs();
		todayPairs.addPair("track1", new Pair(Arrays.asList(developer1, developer2)));
		Pair soloPair = new Pair(Arrays.asList(soloDeveloper));
		todayPairs.addPair("track2", soloPair);
		pairs.add(todayPairs);	
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(new DevPairCombinations(pairs), availableDevs);
		subject.adaptPairsWeight(pairsWeight, availableDevs);
	
		
		subject.rotateSoloPairIfAny(todayPairs, new DevPairCombinations(pairs.subList(1, pairs.size())), pairsWeight);
		
		assertThat(todayPairs.getPairByTrack("track2"), is(not(soloPair)));
	}
	
	@Test
	public void testRotateSoloPairOnDoDNoPairFromSameCompany() {
		Developer soloDeveloper = new Developer("dev3");
		soloDeveloper.setDoD(true);
		soloDeveloper.setCompany(new Company("company"));
		Developer developer1 = new Developer("dev1");
		developer1.setCompany(new Company("someOtherCompany"));
		Developer developer2 = new Developer("dev2");
		developer2.setCompany(new Company("someOtherCompany"));
		List<Developer> availableDevs = Arrays.asList(developer1, developer2, soloDeveloper);
		List<DayPairs> pairs = new ArrayList<>();
		DayPairs todayPairs = new DayPairs();
		todayPairs.addPair("track1", new Pair(Arrays.asList(developer1, developer2)));
		Pair soloPair = new Pair(Arrays.asList(soloDeveloper));
		todayPairs.addPair("track2", soloPair);
		pairs.add(todayPairs);	
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(new DevPairCombinations(pairs), availableDevs);
		subject.adaptPairsWeight(pairsWeight, availableDevs);
	
		
		subject.rotateSoloPairIfAny(todayPairs, new DevPairCombinations(pairs.subList(1, pairs.size())), pairsWeight);
		
		assertThat(todayPairs.getPairByTrack("track2"), is(soloPair));
		assertThat(todayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(developer1, soloDeveloper)))));
	}
	
	@Test
	public void testRotateSoloPairWithoutState() {
		List<DayPairs> pairs = new ArrayList<>();
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(new DevPairCombinations(pairs), Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3")));

		subject.rotateSoloPairIfAny(new DayPairs(), new DevPairCombinations(pairs), pairsWeight);
	}
	
	@Test
	public void testBuildBuildPairsWeightFromPastPairingWhenAny() {
		PairCombinations pairCombinations = getPairsList();
		pairCombinations.getPastPairByTrack(0, "track1").setBuildPair(true);
		pairCombinations.getPastPairByTrack(1, "track2").setBuildPair(true);
		pairCombinations.getPastPairByTrack(2, "track1").setBuildPair(true);
		List<Developer> devs = getStandardDevs();
		
		Map<Pair, Integer> pairsWeight = subject.buildBuildPairsWeightFromPastPairing(pairCombinations, devs);
		
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev4")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")))), is(1));
	}
	
	@Test
	public void testBuildBuildPairsWeightFromPastPairingWhenNoInitialWeight() {
		PairCombinations pairCombinations = getPairsList();
		pairCombinations.getPastPairByTrack(2, "track1").setBuildPair(true);
		List<Developer> devs = Arrays.asList(new Developer("dev5"), new Developer("dev6"));
		
		Map<Pair, Integer> pairsWeight = subject.buildBuildPairsWeightFromPastPairing(pairCombinations, devs);
		
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(nullValue()));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev5"), new Developer("dev6")))), is(0));
	}
	
	@Test
	public void testBuildBuildPairsWeightFromPastPairingWhenNon() {
		PairCombinations pairCombinations = getPairsList();
		List<Developer> devs = getStandardDevs();
		
		Map<Pair, Integer> pairsWeight = subject.buildBuildPairsWeightFromPastPairing(pairCombinations, devs);
		
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev4")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")))), is(0));
	}

	@Test
	public void testBuildCommunityPairsWeightFromPastPairingWhenAny() {
		PairCombinations pairCombinations = getPairsList();
		pairCombinations.getPastPairByTrack(0, "track1").setCommunityPair(true);
		pairCombinations.getPastPairByTrack(1, "track2").setCommunityPair(true);
		pairCombinations.getPastPairByTrack(2, "track1").setCommunityPair(true);
		List<Developer> devs = getStandardDevs();
		
		Map<Pair, Integer> pairsWeight = subject.buildCommunityPairsWeightFromPastPairing(pairCombinations, devs);
		
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev4")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")))), is(1));
	}
	
	@Test
	public void testBuildCommunityPairsWeightFromPastPairingWhenNoInitialWeight() {
		PairCombinations pairCombinations = getPairsList();
		pairCombinations.getPastPairByTrack(2, "track1").setCommunityPair(true);
		List<Developer> devs = Arrays.asList(new Developer("dev5"), new Developer("dev6"));
		
		Map<Pair, Integer> pairsWeight = subject.buildCommunityPairsWeightFromPastPairing(pairCombinations, devs);
		
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(1));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(nullValue()));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev5"), new Developer("dev6")))), is(0));
	}
	
	@Test
	public void testBuildCommunityPairsWeightFromPastPairingWhenNon() {
		PairCombinations pairCombinations = getPairsList();
		List<Developer> devs = getStandardDevs();
		
		Map<Pair, Integer> pairsWeight = subject.buildCommunityPairsWeightFromPastPairing(pairCombinations, devs);
		
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev4")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")))), is(0));
	}
	
	@Test
	public void testSetBuildPairWithoutWeights() {		
		List<Developer> devs = getStandardDevs();
		Map<Pair, Integer> pairsWeight = subject.buildBuildPairsWeightFromPastPairing(getPairsList(), devs);
		List<Pair> todayPairs = Arrays.asList(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2"))), new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4"))));
		
		subject.setBuildPair(todayPairs, pairsWeight);
		
		if(todayPairs.get(0).isBuildPair()){
			assertThat(todayPairs.get(1).isBuildPair(), is(false));
		}else {
			assertThat(todayPairs.get(1).isBuildPair(), is(true));
		}
	}
	
	@Test
	public void testSetBuildPairWithDifferentWeights() {		
		List<Developer> devs = getStandardDevs();				
		PairCombinations pairCombinations = getPairsList();
		pairCombinations.getPastPairByTrack(1, "track2").setBuildPair(true);
		Map<Pair, Integer> pairsWeight = subject.buildBuildPairsWeightFromPastPairing(pairCombinations, devs);
		List<Pair> todayPairs = Arrays.asList(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2"))), new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4"))));
		
		subject.setBuildPair(todayPairs, pairsWeight);
		
		assertThat(todayPairs.get(0).isBuildPair(), is(true));
		assertThat(todayPairs.get(1).isBuildPair(), is(false));
	}
	
	@Test
	public void testSetBuildPairWithMissingWeights() {		
		Map<Pair, Integer> pairsWeight = new HashMap<Pair, Integer>();
		List<Pair> todayPairs = Arrays.asList(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2"))), new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4"))));
		
		subject.setBuildPair(todayPairs, pairsWeight);
		
		
		if(todayPairs.get(0).isBuildPair()){
			assertThat(todayPairs.get(1).isBuildPair(), is(false));
		}else {
			assertThat(todayPairs.get(1).isBuildPair(), is(true));
		}
	}
	
	@Test
	public void testSetCommunityPairWithoutWeightsAndBuildPairAvailable() {		
		List<Developer> devs = getStandardDevs();
		Map<Pair, Integer> pairsWeight = subject.buildCommunityPairsWeightFromPastPairing(getPairsList(), devs);
		Pair buildPair = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		buildPair.setBuildPair(true);
		List<Pair> todayPairs = Arrays.asList(buildPair, new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4"))));
		
		subject.setCommunityPair(todayPairs, pairsWeight);
		
		assertThat(todayPairs.get(0).isCommunityPair(), is(false));
		assertThat(todayPairs.get(1).isCommunityPair(), is(true));
	}
	
	@Test
	public void testSetBuildPairWithDifferentWeightsAndMinWeightIsBuildPair() {		
		List<Developer> devs = getStandardDevs();				
		PairCombinations pairCombinations = getPairsList();
		pairCombinations.getPastPairByTrack(1, "track2").setCommunityPair(true);
		Map<Pair, Integer> pairsWeight = subject.buildCommunityPairsWeightFromPastPairing(pairCombinations, devs);
		Pair buildPair = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		buildPair.setBuildPair(true);
		List<Pair> todayPairs = Arrays.asList(buildPair, new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4"))));
		
		subject.setCommunityPair(todayPairs, pairsWeight);
		
		assertThat(todayPairs.get(0).isCommunityPair(), is(false));
		assertThat(todayPairs.get(1).isCommunityPair(), is(true));
	}
	
	@Test
	public void testSetCommunityPairWithMissingWeights() {		
		Map<Pair, Integer> pairsWeight = new HashMap<Pair, Integer>();
		List<Pair> todayPairs = Arrays.asList(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2"))), new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4"))));
		
		subject.setCommunityPair(todayPairs, pairsWeight);
		
		
		if(todayPairs.get(0).isCommunityPair()){
			assertThat(todayPairs.get(1).isCommunityPair(), is(false));
		}else {
			assertThat(todayPairs.get(1).isCommunityPair(), is(true));
		}
	}
	
	@Test
	public void testSetCommunityPairOnePairAvailable() {
		Pair pair = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		pair.setBuildPair(true);
		List<Pair> todayPairs = Arrays.asList(pair);
		
		subject.setCommunityPair(todayPairs, new HashMap<Pair, Integer>());
		
		assertThat(todayPairs.get(0).isCommunityPair(), is(true));
	}
	
	private Date getPastDate(int daysCountToPast) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -(daysCountToPast));
		return cal.getTime();
	}
}
