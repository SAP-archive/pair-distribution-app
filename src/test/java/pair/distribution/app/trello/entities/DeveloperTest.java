package pair.distribution.app.trello.entities;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import org.junit.Test;

import pair.distribution.app.trello.entities.Company;
import pair.distribution.app.trello.entities.Developer;

public class DeveloperTest {

	@Test
	public void testId() {
		Developer developer = new Developer("developerId");
		
		assertThat(developer.getId(), is("developerId"));
	}

	@Test
	public void testCompanyDefault() {
		Developer developer = new Developer("developerId");
		
		assertThat(developer.getCompany().getName(), is(""));
	}
	
	@Test
	public void testCompany() {
		Developer developer = new Developer("developerId");
		developer.setCompany(new Company("my-company"));
		
		assertThat(developer.getCompany().getName(), is("my-company"));
	}
	
	@Test
	public void testNew() {
		Developer developer = new Developer("developerId");
		
		assertThat(developer.getNew(), is(false));
		
		developer.setNew(true);
		
		assertThat(developer.getNew(), is(true));
	}
	
	@Test
	public void testHasContext() {
		Developer developer = new Developer("developerId");
		
		assertThat(developer.hasContext(), is(false));
		
		developer.setHasContext(true);
		
		assertThat(developer.hasContext(), is(true));
	}
	
	@Test
	public void testCompareTo() {
		Developer developer = new Developer("developerId");
		Developer developer2 = new Developer("developerId2");
		
		assertThat(developer.getId().compareTo(developer2.getId()), is(-1));
		assertThat(developer2.getId().compareTo(developer.getId()), is(1));
		assertThat(developer.getId().compareTo(developer.getId()), is(0));
	}
	
	@Test
	public void testHashCodeOfEqualInstances() {
		Developer developer = new Developer("developerId");
		Developer sameDeveloper = new Developer("developerId");
		
		assertThat(developer.hashCode(), is(sameDeveloper.hashCode()));
	}
	
	@Test
	public void testHashCodeOfDifferentInstances() {
		Developer developer = new Developer("developerId");
		Developer differentDeveloper = new Developer("developerId2");
		
		assertThat(developer.hashCode(), is(not(differentDeveloper.hashCode())));
	}
	
	@Test
	public void testEqualsOfEqualInstances() {
		Developer developer = new Developer("developerId");
		Developer sameDeveloper = new Developer("developerId");
		
		assertThat(developer.equals(sameDeveloper), is(true));
		assertThat(sameDeveloper.equals(developer), is(true));
	}
	
	@Test
	public void testEqualsOfDifferentInstances() {
		Developer developer = new Developer("developerId");
		Developer differentDeveloper = new Developer("developerId2");
		
		assertThat(developer.equals(differentDeveloper), is(false));
		assertThat(differentDeveloper.equals(developer), is(false));
	}
	
	@Test
	public void testToString() {
		Developer developer = new Developer("developerId");
		
		assertThat(developer.toString(), is("developerId"));
	}
	
	@Test
	public void testGetTrackWeightDefault() {
		Developer developer = new Developer("developerId");
		
		assertThat(developer.getTrackWeight("track"), is(0));
	}
	
	@Test
	public void testGetTrackWeightOne() {
		Developer developer = new Developer("developerId");
		
		developer.updateTrackWeight("track");
		
		assertThat(developer.getTrackWeight("track"), is(1));
	}
	
	@Test
	public void testGetPairingDaysDefault() {
		Developer developer = new Developer("developerId");
		
		assertThat(developer.getPairingDays(), is(0));
	}
	
	@Test
	public void testGetPairingDaysOne() {
		Developer developer = new Developer("developerId");
		
		developer.udpatePairingDays();
		
		assertThat(developer.getPairingDays(), is(1));
	}
}
