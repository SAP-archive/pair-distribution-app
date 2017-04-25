package pair.rotation.app.trello;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import pair.rotation.app.persistence.mongodb.TrelloPairsRepository;

public class DayPairsHelperTest {

	private DayPairsHelper subject;
	private TrelloPairsRepository trelloPairsRepository;


	@Before
	public void setUp() throws ParseException{
		trelloPairsRepository = mock(TrelloPairsRepository.class);
		subject = new DayPairsHelper(trelloPairsRepository);
	}
	
	
	@Test(expected=RuntimeException.class)
	public void testUpdateDataBaseWithTrelloContentWithException() throws Exception {
		List<DayPairs> pairsList = getPairsList();
		when(trelloPairsRepository.findByDate(pairsList.get(2).getDate())).thenReturn(pairsList);
		
		subject.updateDataBaseWithTrelloContent(pairsList);
	}
	
	@Test
	public void testUpdateDataBaseWithTrelloContent() throws Exception {
		List<DayPairs> pairsList = getPairsList();
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
	public void testBuildPairsWeightFromPastPairing() throws Exception {
		List<DayPairs> pairs = getPairsList();
		List<Developer> devs = Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3"), new Developer("dev4"));
		
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")))), is(3));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev4")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")))), is(3));
	}

	@Test
	public void testAdaptPairsWeightForDoD() throws Exception {
		
		Developer developer1 = new Developer("dev1");
		developer1.setCompany("someCompany");
		Developer developer2 = new Developer("dev2");
		developer2.setCompany("someCompany");
		developer2.setDoD(true);
		Developer developer3 = new Developer("dev3");
		developer3.setCompany("someCompany");
		Developer developer4 = new Developer("dev4");
		developer4.setCompany("someOtherCompany");
		List<Developer> devs = Arrays.asList(developer1, developer2, developer3, developer4);
		List<DayPairs> pairs = getPairsListFromDevs(devs);
		
		
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		subject.adaptPairsWeightForDoD(pairsWeight, devs);
		
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")))), is(3));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(0));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev4")))), is(+100));
		assertThat(pairsWeight.get(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")))), is(3));
	}
	
	@Test
	public void testGenerateNewDayPairs() throws Exception {
		List<DayPairs> pairs = getPairsList();
		List<Developer> devs = Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3"), new Developer("dev4"));
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
		assertThat(dayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2"))))));
		assertThat(dayPairs.getPairByTrack("track2"), is(not(new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4"))))));
	}
	
	@Test
	public void testGenerateNewDayPairsWithSmallestWeight() throws Exception {
		List<DayPairs> pairs = getPairsList();
		List<Developer> devs = Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3"), new Developer("dev4"));
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		pairsWeight.put(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev3"))), 1);
		pairsWeight.put(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev4"))), 1);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
		System.out.println(dayPairs.getPairs());
		assertThat(dayPairs.hasPair(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4")))), is(true));
		assertThat(dayPairs.hasPair(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3")))), is(true));
	}
	
	@Test
	public void testGenerateNewDayPairsSoloRequired() throws Exception {
		List<DayPairs> pairs = getPairsList();
		List<Developer> devs = Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3"));
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		pairsWeight.put(new Pair(Arrays.asList(new Developer("dev2"), new Developer("dev3"))), 1);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
		assertThat(dayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2"))))));
	}

	@Test
	public void testGenerateNewDayPairsNoOldDevAvailable() throws Exception {
		List<DayPairs> pairs = getPairsList();
		pairs.remove(2);
		List<Developer> devs = Arrays.asList(new Developer("dev5"), new Developer("dev6"));
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(dayPairs.getTracks().size(), is(1));
		assertThat(dayPairs.getTracks(), contains("track1"));
		assertThat(dayPairs.getPairByTrack("track1"), is(new Pair(Arrays.asList(new Developer("dev5"), new Developer("dev6")))));
	}
	
	@Test
	public void testGenerateNewDayPairsDoDAvailable() throws Exception {
		Developer developer1 = new Developer("dev1");
		developer1.setCompany("someCompany");
		developer1.setDoD(true);
		Developer developer2 = new Developer("dev2");
		developer2.setCompany("someCompany");
		Developer developer3 = new Developer("dev3");
		developer3.setCompany("someCompany");
		Developer developer4 = new Developer("dev4");
		developer4.setCompany("someOtherCompany");
		DayPairs dayPairs = new DayPairs();
		dayPairs.addPair("track1", new Pair(Arrays.asList(developer1, developer4)));
		dayPairs.addPair("track2", new Pair(Arrays.asList(developer2, developer3)));
		dayPairs.setDate(getPastDate(1));
		List<DayPairs> pairs = Arrays.asList(dayPairs);
		List<Developer> devs = Arrays.asList(developer1, developer2, developer3, developer4);
		List<String> tracks = Arrays.asList("track1", "track2");
		
		
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		subject.adaptPairsWeightForDoD(pairsWeight, devs);
		
		DayPairs todayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(todayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev4"))))));
	}
	
	@Test
	public void testGenerateNewDayPairsDoDAvailableAndSolo() throws Exception {
		Developer developer1 = new Developer("dev1");
		developer1.setCompany("someCompany");
		developer1.setDoD(true);
		Developer developer2 = new Developer("dev2");
		developer2.setCompany("someCompany");
		Developer developer3 = new Developer("dev3");
		developer3.setCompany("someCompany");
		DayPairs dayPairs = new DayPairs();
		dayPairs.addPair("track1", new Pair(Arrays.asList(developer1)));
		dayPairs.addPair("track2", new Pair(Arrays.asList(developer2, developer3)));
		dayPairs.setDate(getPastDate(1));
		List<DayPairs> pairs = Arrays.asList(dayPairs);
		List<Developer> devs = Arrays.asList(developer1, developer2, developer3);
		List<String> tracks = Arrays.asList("track1", "track2");
		
		
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		subject.adaptPairsWeightForDoD(pairsWeight, devs);
		
		DayPairs todayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(todayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(new Developer("dev1"))))));
		assertThat(todayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(developer2, developer3)))));
	}
	
	@Test
	public void testGenerateNewDayPairsNoPastState() throws Exception {
		List<DayPairs> pairs = new ArrayList<>();
		List<Developer> devs = Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3"), new Developer("dev4"));
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight, false);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
	}
	
	@Test
	public void testDateFormatterFormat() {
		assertThat(DayPairsHelper.DATE_FORMATTER, is(new SimpleDateFormat("dd-MM-yyyy")));
    }
	
	private List<DayPairs> getPairsList() {
		return getPairsListFromDevs(Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3"), new Developer("dev4")));
	}
	
	private List<DayPairs> getPairsListFromDevs(List<Developer> devs) {
		ArrayList<DayPairs> result = new ArrayList<DayPairs>();
		for(int i = 1; i < 4 ; i++){
			DayPairs pairs = new DayPairs();
			pairs.setDate(getPastDate(i));
			pairs.addPair("track1", new Pair(Arrays.asList(devs.get(0), devs.get(1))));
			pairs.addPair("track2", new Pair(Arrays.asList(devs.get(2), devs.get(3))));
			result.add(pairs);
		}
		return result;
	}
	
	@Test
	public void testRotateSoloPair() throws Exception {
		Pair soloPair = new Pair(Arrays.asList(new Developer("dev3")));
		List<DayPairs> pairs = getPairsList();
		for (DayPairs dayPairs : pairs) {
			dayPairs.addPair("track2", soloPair);
		}
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3")));
		DayPairs todayPairs = pairs.get(0);
	
		assertThat(todayPairs.getPairByTrack("track2"), is(soloPair));
		
		subject.rotateSoloPairIfAny(todayPairs, pairs.subList(1, pairs.size()), pairsWeight);
		
		assertThat(todayPairs.getPairByTrack("track2"), is(not(soloPair)));
	}
	
	@Test
	public void testRotateSoloPairOnDoD() throws Exception {
		Developer soloDeveloper = new Developer("dev3");
		soloDeveloper.setDoD(true);
		soloDeveloper.setCompany("company");
		Developer developer1 = new Developer("dev1");
		developer1.setCompany("company");
		Developer developer2 = new Developer("dev2");
		developer2.setCompany("someOtherCompany");
		List<Developer> availableDevs = Arrays.asList(developer1, developer2, soloDeveloper);
		List<DayPairs> pairs = new ArrayList<>();
		DayPairs todayPairs = new DayPairs();
		todayPairs.addPair("track1", new Pair(Arrays.asList(developer1, developer2)));
		Pair soloPair = new Pair(Arrays.asList(soloDeveloper));
		todayPairs.addPair("track2", soloPair);
		pairs.add(todayPairs);	
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, availableDevs);
		subject.adaptPairsWeightForDoD(pairsWeight, availableDevs);
	
		
		subject.rotateSoloPairIfAny(todayPairs, pairs.subList(1, pairs.size()), pairsWeight);
		
		assertThat(todayPairs.getPairByTrack("track2"), is(not(soloPair)));
		assertThat(todayPairs.getPairByTrack("track1"), is(new Pair(Arrays.asList(developer1, soloDeveloper))));
	}
	
	@Test
	public void testRotateSoloPairOnDoDAllDevFromSameCompany() throws Exception {
		Developer soloDeveloper = new Developer("dev3");
		soloDeveloper.setDoD(true);
		soloDeveloper.setCompany("company");
		Developer developer1 = new Developer("dev1");
		developer1.setCompany("company");
		Developer developer2 = new Developer("dev2");
		developer2.setCompany("company");
		List<Developer> availableDevs = Arrays.asList(developer1, developer2, soloDeveloper);
		List<DayPairs> pairs = new ArrayList<>();
		DayPairs todayPairs = new DayPairs();
		todayPairs.addPair("track1", new Pair(Arrays.asList(developer1, developer2)));
		Pair soloPair = new Pair(Arrays.asList(soloDeveloper));
		todayPairs.addPair("track2", soloPair);
		pairs.add(todayPairs);	
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, availableDevs);
		subject.adaptPairsWeightForDoD(pairsWeight, availableDevs);
	
		
		subject.rotateSoloPairIfAny(todayPairs, pairs.subList(1, pairs.size()), pairsWeight);
		
		assertThat(todayPairs.getPairByTrack("track2"), is(not(soloPair)));
	}
	
	@Test
	public void testRotateSoloPairOnDoDNoPairFromSameCompany() throws Exception {
		Developer soloDeveloper = new Developer("dev3");
		soloDeveloper.setDoD(true);
		soloDeveloper.setCompany("company");
		Developer developer1 = new Developer("dev1");
		developer1.setCompany("someOtherCompany");
		Developer developer2 = new Developer("dev2");
		developer2.setCompany("someOtherCompany");
		List<Developer> availableDevs = Arrays.asList(developer1, developer2, soloDeveloper);
		List<DayPairs> pairs = new ArrayList<>();
		DayPairs todayPairs = new DayPairs();
		todayPairs.addPair("track1", new Pair(Arrays.asList(developer1, developer2)));
		Pair soloPair = new Pair(Arrays.asList(soloDeveloper));
		todayPairs.addPair("track2", soloPair);
		pairs.add(todayPairs);	
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, availableDevs);
		subject.adaptPairsWeightForDoD(pairsWeight, availableDevs);
	
		
		subject.rotateSoloPairIfAny(todayPairs, pairs.subList(1, pairs.size()), pairsWeight);
		
		assertThat(todayPairs.getPairByTrack("track2"), is(soloPair));
		assertThat(todayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList(developer1, soloDeveloper)))));
	}
	
	@Test
	public void testRotateSoloPairWithoutState() throws Exception {
		List<DayPairs> pairs = new ArrayList<>();
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeightFromPastPairing(pairs, Arrays.asList(new Developer("dev1"), new Developer("dev2"), new Developer("dev3")));

		subject.rotateSoloPairIfAny(new DayPairs(), pairs, pairsWeight);
	}
	
	private Date getPastDate(int daysCountToPast) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -(daysCountToPast));
		return cal.getTime();
	}
}
