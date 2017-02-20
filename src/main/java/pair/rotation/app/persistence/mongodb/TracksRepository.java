package pair.rotation.app.persistence.mongodb;


import org.springframework.data.mongodb.repository.MongoRepository;

import pair.rotation.app.random.Tracks;

public interface TracksRepository extends MongoRepository<Tracks, String> {

}
