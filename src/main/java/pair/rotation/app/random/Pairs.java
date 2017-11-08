package pair.rotation.app.random;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.ListUtils;

import pair.rotation.app.persistence.mongodb.TracksRepository;

public class Pairs {

	public List<List<String>> getRandomPairs(List<Devs> allDevs){
        if (allDevs.isEmpty()){
        	return Collections.emptyList();
        }else{
    		List<String> devs = allDevs.get(0).getDevs();
            Collections.shuffle(devs);
            return ListUtils.partition(devs, 2);	
        }
	}

}
