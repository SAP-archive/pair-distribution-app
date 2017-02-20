package pair.rotation.app.trello;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;

import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.TList;

import pair.rotation.app.persistence.mongodb.TrelloPairsRepository;
import pair.rotation.app.trello.DayPairs;
import pair.rotation.app.trello.DayPairsHelper;
import pair.rotation.app.trello.Pair;
import pair.rotation.app.trello.PairingBoard;

public class DayPairsHelperTest {

	private PairingBoard pairingBoardTrell;
	private DayPairsHelper subject;
	private TrelloPairsRepository trelloPairsRepository;


	@Before
	public void setUp() throws ParseException{
		pairingBoardTrell = mock(PairingBoard.class);
		trelloPairsRepository = mock(TrelloPairsRepository.class);
		subject = new DayPairsHelper(trelloPairsRepository, pairingBoardTrell);
		when(pairingBoardTrell.getLits()).thenReturn(getTestDataForBoard());
	}
	
	
	@Test
	public void testGetTracks() {
		assertThat(subject.getTracks(), is(equalTo(Arrays.asList("track1", "track2"))));
	}

	@Test
	public void testGetDevs() {
		assertThat(subject.getDevs(), is(equalTo(Arrays.asList("dev1", "dev2"))));
	}
	
	@Test
	public void testGetPairs() throws ParseException {
		assertThat(subject.getPairs(), is(equalTo(getPairsList())));
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
	public void testBuildPairsWeight() throws Exception {
		List<DayPairs> pairs = getPairsList();
		List<String> devs = Arrays.asList("dev1", "dev2", "dev3", "dev4");
		
		Map<Pair, Integer> pairsWight = subject.buildPairsWeight(pairs, devs);
		
		assertThat(pairsWight.get(new Pair(Arrays.asList("dev1", "dev2"))), is(3));
		assertThat(pairsWight.get(new Pair(Arrays.asList("dev1", "dev3"))), is(0));
		assertThat(pairsWight.get(new Pair(Arrays.asList("dev1", "dev4"))), is(0));
		assertThat(pairsWight.get(new Pair(Arrays.asList("dev2", "dev3"))), is(0));
		assertThat(pairsWight.get(new Pair(Arrays.asList("dev2", "dev4"))), is(0));
		assertThat(pairsWight.get(new Pair(Arrays.asList("dev3", "dev4"))), is(3));
	}

	@Test
	public void testGenerateNewDayPairs() throws Exception {
		List<DayPairs> pairs = getPairsList();
		List<String> devs = Arrays.asList("dev1", "dev2", "dev3", "dev4");
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeight(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
		assertThat(dayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList("dev1", "dev2")))));
		assertThat(dayPairs.getPairByTrack("track2"), is(not(new Pair(Arrays.asList("dev3", "dev4")))));
	}
	
	@Test
	public void testGenerateNewDayPairsWithSmallestWeight() throws Exception {
		List<DayPairs> pairs = getPairsList();
		List<String> devs = Arrays.asList("dev1", "dev2", "dev3", "dev4");
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeight(pairs, devs);
		pairsWeight.put(new Pair(Arrays.asList("dev1", "dev3")), 1);
		pairsWeight.put(new Pair(Arrays.asList("dev2", "dev4")), 1);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
		System.out.println(dayPairs.getPairs());
		assertThat(dayPairs.hasPair(new Pair(Arrays.asList("dev1", "dev4"))), is(true));
		assertThat(dayPairs.hasPair(new Pair(Arrays.asList("dev2", "dev3"))), is(true));
	}
	
	@Test
	public void testGenerateNewDayPairsSoloRequired() throws Exception {
		List<DayPairs> pairs = getPairsList();
		List<String> devs = Arrays.asList("dev1", "dev2", "dev3");
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeight(pairs, devs);
		pairsWeight.put(new Pair(Arrays.asList("dev2", "dev3")), 1);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
		assertThat(dayPairs.getPairByTrack("track1"), is(not(new Pair(Arrays.asList("dev1", "dev2")))));
	}

	@Test
	public void testGenerateNewDayPairsNoOldDevAvailable() throws Exception {
		List<DayPairs> pairs = getPairsList();
		pairs.remove(2);
		List<String> devs = Arrays.asList("dev5", "dev6");
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeight(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight);
		
		assertThat(dayPairs.getTracks().size(), is(1));
		assertThat(dayPairs.getTracks(), contains("track1"));
		assertThat(dayPairs.getPairByTrack("track1"), is(new Pair(Arrays.asList("dev5", "dev6"))));
	}
	
	@Test
	public void testGenerateNewDayPairsNoPastState() throws Exception {
		List<DayPairs> pairs = new ArrayList<>();
		List<String> devs = Arrays.asList("dev1", "dev2", "dev3", "dev4");
		List<String> tracks = Arrays.asList("track1", "track2", "track3");
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeight(pairs, devs);
		
		DayPairs dayPairs = subject.generateNewDayPairs(tracks, devs, pairs, pairsWeight);
		
		assertThat(dayPairs.getTracks().size(), is(2));
		assertThat(dayPairs.getTracks(), contains("track1", "track2"));
	}
	
	
	private List<DayPairs> getPairsList() {
		ArrayList<DayPairs> result = new ArrayList<DayPairs>();
		for(int i = 1; i < 4 ; i++){
			DayPairs pairs = new DayPairs();
			pairs.setDate(getPastDate(i));
			pairs.addPair("track1", new Pair(Arrays.asList("dev1", "dev2")));
			pairs.addPair("track2", new Pair(Arrays.asList("dev3", "dev4")));
			result.add(pairs);
		}
		return result;
	}
	
	private List<TList> getTestDataForBoard() throws ParseException{
		ArrayList<TList> result = new ArrayList<TList>();
		result.add(getListWithCards("tracks", "track1", "track2", null));
		result.add(getListWithCards("devs", "dev1", "dev2", null));
		result.add(getListWithCards("pairing(" + DayPairsHelper.DATE_FORMATTER.format(getPastDate(1)) + ")", "track1", "track2", getPairs()));
		result.add(getListWithCards("pairing(" + DayPairsHelper.DATE_FORMATTER.format(getPastDate(2)) + ")", "track1", "track2", getPairs()));
		result.add(getListWithCards("pairing(" + DayPairsHelper.DATE_FORMATTER.format(getPastDate(3)) + ")", "track1", "track2", getPairs()));
		
		return result;
	}
	
	@Test
	public void testRotateSoloPair() throws Exception {
		Pair soloPair = new Pair(Arrays.asList("dev3"));
		List<DayPairs> pairs = getPairsList();
		for (DayPairs dayPairs : pairs) {
			dayPairs.addPair("track2", soloPair);
		}
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeight(pairs, Arrays.asList("dev1", "dev2", "dev3"));
		DayPairs todayPairs = pairs.get(0);
	
		assertThat(todayPairs.getPairByTrack("track2"), is(soloPair));
		
		subject.rotateSoloPairIfAny(todayPairs, pairs.subList(1, pairs.size()), pairsWeight);
		
		assertThat(todayPairs.getPairByTrack("track2"), is(not(soloPair)));
	}
	
	@Test
	public void testRotateSoloPairWithoutState() throws Exception {
		List<DayPairs> pairs = new ArrayList<>();
		Map<Pair, Integer> pairsWeight = subject.buildPairsWeight(pairs, Arrays.asList("dev1", "dev2", "dev3"));

		subject.rotateSoloPairIfAny(new DayPairs(), pairs, pairsWeight);
	}
	
	private Map<String, List<String>> getPairs(){
		HashMap<String, List<String>> result = new HashMap<String, List<String>>();
		result.put("track1", Arrays.asList("dev1", "dev2"));
		result.put("track2", Arrays.asList("dev3", "dev4"));
		
		return result;
	}

	private TList getListWithCards(String listName, String firstCardName, String secondCardName, Map<String, List<String>> members) {
		TList tList = new TList();
		tList.setName(listName);
		Card firstCard = new Card();
		firstCard.setName(firstCardName);
		Card secondCard = new Card();
		secondCard.setName(secondCardName);
		if(members != null){
			firstCard.setIdMembers(members.get(firstCardName));
			secondCard.setIdMembers(members.get(secondCardName));
		}
		tList.setCards(Arrays.asList(firstCard, secondCard));

		return tList;
	}

	private Date getPastDate(int daysCountToPast) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -(daysCountToPast));
		return cal.getTime();
	}
}
