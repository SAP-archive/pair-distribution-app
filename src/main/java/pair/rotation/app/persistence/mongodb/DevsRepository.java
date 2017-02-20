package pair.rotation.app.persistence.mongodb;


import org.springframework.data.mongodb.repository.MongoRepository;

import pair.rotation.app.random.Devs;

public interface DevsRepository extends MongoRepository<Devs, String> {

}
