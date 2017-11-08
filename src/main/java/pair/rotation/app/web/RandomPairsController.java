package pair.rotation.app.web;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pair.rotation.app.persistence.mongodb.DevsRepository;
import pair.rotation.app.random.Devs;
import pair.rotation.app.random.Pairs;

@RestController
@RequestMapping(value = "/pairs/random")
public class RandomPairsController {
    private DevsRepository repository;

    @Autowired
    public RandomPairsController(DevsRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<List<String>> pairs() {
    	return new Pairs().getRandomPairs(repository.findAll());
    }
}