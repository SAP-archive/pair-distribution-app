package pair.rotation.app.trello;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import org.junit.Test;

import pair.rotation.app.trello.entities.Developer;

public class DeveloperTest {

	@Test
	public void testId() {
		Developer developer = new Developer("developerId");
		
		assertThat(developer.getId(), is("developerId"));
	}

	@Test
	public void testCompanyDefault() {
		Developer developer = new Developer("developerId");
		
		assertThat(developer.getCompany(), is(""));
	}
	
	@Test
	public void testCompany() {
		Developer developer = new Developer("developerId");
		developer.setCompany("my-company");
		
		assertThat(developer.getCompany(), is("my-company"));
	}
	
	@Test
	public void testDoDDefault() {
		Developer developer = new Developer("developerId");
		
		assertThat(developer.getDoD(), is(false));
	}
	
	@Test
	public void testDoD() {
		Developer developer = new Developer("developerId");
		developer.setDoD(true);
		
		assertThat(developer.getDoD(), is(true));
	}
	
	@Test
	public void testNew() {
		Developer developer = new Developer("developerId");
		
		assertThat(developer.getNew(), is(false));
		
		developer.setNew(true);
		
		assertThat(developer.getNew(), is(true));
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
}
