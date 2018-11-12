package pair.distribution.app.trello;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.TList;
import com.julienvey.trello.impl.TrelloImpl;
import com.julienvey.trello.impl.TrelloUrl;
import com.julienvey.trello.impl.http.RestTemplateHttpClient;

import pair.distribution.app.trello.entities.Company;
import pair.distribution.app.trello.entities.DayPairs;
import pair.distribution.app.trello.entities.Developer;
import pair.distribution.app.trello.entities.Pair;


public class PairingBoard {

	private static final String BUILD_PAIR_LABEL_COLOR = "orange";
	private static final String COMMUNITY_PAIR_LABEL_COLOR = "green";
	private static final String LOCK_LABEL_COLOR = "red";
	private static final String DEVOPS_PAIR_DESCRIPTION = "devops";
	private static final String CREATE_LISTS = "/lists?";
    private static final String GET_LIST_CARDS = "/lists/{listId}/cards?";
	private RestTemplateHttpClient httpClient;
	private TrelloImpl trelloImpl;
	private List<Developer> availableDevelopers;
	private List<Developer> allDevelopers;
	private List<Company> allCompanies;
	private List<Company> devOpsCompanies;
	private List<String> tracks;
	private List<DayPairs> pastPairs;
	private String apiToken;
	private String apiKey;
	private String pairingBoardId;
    
	public PairingBoard(String apiToken, String apiKey, String pairingBoardId) {
		this.apiToken = apiToken;
		this.apiKey = apiKey;
		this.pairingBoardId = pairingBoardId;
		availableDevelopers = new ArrayList<>();
		allDevelopers = new ArrayList<>();
		allCompanies = new ArrayList<>();
		devOpsCompanies = new ArrayList<>();
		httpClient = new RestTemplateHttpClient();
		trelloImpl = new TrelloImpl(apiKey, apiToken, httpClient);
	}
    
	public List<Developer> getDevs() {
		return allDevelopers.stream().filter(developer -> availableDevelopers.contains(developer)).collect(Collectors.toList());
	}
	
	public List<String> getTracks() {
		return tracks;
	}
	
   public List<DayPairs> getPastPairs() {
	   return pastPairs;
   }

   public List<Company> getDevOpsCompanies(){
	   return devOpsCompanies;
   }

   public List<Company> getCompanies(){
	   return allCompanies;
   }
   
	public void syncTrelloBoardState() {
		tracks = new ArrayList<>();
		pastPairs = new ArrayList<>();
		for (TList tList : getLits()) {
			String listName = tList.getName();
			List<Card> cards = getListCards(tList.getId());
			if ("devs".equalsIgnoreCase(listName)){
				syncDevs(cards);
				syncDevsMetadata(cards);
			}
			
			if ("tracks".equalsIgnoreCase(listName)){
				for (Card card : cards) {
					tracks.add(card.getName());
				}
			}
			
			if (listName.toLowerCase().startsWith("pairing")){
				DayPairs pairs = syncPairs(tList, cards);
				pastPairs.add(pairs);
			}
		}
	}

	private DayPairs syncPairs(TList tList, List<Card> cards) {
		DayPairs pairs = new DayPairs();
		try {
			pairs.setDate(getDateFromListName(tList.getName()));
		} catch (ParseException e) {
			throw new RuntimeException("Unsupported date format in list name: " + tList.getName(), e);
		}
		for (Card card : cards) {
			Pair pair = new Pair();
			pair.setBuildPair(isPairWithLabel(card, BUILD_PAIR_LABEL_COLOR));
			pair.setCommunityPair(isPairWithLabel(card, COMMUNITY_PAIR_LABEL_COLOR));
			pair.setLockedPair(isPairWithLabel(card, LOCK_LABEL_COLOR));
			pair.setOpsPair(isPairDevOpsPair(card.getDesc()));
			pair.setDevs( getDevelopersFromCard(card));
			pair.setTrack(card.getName());
			pairs.addPair(card.getName(), pair);
		}
		return pairs;
	}

	private boolean isPairDevOpsPair(String description) {
		return DEVOPS_PAIR_DESCRIPTION.equals(description);
	}

	private void syncDevs(List<Card> cards) {
		cards.stream().filter(card -> "devs".equalsIgnoreCase(card.getName()))
		              .forEach(card -> availableDevelopers.addAll(getDevelopersFromCard(card)));
	}
	
	private void syncDevsMetadata(List<Card> cards) {
		for (Card card : cards) {
			String lowerCaseCardName = card.getName().toLowerCase();
			if (lowerCaseCardName.startsWith(DEVOPS_PAIR_DESCRIPTION)) {
				String[] companyNames = parseDevOpsCompanies(lowerCaseCardName);
				initDevOpsCompanies(companyNames);
			} else if ("new".equals(lowerCaseCardName)) {
				card.getIdMembers().forEach(developerId -> getDeveloperById(developerId).setNew(true));
			} else {
				setDevsCompany(card.getName(), card);
			}
		}
	}

