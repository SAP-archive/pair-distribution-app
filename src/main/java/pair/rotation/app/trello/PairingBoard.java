package pair.rotation.app.trello;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.TList;
import com.julienvey.trello.impl.TrelloImpl;
import com.julienvey.trello.impl.TrelloUrl;
import com.julienvey.trello.impl.http.RestTemplateHttpClient;


public class PairingBoard {

	private static final String CREATE_LISTS = "/lists?";
    private static final String GET_LIST_CARDS = "/lists/{listId}/cards?";
	private RestTemplateHttpClient httpClient;
	private TrelloImpl trelloImpl;
	private List<Developer> devs;
	private List<String> tracks;
	private List<DayPairs> pastPairs;
	private String accessKey;
	private String applicationKey;
	private String pairingBoardId;
    
	public PairingBoard(String accessKey, String applicationKey, String pairingBoardId) {
    	this.accessKey = accessKey;
		this.applicationKey = applicationKey;
		this.pairingBoardId = pairingBoardId;
		httpClient = new RestTemplateHttpClient();
    	trelloImpl = new TrelloImpl(applicationKey, accessKey, httpClient);
	}
    
	public List<Developer> getDevs() {
		return devs;
	}
	
	public List<String> getTracks() {
		return tracks;
	}
	
   public List<DayPairs> getPastPairs() {
	   return pastPairs;
   }

	
	public void syncTrelloBoardState() {
		devs = new ArrayList<Developer>();
		tracks = new ArrayList<String>();
		pastPairs = new ArrayList<DayPairs>();
		for (TList tList : getLits()) {
			String listName = tList.getName();
			System.out.println("List name is: " + listName);
			List<Card> cards = getListCards(tList.getId());
			System.out.println("Cards count is: " + cards.size());
			if ("devs".equals(listName.toLowerCase())){
				syncDevs(cards);
			}
			
			if ("tracks".equals(listName.toLowerCase())){
				for (Card card : cards) {
					tracks.add(card.getName());
				}
			}
			if (listName.toLowerCase().startsWith("pairing")){
				DayPairs pairs = new DayPairs();
				try {
					pairs.setDate(getDateFromCradName(tList.getName()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (Card card : cards) {
					Pair pair = new Pair();
					pair.setDevs( getDevelopersFromCard(card));
					pairs.addPair(card.getName(), pair);
					System.out.println(card.getName());
					System.out.println(card.getDesc());
				}
				pastPairs.add(pairs);
			}
		}
	}

	private void syncDevs(List<Card> cards) {
		for (Card card : cards) {
			String cardName = card.getName().toLowerCase();
			switch (cardName) {
			case "devs":
				devs.addAll(getDevelopersFromCard(card));
				break;
			case "dod":
				makeAllDevsDoD(devs, card);
				break;
			default:
				setDevsCompany(cardName, devs, card);
			}
		}
	}

	private List<Developer> getDevelopersFromCard(Card card) {
		List<Developer> developers = new ArrayList<>();
		for (String developerId : card.getIdMembers()) {
			developers.add(new Developer(developerId));
		}
		return developers;
	}
	
	private void setDevsCompany(String company, List<Developer> developers, Card card) {
		for (String developerId : card.getIdMembers()) {
			Developer developer = getDeveloperById(devs, new Developer(developerId));
			if(developer != null){
				developer.setCompany(company);	
			}
		}
	}

	private void makeAllDevsDoD(List<Developer> developers, Card card) {
		for (String developerId : card.getIdMembers()) {
			Developer developer = getDeveloperById(devs, new Developer(developerId));
			if(developer != null){
				developer.setDoD(true);
			}
		}
		
	}
	
	private Developer getDeveloperById(List<Developer> devs, Developer developerToCompare){
		for (Developer developer : devs) {
			if(developer.equals(developerToCompare)){
				return developer;
			}
		}
		return null;
	}

	public List<TList> getLits(){
		return trelloImpl.getBoardLists(pairingBoardId);
	}
	
	public Date getDateFromCradName(String name) throws ParseException{
		String date = name.substring(name.indexOf("(") + 1, name.lastIndexOf(")"));
		return DayPairsHelper.DATE_FORMATTER.parse(date);
	}
	
	
	public List<Card> getListCards(String listId) {
		TrelloUrl getListCardsURL = TrelloUrl.createUrl(GET_LIST_CARDS);
		List<Card> listCards = Arrays.asList(httpClient.get(getListCardsURL.asString(), Card[].class, listId, applicationKey, accessKey));
		return listCards;
	}

	public void addTodayPairsToBoard(DayPairs pairs, int daysIntoFuture) {
		TList newPairingList = createNewPairingList(pairs, daysIntoFuture);
		newPairingList.setInternalTrello(trelloImpl);		
        addPairsToList(pairs, newPairingList);
	}

	private void addPairsToList(DayPairs pairs, TList newPairingList) {
		for (int i = tracks.size() - 1; i >= 0; i--) {
			if(pairs.getTracks().contains(tracks.get(i))){
				Card card = new Card();
				card.setName(tracks.get(i));
				card.setIdMembers(getIdsFromDevelopers(pairs, i));
				newPairingList.createCard(card);	
			}
		}
	}

	private List<String> getIdsFromDevelopers(DayPairs pairs, int i) {
		List<String> developerIds = new ArrayList<>();
		List<Developer> developers = pairs.getPairByTrack(tracks.get(i)).getDevs();
		for (Developer developer : developers) {
			developerIds.add(developer.getId());
		}
		return developerIds;
	}

	private TList createNewPairingList(DayPairs pairs, int daysIntoFuture) {
		TrelloUrl createListURL = TrelloUrl.createUrl(CREATE_LISTS);
		String name = "pairing(" + DayPairsHelper.DATE_FORMATTER.format(getFutureDate(pairs.getDate(), daysIntoFuture)) + ")";
		TList tList = new TList();
		tList.setName(name);
		tList.setIdBoard(pairingBoardId);
		createListURL.params();
		TList newPairingList = httpClient.postForObject(createListURL.asString(), tList, TList.class, applicationKey, accessKey);
		return newPairingList;
	}
	
	private Date getFutureDate(Date dateToStart, int daysIntoFuture) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateToStart);
		cal.add(Calendar.DATE, daysIntoFuture);
		return cal.getTime();
	}
}
