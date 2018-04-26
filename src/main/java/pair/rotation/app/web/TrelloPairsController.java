package pair.rotation.app.web;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pair.rotation.app.helpers.DayPairsHelper;
import pair.rotation.app.persistence.mongodb.TrelloPairsRepository;
import pair.rotation.app.trello.PairingBoard;
import pair.rotation.app.trello.entities.Company;
import pair.rotation.app.trello.entities.DayPairs;
import pair.rotation.app.trello.entities.DevPairCombinations;
import pair.rotation.app.trello.entities.Developer;
import pair.rotation.app.trello.entities.OpsPairCombinations;
import pair.rotation.app.trello.entities.Pair;
import pair.rotation.app.trello.entities.PairCombinations;



@RestController
public class TrelloPairsController {
   
    private static final Logger logger = LoggerFactory.getLogger(TrelloPairsController.class);
    private boolean rotate_everyday = false;
    
    private TrelloPairsRepository repository;
	@Value("${trello.access.key}")
	private String accessKey;
	@Value("${trello.application.key}")
	private String applicationKey;
	@Value("${trello.pairing.board}")
	private String pairingBoardId;
	
    @Autowired
    public TrelloPairsController(TrelloPairsRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/pairs/trello", method = RequestMethod.GET)
    public DayPairs pairs() {
    		return generatePairs(0);
    }
    
    @RequestMapping(value = "/pairs/test/trello", method = RequestMethod.GET)
    public DayPairs pairs(@RequestParam("days") int daysIntoFuture ) {
    		return generatePairs(daysIntoFuture);
    }
    
    @RequestMapping(value = "/pairs/rotate", method = RequestMethod.PUT)
    public void pairs(@RequestParam("everyday") boolean everyday ) {
    		rotate_everyday = everyday;
    }

	private DayPairs generatePairs(int daysIntoFuture) {
		PairingBoard pairingBoardTrello = new PairingBoard(accessKey, applicationKey, pairingBoardId);
		logger.info("Pairing board found. Syncing state now");
		pairingBoardTrello.syncTrelloBoardState();
		logger.info("Syncing state finished. Updating database state");
		DayPairsHelper pairsHelper = new DayPairsHelper(repository);
		pairsHelper.updateDataBaseWithTrelloContent(pairingBoardTrello.getPastPairs());
		List<DayPairs> pastPairs = repository.findAll();
		logger.info("Database state is: " + pastPairs.toString());
		PairCombinations pairCombination = new DevPairCombinations(pastPairs);
		OpsPairCombinations devOpsPairCombination = new OpsPairCombinations(pastPairs, daysIntoFuture);
		
		List<DayPairs> todayDevOpsPairs = generateTodayOpsPairs(pairingBoardTrello, pairsHelper, devOpsPairCombination, pairingBoardTrello.getDevs(), pairingBoardTrello.getDevOpsCompanies());
		DayPairs todayPairs = generateTodayDevPairs(pairingBoardTrello, pairsHelper, pairCombination, getTodayDevelopers(pairingBoardTrello, todayDevOpsPairs));
		todayDevOpsPairs.stream().forEach(devOpsPairs -> todayPairs.addPiars(devOpsPairs.getPairs()));
		
		pairingBoardTrello.addTodayPairsToBoard(todayPairs, daysIntoFuture);
		logger.info("Trello board has been updated");
		
		return todayPairs;
	}

	private List<Developer> getTodayDevelopers(PairingBoard pairingBoardTrello, List<DayPairs> todayDevOpsPairs) {
		List<Developer> todayDevDevelopers = new ArrayList<>(pairingBoardTrello.getDevs());
		todayDevOpsPairs.stream().forEach(dayPairs -> { dayPairs.getPairs().values().stream().forEach(pair -> todayDevDevelopers.removeAll(pair.getDevs())); });
		return todayDevDevelopers;
	}

	private DayPairs generateTodayDevPairs(PairingBoard pairingBoardTrello, DayPairsHelper pairsHelper, PairCombinations pairCombination, List<Developer> todayDevs) {
		Map<Pair, Integer> pairsWeight = pairsHelper.buildPairsWeightFromPastPairing(pairCombination, todayDevs);
		logger.info("Pairs weight is:" + pairsWeight);
		logger.info("Building build pairs weight");
		Map<Pair, Integer> buildPairsWeight = pairsHelper.buildBuildPairsWeightFromPastPairing(pairCombination, todayDevs);
		logger.info("BuildPairs weight is:" + buildPairsWeight);
		Map<Pair, Integer> communityPairsWeight = pairsHelper.buildCommunityPairsWeightFromPastPairing(pairCombination, todayDevs);
		logger.info("CommunityPairs weight is:" + communityPairsWeight);
		pairsHelper.adaptPairsWeight(pairsWeight, todayDevs);
		logger.info("Pairs weight after adaptation:" + pairsWeight);
		logger.info("Tracks are: " + pairingBoardTrello.getTracks() + " today devs are: " + todayDevs);
		DayPairs todayDevPairs = pairsHelper.generateNewDayPairs(pairingBoardTrello.getTracks(), todayDevs, pairCombination, pairsWeight, rotate_everyday, pairingBoardTrello.getCompanies());
		logger.info("Today pairs are: " + todayDevPairs);
		pairsHelper.rotateSoloPairIfAny(todayDevPairs, pairCombination, pairsWeight);
		logger.info("After single pair rotation they are: " + todayDevPairs);
		logger.info("Setting BuildPair");
		pairsHelper.setBuildPair(todayDevPairs.getPairs().values(), buildPairsWeight);
		logger.info("Setting CommunityPair");
		pairsHelper.setCommunityPair(todayDevPairs.getPairs().values(), communityPairsWeight);
		logger.info("After setting build pair pairs are: " + todayDevPairs);
		return todayDevPairs;
	}

	private List<DayPairs> generateTodayOpsPairs(PairingBoard pairingBoardTrello, DayPairsHelper pairsHelper, OpsPairCombinations devOpsPairCombination,
			List<Developer> todayDevs, List<Company> devOpsCompanies) {
		List<DayPairs> todayPairs = new ArrayList<>();
		for (Company company : devOpsCompanies) {
			List<Developer> companyDevs = company.getCompanyExperiencedDevs(todayDevs);
			logger.info("Company :" + company.getName() + "devs are: " + companyDevs);
			Map<Pair, Integer> companyDevOpsPairsWeight = pairsHelper.buildPairsWeightFromPastPairing(devOpsPairCombination, companyDevs);
			logger.info("DevOpsPairs weight for company: " + company.getName() + " is " + companyDevOpsPairsWeight);
			DayPairs dayPairs = pairsHelper.generateNewDayPairs(Arrays.asList(company.getTrack()), companyDevs, devOpsPairCombination, companyDevOpsPairsWeight, rotate_everyday, pairingBoardTrello.getCompanies());
			dayPairs.getPairs().values().stream().forEach(pair -> pair.setOpsPair(true));
			todayPairs.add(dayPairs);
			logger.info("Today DevOpsPairs for company: " + company.getName() + " are " + todayPairs);
		}
		return todayPairs;
	}
}