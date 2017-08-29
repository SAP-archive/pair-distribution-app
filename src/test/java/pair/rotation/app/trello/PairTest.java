package pair.rotation.app.trello;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class PairTest {

	@Test
	public void testGetAndSetDevs() throws Exception {
		Pair subject = new Pair();
		subject.setDevs(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		
		assertThat(subject.getDevs(), is(equalTo(Arrays.asList(new Developer("dev1"), new Developer("dev2")))));
	}
	
	@Test
	public void testGetAndSetDevsWithNullValues() throws Exception {
		Pair subject = new Pair();
		subject.setDevs(Arrays.asList(null, new Developer("dev2")));
		
		assertThat(subject.getDevs(), is(equalTo(Arrays.asList(new Developer("dev2")))));
	}
	
	@Test
	public void testAddDev() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.getDevs(), is(equalTo(Arrays.asList(new Developer("dev1")))));
	}
	
	@Test
	public void testAddDevWithNull() throws Exception {
		Pair subject = new Pair();
		
		subject.addDev(null);
		
		assertThat(subject.getDevs().isEmpty(), is(true));
	}
	
	@Test
	public void testHasDev() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.hasDev(new Developer("dev1")), is(true));
	}
	
	@Test
	public void testGetOtherDev() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		
		assertThat(subject.getOtherDev(new Developer("dev1")), is(equalTo(new Developer("dev2"))));
	}
	
	@Test
	public void testOtherDevWithOneDev() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.getOtherDev(new Developer("dev1")), nullValue());
	}
	
	@Test
	public void testIsComplete() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		
		assertThat(subject.isComplete(), is(true));
	}
	
	@Test
	public void testIsCompleteWithOneDev() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.isComplete(), is(false));
	}
	
	@Test
	public void testToString() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.toString(), is(equalTo("Pair [devs=[dev1]]")));
	}
	
	@Test
	public void testHashCode() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		Pair subject2 = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.hashCode(), is(equalTo(subject2.hashCode())));
	}
	
	@Test
	public void testHashCodeNotEqual() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		Pair subject2 = new Pair(Arrays.asList(new Developer("dev2")));
		
		assertThat(subject.hashCode(), is(not(equalTo(subject2.hashCode()))));
	}
	
	@Test
	public void testEqual() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		Pair subject2 = new Pair(Arrays.asList(new Developer("dev1")));
		
		assertThat(subject.equals(subject2), is(true));
	}
	
	@Test
	public void testEqualDifferentPairs() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		Pair subject2 = new Pair(Arrays.asList(new Developer("dev2")));
		
		assertThat(subject.equals(subject2), is(false));
	}
	
	@Test
	public void testIsSolo() throws Exception {
		Pair subject = new Pair(Arrays.asList(new Developer("dev1")));
		Pair subject2 = new Pair(Arrays.asList(new Developer("dev1"), new Developer("dev2")));
		
		assertThat(subject.equals(subject2), is(false));
	}
		
	
	@Test
	public void testGetDevFromCompany() throws Exception {
		Developer developer = new Developer("dev1");
		developer.setCompany("company");
		Developer developer2 = new Developer("dev2");
		developer2.setCompany("someOtherCompany");
		Pair subject = new Pair(Arrays.asList(developer, developer2));
		
		assertThat(subject.getDevFromCompany("company"), is(developer));
	}
	
	@Test
	public void testGetDevFromCompanyNoDev() throws Exception {
		Developer developer = new Developer("dev1");
		developer.setCompany("someOtherCompany");
		Developer developer2 = new Developer("dev2");
		developer2.setCompany("someOtherCompany");
		Pair subject = new Pair(Arrays.asList(developer, developer2));
		
		assertThat(subject.getDevFromCompany("company"), is(nullValue()));
	}
	
	@Test
	public void testIsPairFromSameCompanyFalse() throws Exception {
		Developer developer = new Developer("dev1");
		developer.setCompany("someCompany");
		Developer developer2 = new Developer("dev2");
		developer2.setCompany("someOtherCompany");
		Pair subject = new Pair(Arrays.asList(developer, developer2));
		
		assertThat(subject.isPairFromSameCompany(), is(false));
	}
	
	@Test
	public void testIsPairFromSameCompanyTrue() throws Exception {
		Developer developer = new Developer("dev1");
		developer.setCompany("someCompany");
		Developer developer2 = new Developer("dev2");
		developer2.setCompany("someCompany");
		Pair subject = new Pair(Arrays.asList(developer, developer2));
		
		assertThat(subject.isPairFromSameCompany(), is(true));
	}
	
	@Test
	public void testIsPairFromSameCompanyForSolo() throws Exception {
		Developer developer = new Developer("dev1");
		developer.setCompany("someCompany");
		Pair subject = new Pair(Arrays.asList(developer));
		
		assertThat(subject.isPairFromSameCompany(), is(true));
	}
	
	@Test
	public void testIsBuildPairFalse() throws Exception {
		Pair subject = new Pair();
		
		subject.setBuildPair(false);
		
		assertThat(subject.isBuildPair(), is(false));
	}
	
	@Test
	public void testIsBuildPairTrue() throws Exception {
		Pair subject = new Pair();
		
		subject.setBuildPair(true);
		
		assertThat(subject.isBuildPair(), is(true));
	}
	
	@Test
	public void testIsCommunitydPairFalse() throws Exception {
		Pair subject = new Pair();
		
		subject.setCommunityPair(false);
		
		assertThat(subject.isCommunityPair(), is(false));
	}
	
	@Test
	public void testIsCommunityPairTrue() throws Exception {
		Pair subject = new Pair();
		
		subject.setCommunityPair(true);
		
		assertThat(subject.isCommunityPair(), is(true));
	}
}
