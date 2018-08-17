package pair.distribution.app.web;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pair.distribution.app.persistence.mongodb.TrelloPairsRepository;
import pair.distribution.app.trello.entities.DayPairs;


@RestController
@RequestMapping(value = "/pairs/content/trello")
public class TrelloPairsDbController {
    private TrelloPairsRepository repository;

    @Autowired
    public TrelloPairsDbController(TrelloPairsRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<DayPairs> pairs() {
		return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void clear() {
       repository.deleteAll();
    }
}
