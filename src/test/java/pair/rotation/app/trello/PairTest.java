package pair.rotation.app.trello;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;

import org.junit.Test;

import pair.rotation.app.trello.Pair;

public class PairTest {

	@Test
	public void testGetAndSetDevs() throws Exception {
		Pair subject = new Pair();
		subject.setDevs(Arrays.asList("dev1", "dev2"));
		
		assertThat(subject.getDevs(), is(equalTo(Arrays.asList("dev1", "dev2"))));
	}
	
	@Test
	public void testAddDev() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1"));
		
		assertThat(subject.getDevs(), is(equalTo(Arrays.asList("dev1"))));
	}
	
	@Test
	public void testAddDevWithNull() throws Exception {
		Pair subject = new Pair();
		
		subject.addDev(null);
		
		assertThat(subject.getDevs().isEmpty(), is(true));
	}
	
	@Test
	public void testHasDev() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1"));
		
		assertThat(subject.hasDev("dev1"), is(true));
	}
	
	@Test
	public void testGetOtherDev() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1", "dev2"));
		
		assertThat(subject.getOtherDev("dev1"), is(equalTo("dev2")));
	}
	
	@Test
	public void testOtherDevWithOneDev() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1"));
		
		assertThat(subject.getOtherDev("dev1"), nullValue());
	}
	
	@Test
	public void testIsComplete() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1", "dev2"));
		
		assertThat(subject.isComplete(), is(true));
	}
	
	@Test
	public void testIsCompleteWithOneDev() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1"));
		
		assertThat(subject.isComplete(), is(false));
	}
	
	@Test
	public void testToString() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1"));
		
		assertThat(subject.toString(), is(equalTo("Pair [devs=[dev1]]")));
	}
	
	@Test
	public void testHashCode() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1"));
		Pair subject2 = new Pair(Arrays.asList("dev1"));
		
		assertThat(subject.hashCode(), is(equalTo(subject2.hashCode())));
	}
	
	@Test
	public void testHashCodeNotEqual() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1"));
		Pair subject2 = new Pair(Arrays.asList("dev2"));
		
		assertThat(subject.hashCode(), is(not(equalTo(subject2.hashCode()))));
	}
	
	@Test
	public void testEqual() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1"));
		Pair subject2 = new Pair(Arrays.asList("dev1"));
		
		assertThat(subject.equals(subject2), is(true));
	}
	
	@Test
	public void testEqualDifferentPairs() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1"));
		Pair subject2 = new Pair(Arrays.asList("dev2"));
		
		assertThat(subject.equals(subject2), is(false));
	}
	
	@Test
	public void testIsSolo() throws Exception {
		Pair subject = new Pair(Arrays.asList("dev1"));
		Pair subject2 = new Pair(Arrays.asList("dev1", "dev2"));
		
		assertThat(subject.equals(subject2), is(false));
	}
}