	protected String[] parseDevOpsCompanies(String lowerCaseCardName) {
		String companies = lowerCaseCardName.replaceFirst("devops:", "");
		if ("".equals(companies)){
			return new String[0];
		}
		return companies.split(",");
	}

	private void initDevOpsCompanies(String[] companyNames) {
		for (String companyName : companyNames) {
			devOpsCompanies.add(getCompanyByName(companyName));
		}
	}

	private boolean isPairWithLabel(Card card, String labelColor) {
		return card.getLabels().stream().anyMatch(label -> labelColor.equals(label.getColor()));
	}
	
	private List<Developer> getDevelopersFromCard(Card card) {
		return card.getIdMembers().stream().map(this::getDeveloperById).collect(Collectors.toList());
	}
	
	private void setDevsCompany(String companyName, Card card) {
		Company company = getCompanyByName(companyName);
		card.getIdMembers().stream().map(this::getDeveloperById).forEach(developer -> developer.setCompany(company));
	}

	private Company getCompanyByName(String companyName){
		Company result = allCompanies.stream().filter(company -> company.getName().equals(companyName)).findFirst().orElse(null);
		if(result == null){
			result = new Company(companyName);
			allCompanies.add(result);
		}
		
		return result;
	}
	
	private Developer getDeveloperById(String developerId){
		Developer result = allDevelopers.stream().filter(developer -> developer.equals(new Developer(developerId))).findFirst().orElse(new Developer(developerId));
		if(!allDevelopers.contains(result)){
			allDevelopers.add(result);
		}
		
		return result;
	}

	public List<TList> getLits(){
		return trelloImpl.getBoardLists(pairingBoardId);
	}
	
	public Date getDateFromListName(String name) throws ParseException{
		String date = name.substring(name.indexOf('(') + 1, name.lastIndexOf(')'));
		return new DayPairs().parse(date);
	}
	
	
	public List<Card> getListCards(String listId) {
		TrelloUrl getListCardsURL = TrelloUrl.createUrl(GET_LIST_CARDS);
		return Arrays.asList(httpClient.get(getListCardsURL.asString(), Card[].class, listId, apiKey, apiToken));
	}

	public void addTodayPairsToBoard(DayPairs pairs, int daysIntoFuture) {
		TList newPairingList = createNewPairingList(pairs, daysIntoFuture);
		newPairingList.setInternalTrello(trelloImpl);		
        addPairsToList(pairs, newPairingList);
	}

	private void addPairsToList(DayPairs pairs, TList newPairingList) {
		addTracksToList(pairs, getDevOpsTracks(), newPairingList);
		addTracksToList(pairs, tracks, newPairingList);
	}

	private List<String> getDevOpsTracks() {
		return devOpsCompanies.stream().map(Company::getTrack).collect(Collectors.toList());
	}

	private void addTracksToList(DayPairs pairs, List<String> tracksToAdd, TList newPairingList) {
		for (int i = tracksToAdd.size() - 1; i >= 0; i--) {
			if(pairs.getTracks().contains(tracksToAdd.get(i))){
				String trackName = tracksToAdd.get(i);
				Pair pairByTrack = pairs.getPairByTrack(trackName);
				Card card = new Card();
				card.setName(trackName);
				card.setIdMembers(getIdsFromDevelopers(pairByTrack));
				if(pairByTrack.isOpsPair()) {
					card.setDesc(DEVOPS_PAIR_DESCRIPTION);
				}
				Card pairingCard = newPairingList.createCard(card);
				if(pairByTrack.isBuildPair()){
					pairingCard.addLabels(BUILD_PAIR_LABEL_COLOR);
				}
				if(pairByTrack.isCommunityPair()){
					pairingCard.addLabels(COMMUNITY_PAIR_LABEL_COLOR);
				}
			}
		}
	}

	private List<String> getIdsFromDevelopers(Pair pair) {
		return pair.getDevs().stream().map(Developer::getId).collect(Collectors.toList());
	}
	
	private TList createNewPairingList(DayPairs pairs, int daysIntoFuture) {
		TrelloUrl createListURL = TrelloUrl.createUrl(CREATE_LISTS);
		String name = "pairing(" + pairs.format(getFutureDate(pairs.getDate(), daysIntoFuture)) + ")";
		TList tList = new TList();
		tList.setName(name);
		tList.setIdBoard(pairingBoardId);
		createListURL.params();
		return httpClient.postForObject(createListURL.asString(), tList, TList.class, apiKey, apiToken);
	}
	
	private Date getFutureDate(Date dateToStart, int daysIntoFuture) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateToStart);
		cal.add(Calendar.DATE, daysIntoFuture);
		return cal.getTime();
	}
}
