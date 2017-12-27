package pair.rotation.app.trello.entities;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;
import org.springframework.data.annotation.Transient;

import pair.rotation.app.helpers.DayPairsHelper;
import pair.rotation.app.trello.entities.DayPairs;
import pair.rotation.app.trello.entities.Developer;
import pair.rotation.app.trello.entities.Pair;

public class DayPairsTest {

	@Test
	public void testGetPairsNewInstance() {
		assertThat(new DayPairs().getPairs().isEmpty(), is(true));
	}
	
	@Test
	public void testAddPairAndGetTracks(){
		HashMap<String, Pair> expectedPairs = new HashMap<String, Pair>();
		expectedPairs.put("testTrack", new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2"))));
		DayPairs pairs = new DayPairs();
		pairs.addPair("testTrack", expectedPairs.get("testTrack"));
		
		assertThat(pairs.getPairs(), is(equalTo(expectedPairs)));
		assertThat(pairs.getTracks(), is(equalTo(new HashSet<>(Arrays.asList("testTrack")))));
	}

	@Test
	public void testSetDate() throws Exception {
		DayPairs pairs = new DayPairs();
		Date expectedDate = new Date();
		pairs.setDate(expectedDate);
		
		assertThat(pairs.getDate(), is(equalTo(getDateWithoutTime(expectedDate))));
	}
	
	@Test
	public void testGetDate() throws Exception {
		assertThat(new DayPairs().getDate(), is(equalTo(getDateWithoutTime(new Date()))));
	}

	private Date getDateWithoutTime(Date date) throws ParseException {
		return new DayPairs().parse(new DayPairs().format(date));
	}
	
	@Test
	public void testCompareTo() throws Exception {
		DayPairs todaysPairs = new DayPairs();
		todaysPairs.setDate(new Date());
		DayPairs yesterdayPairs = new DayPairs();
		yesterdayPairs.setDate(getYesterdayDate());
		
		assertThat(todaysPairs.compareTo(yesterdayPairs), is(equalTo(1)));
		assertThat(yesterdayPairs.compareTo(todaysPairs), is(equalTo(-1)));
		assertThat(todaysPairs.compareTo(todaysPairs), is(equalTo(0)));
	}
	
	@Test
	public void testGetPairByTrack() throws Exception {
		Pair pair1 = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		Pair pair2 = new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")));
		DayPairs pairs = new DayPairs();
		pairs.addPair("track1", pair1);
		pairs.addPair("track2", pair2);
		
		assertThat(pairs.getPairByTrack("track1"), is(equalTo(pair1)));
		assertThat(pairs.getPairByTrack("track2"), is(equalTo(pair2)));
	}
	
	@Test
	public void testHashCode() throws Exception {
		DayPairs pairsOfToday = new DayPairs();
		DayPairs differentPairsOfToday = new DayPairs();
		DayPairs yesterdayPairs = new DayPairs();
		yesterdayPairs.setDate(getYesterdayDate());
		Pair pair1 = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		Pair pair2 = new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")));
		pairsOfToday.addPair("track1", pair1);
		differentPairsOfToday.addPair("track2", pair2);
		yesterdayPairs.addPair("track1", pair1);
		
		assertThat(pairsOfToday.hashCode(), is(equalTo(differentPairsOfToday.hashCode())));
		assertThat(yesterdayPairs.hashCode(), is(not(equalTo(pairsOfToday.hashCode()))));
	}
	
	@Test
	public void testEquals() throws Exception {
		DayPairs pairsOfToday = new DayPairs();
		DayPairs differentPairsOfToday = new DayPairs();
		DayPairs yesterdayPairs = new DayPairs();
		yesterdayPairs.setDate(getYesterdayDate());
		Pair pair1 = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		Pair pair2 = new Pair(Arrays.asList(new Developer("dev3"), new Developer("dev4")));
		pairsOfToday.addPair("track1", pair1);
		differentPairsOfToday.addPair("track2", pair2);
		yesterdayPairs.addPair("track1", pair1);
		
		assertThat(pairsOfToday, is(equalTo(differentPairsOfToday)));
		assertThat(yesterdayPairs, is(not(equalTo(pairsOfToday))));
	}
	
	@Test
	public void testToString() throws Exception {
		DayPairs pairs = new DayPairs();
		Pair pair = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		pairs.addPair("track", pair);
		
		assertThat(pairs.toString(), is(equalTo("Pairs [pairs=" + pairs.getPairs() + ", date=" + pairs.format(pairs.getDate()) + "]")));
	}
	
	@Test
	public void testHasPair() throws Exception {
		DayPairs pairs = new DayPairs();
		Pair pair = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		Pair differentPair = new Pair();
		pairs.addPair("track", pair);
		
		assertThat(pairs.hasPair(pair), is(true));
		assertThat(pairs.hasPair(differentPair), is(false));
	}
	
	@Test
	public void testReplacePairWith() throws Exception {
		DayPairs pairs = new DayPairs();
		Pair pair = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		Pair differentPair = new Pair();
		pairs.addPair("track", pair);
		
		assertThat(pairs.hasPair(pair), is(true));
		assertThat(pairs.hasPair(differentPair), is(false));
		
		pairs.replacePairWith(pair, differentPair);
		
		assertThat(pairs.hasPair(pair), is(false));
		assertThat(pairs.hasPair(differentPair), is(true));
	}
	
	@Test
	public void testGetTrackByPair() throws Exception {
		DayPairs pairs = new DayPairs();
		Pair pair = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		Pair differentPair = new Pair();
		pairs.addPair("track", pair);
		
		assertThat(pairs.getTrackByPair(pair), is(equalTo("track")));
		assertThat(pairs.getTrackByPair(differentPair), is(nullValue()));
	}
	
	@Test
	public void testSimpleDateFormatNotPersisted() throws Exception {
		DayPairs pairs = new DayPairs();
		Field dateFormatterField = pairs.getClass().getDeclaredField("dateFormatter");
		dateFormatterField.setAccessible(true);
		Transient annotation = dateFormatterField.getAnnotation(Transient.class);
		
		assertThat(annotation, is(not(nullValue())));
	}
	
	private Date getYesterdayDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		return cal.getTime();
	}
}
