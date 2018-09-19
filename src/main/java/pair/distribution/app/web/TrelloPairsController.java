package pair.distribution.app.web;
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

import pair.distribution.app.helpers.DayPairsHelper;
import pair.distribution.app.persistence.mongodb.TrelloPairsRepository;
import pair.distribution.app.trello.PairingBoard;
import pair.distribution.app.trello.entities.Company;
import pair.distribution.app.trello.entities.DayPairs;
import pair.distribution.app.trello.entities.DevPairCombinations;
import pair.distribution.app.trello.entities.Developer;
import pair.distribution.app.trello.entities.OpsPairCombinations;
import pair.distribution.app.trello.entities.Pair;
import pair.distribution.app.trello.entities.PairCombinations;



@RestController
public class TrelloPairsController {
   
    private static final Logger logger = LoggerFactory.getLogger(TrelloPairsController.class);
    private boolean rotateEveryday = false;
    
    private TrelloPairsRepository repository;
	@Value("${trello.api.token}")
	private String apiToken;
	@Value("${trello.api.key}")
	private String apiKey;
	@Value("${trello.pairing.board.id}")
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
    		rotateEveryday = everyday;
    }

	private DayPairs generatePairs(int daysIntoFuture) {
		PairingBoard pairingBoardTrello = new PairingBoard(apiToken, apiKey, pairingBoardId);
		pairingBoardTrello.syncTrelloBoardState();
		logger.info("Syncing state finished. Updating database state");
		DayPairsHelper pairsHelper = new DayPairsHelper(repository);
		pairsHelper.updateDataBaseWithTrelloContent(pairingBoardTrello.getPastPairs());
		List<DayPairs> pastPairs = repository.findAll();
		logger.info("Database state is: {}", pastPairs);
		PairCombinations pairCombination = new DevPairCombinations(pastPairs);
		OpsPairCombinations devOpsPairCombination = new OpsPairCombinations(pastPairs, daysIntoFuture);
		
		List<DayPairs> todayDevOpsPairs = generateTodayOpsPairs(pairingBoardTrello, pairsHelper, devOpsPairCombination, pairingBoardTrello.getDevs(), pairingBoardTrello.getDevOpsCompanies());
		DayPairs todayPairs = generateTodayDevPairs(pairingBoardTrello, pairsHelper, pairCombination, getTodayDevelopers(pairingBoardTrello, todayDevOpsPairs), !todayDevOpsPairs.isEmpty());
		todayDevOpsPairs.stream().forEach(devOpsPairs -> todayPairs.addPiars(devOpsPairs.getPairs()));
		
		pairingBoardTrello.addTodayPairsToBoard(todayPairs, daysIntoFuture);
		logger.info("Trello board has been updated");
		
		return todayPairs;
	}

	private List<Developer> getTodayDevelopers(PairingBoard pairingBoardTrello, List<DayPairs> todayDevOpsPairs) {
		List<Developer> todayDevDevelopers = new ArrayList<>(pairingBoardTrello.getDevs());
		todayDevOpsPairs.stream().forEach(dayPairs -> dayPairs.getPairs().values().stream().forEach(pair -> todayDevDevelopers.removeAll(pair.getDevs())));
		return todayDevDevelopers;
	}

	private DayPairs generateTodayDevPairs(PairingBoard pairingBoardTrello, DayPairsHelper pairsHelper, PairCombinations pairCombination, List<Developer> todayDevs, boolean opsPair) {
		Map<Pair, Integer> pairsWeight = pairsHelper.buildPairsWeightFromPastPairing(pairCombination, todayDevs);
		logger.info("Pairs weight is: {}", pairsWeight);
		pairsHelper.buildDevelopersPairingDays(pairCombination, todayDevs);
		pairsHelper.adaptPairsWeight(pairsWeight, todayDevs);
		logger.info("Pairs weight after adaptation: {}", pairsWeight);
		pairsHelper.buildDevelopersTracksWeightFromPastPairing(pairCombination, todayDevs);
		logger.info("Tracks are: {} today devs are: {}", pairingBoardTrello.getTracks(), todayDevs);
		DayPairs todayDevPairs = pairsHelper.generateNewDayPairs(pairingBoardTrello.getTracks(), todayDevs, pairCombination, pairsWeight, rotateEveryday, pairingBoardTrello.getCompanies());
		logger.info("Today pairs are: {}",  todayDevPairs);
		pairsHelper.rotateSoloPairIfAny(todayDevPairs, pairCombination, pairsWeight);
		logger.info("After single pair rotation they are: {}", todayDevPairs);

		if(!opsPair) {
			Map<Pair, Integer> buildPairsWeight = pairsHelper.buildBuildPairsWeightFromPastPairing(pairCombination, todayDevs);
			Map<Pair, Integer> communityPairsWeight = pairsHelper.buildCommunityPairsWeightFromPastPairing(pairCombination, todayDevs);
			logger.info("CommunityPairs weight is: {} BuildPairs weight is: {}", communityPairsWeight, buildPairsWeight);
			pairsHelper.setBuildPair(todayDevPairs.getPairs().values(), buildPairsWeight);
			pairsHelper.setCommunityPair(todayDevPairs.getPairs().values(), communityPairsWeight);
			logger.info("After setting build pair pairs are: {}", todayDevPairs);
		}
		return todayDevPairs;
	}

	private List<DayPairs> generateTodayOpsPairs(PairingBoard pairingBoardTrello, DayPairsHelper pairsHelper, OpsPairCombinations devOpsPairCombination,
			List<Developer> todayDevs, List<Company> devOpsCompanies) {
		List<DayPairs> todayPairs = new ArrayList<>();

		for (Company company : devOpsCompanies) {
			List<Developer> companyDevs = company.getCompanyExperiencedDevs(todayDevs);
			logger.info("Company : {} devs are: {}", company.getName(), companyDevs);
			Map<Pair, Integer> companyDevOpsPairsWeight = pairsHelper.buildPairsWeightFromPastPairing(devOpsPairCombination, companyDevs);
			logger.info("DevOpsPairs weight for company: {} is {}", company.getName(), companyDevOpsPairsWeight);
			DayPairs dayPairs = pairsHelper.generateNewDayPairs(Arrays.asList(company.getTrack()), companyDevs, devOpsPairCombination, companyDevOpsPairsWeight, rotateEveryday, pairingBoardTrello.getCompanies());
			dayPairs.getPairs().values().stream().forEach(pair -> { pair.setOpsPair(true); pair.setBuildPair(true); pair.setCommunityPair(true); });
			todayPairs.add(dayPairs);
			logger.info("Today DevOpsPairs for company: {} are {}", company.getName(), todayPairs);
		}
		
		return todayPairs;
	}
}