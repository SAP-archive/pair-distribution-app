package pair.rotation.app.persistence.mongodb;


import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import pair.rotation.app.trello.DayPairs;

public interface TrelloPairsRepository extends MongoRepository<DayPairs, String> {

	List<DayPairs> findByDate(Date date);
	
	void deleteByDate(Date date);
}
