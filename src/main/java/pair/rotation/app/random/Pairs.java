package pair.rotation.app.random;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.ListUtils;

import pair.rotation.app.persistence.mongodb.DevsRepository;
import pair.rotation.app.persistence.mongodb.TracksRepository;

public class Pairs {

	private DevsRepository devsRepository;

	public Pairs(DevsRepository devsRepository, TracksRepository tracksRepository) {
		this.devsRepository = devsRepository;
    }

	
	public List<List<String>> getRandomPairs(){
        List<Devs> allDevs = devsRepository.findAll();
        if (allDevs.isEmpty()){
        	return Collections.emptyList();
        }else{
    		List<String> devs = allDevs.get(0).getDevs();
            Collections.shuffle(devs);
            return ListUtils.partition(devs, 2);	
        }
	}

}
