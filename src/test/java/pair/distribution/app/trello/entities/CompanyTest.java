package pair.distribution.app.trello.entities;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import pair.distribution.app.trello.entities.Company;
import pair.distribution.app.trello.entities.Developer;

public class CompanyTest {

	@Test
	public void testGetCompanyName() {
		assertThat(new Company("company").getName(), is("company"));
	}
	
	@Test
	public void testGetCompanyNameWithSaces() {
		assertThat(new Company("  company  ").getName(), is("company"));
	}
	
	@Test
	public void testGetCompanyNameWithUpperCase() {
		assertThat(new Company("COMPANY").getName(), is("company"));
	}
	
	@Test
	public void testGetCompanyOriginalName() {
		assertThat(new Company("Company").getOriginalName(), is("Company"));
	}
	
	
	@Test
	public void testGetTrack() {
		assertThat(new Company("Company").getTrack(), is("COMPANY-ops/interrupt"));
	}
	
	@Test
	public void testGetCompanyExperiencedDevs() {
		Developer developerCompanyA = new Developer("a");
		developerCompanyA.setCompany(new Company("a"));
		Developer newDeveloperCompanyA = new Developer("a");
		newDeveloperCompanyA.setCompany(new Company("a"));
		newDeveloperCompanyA.setNew(true);
		Developer developerCompanyB = new Developer("b");
		developerCompanyB.setCompany(new Company("b"));
		
		List<Developer> companyDevs = new Company("a").getCompanyExperiencedDevs(Arrays.asList(developerCompanyA, developerCompanyB, newDeveloperCompanyA));
		
		assertThat(companyDevs.size(), is(1));
		assertThat(companyDevs.get(0), is(developerCompanyA));
	}
	
	@Test
	public void testGetCompanyTracks() {
		List<String> tracks = Arrays.asList("other-company-track", "company-track", "companyB-track");
		
		String companyTrack = new Company("Company").getCompanyTrack(tracks);
		
		assertThat(companyTrack, is("company-track"));
	}
	
	@Test
	public void testGetCompanyTracksNoHit() {
		List<String> tracks = Arrays.asList("other-company-track", "third-track");
		
		String companyTrack = new Company("Company").getCompanyTrack(tracks);
		
		assertThat(companyTrack, is(nullValue()));
	}
	
	@Test
	public void testIsCompanyTrack() {
		boolean isCompanyTrack = new Company("Company").isCompanyTrack("company-track");
		
		assertThat(isCompanyTrack, is(true));
	}
	
	@Test
	public void testIsCompanyTrackFalse() {
		boolean isCompanyTrack = new Company("Company").isCompanyTrack("companyB-track");
		
		assertThat(isCompanyTrack, is(false));
	}
	
	@Test
	public void testGetCompanyDevs() {
		Developer developerCompanyA = new Developer("a");
		developerCompanyA.setCompany(new Company("a"));
		Developer newDeveloperCompanyA = new Developer("a");
		newDeveloperCompanyA.setCompany(new Company("a"));
		newDeveloperCompanyA.setNew(true);
		Developer developerCompanyB = new Developer("b");
		developerCompanyB.setCompany(new Company("b"));
		
		List<Developer> companyDevs = new Company("a").getDevs(Arrays.asList(developerCompanyA, developerCompanyB, newDeveloperCompanyA));
		
		assertThat(companyDevs, is(Arrays.asList(developerCompanyA, newDeveloperCompanyA)));
	}
}
