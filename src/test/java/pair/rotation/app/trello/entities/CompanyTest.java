package pair.rotation.app.trello.entities;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

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
		assertThat(new Company("Company").getTrack(), is("COMPANY-internal"));
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
}
