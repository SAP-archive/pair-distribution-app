package pair.distribution.app.trello.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import pair.distribution.app.trello.entities.Developer;
import pair.distribution.app.trello.entities.Pair;

public class PairTest {

	@Test
	public void testGetAndSetDevs()  {
		Pair subject = new Pair();
		subject.setDevs(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		
		assertThat(subject.getDevs(), is(equalTo(Arrays.asList(new Developer("dev1"), new Developer("dev2")))));
	}
	
	@Test
	public void testGetAndSetDevsWithNullValues()  {
		Pair subject = new Pair();
		subject.setDevs(Arrays.asList(null, new Developer("dev2")));
		
		assertThat(subject.getDevs(), is(equalTo(Arrays.asList(new Developer("dev2")))));
	}
	
	@Test
	public void testAddDev()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.getDevs(), is(equalTo(Arrays.asList(new Developer("dev1")))));
	}
	
	@Test
	public void testAddDevWithNull()  {
		Pair subject = new Pair();
		
		subject.addDev(null);
		
		assertThat(subject.getDevs().isEmpty(), is(true));
	}
	
	@Test
	public void testHasDev()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.hasDev(new Developer("dev1")), is(true));
	}
	
	@Test
	public void testGetOtherDev()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		
		assertThat(subject.getOtherDev(new Developer("dev1")), is(equalTo(new Developer("dev2"))));
	}
	
	@Test
	public void testOtherDevWithOneDev()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.getOtherDev(new Developer("dev1")), nullValue());
	}
	
	@Test
	public void testIsComplete()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		
		assertThat(subject.isComplete(), is(true));
	}
	
	@Test
	public void testIsCompleteWithOneDev()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.isComplete(), is(false));
	}
	
	@Test
	public void testToString()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.toString(), is(equalTo("Pair [devs=[dev1], opsPair=false]")));
	}
	
	@Test
	public void testHashCode()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		Pair subject2 = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.hashCode(), is(equalTo(subject2.hashCode())));
	}
	
	@Test
	public void testHashCodeNotEqual()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		Pair subject2 = new Pair(Arrays.asList(new Developer("dev2")));
		
		assertThat(subject.hashCode(), is(not(equalTo(subject2.hashCode()))));
	}
	
	@Test
	public void testEqual()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		Pair subject2 = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.equals(subject2), is(true));
	}
	
	@Test
	public void testEqualWithOpsTrue()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		subject.setOpsPair(true);
		Pair subject2 = new Pair(Arrays.asList(new Developer("dev1")));
		subject2.setOpsPair(true);
		
		assertThat(subject.equals(subject2), is(true));
	}
	
	@Test
	public void testEqualDifferentPairs()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		Pair subject2 = new Pair(Arrays.asList(new Developer("dev2")));
		
		assertThat(subject.equals(subject2), is(false));
	}
	
	@Test
	public void testIsSolo()  {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		Pair subject2 = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		
		assertThat(subject.equals(subject2), is(false));
	}

	
	@Test
	public void testIsBuildPairFalse()  {
		Pair subject = new Pair();
		
		subject.setBuildPair(false);
		
		assertThat(subject.isBuildPair(), is(false));
	}
	
	@Test
	public void testIsBuildPairTrue()  {
		Pair subject = new Pair();
		
		subject.setBuildPair(true);
		
		assertThat(subject.isBuildPair(), is(true));
	}
	
	@Test
	public void testIsCommunitydPairFalse()  {
		Pair subject = new Pair();
		
		subject.setCommunityPair(false);
		
		assertThat(subject.isCommunityPair(), is(false));
	}
	
	@Test
	public void testIsOpsPairTrue()  {
		Pair subject = new Pair();
		
		subject.setOpsPair(true);
		
		assertThat(subject.isOpsPair(), is(true));
	}
	
	@Test
	public void testIsOpsPairFalse()  {
		Pair subject = new Pair();
		
		subject.setOpsPair(false);
		
		assertThat(subject.isOpsPair(), is(false));
	}
	
	@Test
	public void testIsCommunityPairTrue()  {
		Pair subject = new Pair();
		
		subject.setCommunityPair(true);
		
		assertThat(subject.isCommunityPair(), is(true));
	}
}
