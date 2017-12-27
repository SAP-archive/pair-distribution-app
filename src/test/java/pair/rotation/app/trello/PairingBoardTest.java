package pair.rotation.app.trello;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


import org.junit.Test;

public class PairingBoardTest {

	@Test
	public void testParseDevOpsCompany() {
		PairingBoard pairingBoard = new PairingBoard(null, null, null);
		
		assertThat(pairingBoard.parseDevOpsCompanies("devops:company"), is(new String[] {"company"}));
		assertThat(pairingBoard.parseDevOpsCompanies("devops:company,companyb"), is(new String[] {"company", "companyb"}));
		assertThat(pairingBoard.parseDevOpsCompanies("devops:"), is(new String[] {}));
	}
	
}
