package pair.rotation.app.trello.entities;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CompanyTest {

	@Test
	public void testGetCompanyName() {
		assertThat(new Company("company").getName(), is("company"));
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
	public void testEqual() {
		assertThat(new Company("company"), is(new Company("company")));
	}
}
