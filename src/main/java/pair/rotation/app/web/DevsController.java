package pair.rotation.app.web;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pair.rotation.app.persistence.mongodb.DevsRepository;
import pair.rotation.app.random.Devs;

@RestController
@RequestMapping(value = "/devs")
public class DevsController {
    private static final Logger logger = LoggerFactory.getLogger(DevsController.class);
    private DevsRepository repository;

    @Autowired
    public DevsController(DevsRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Devs devs() {
        List<Devs> allDevs = repository.findAll();
        return allDevs.isEmpty() ? null : allDevs.get(0);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Devs add(@RequestBody @Valid Devs devs) {
        logger.info("Adding devs " + devs.getId());
        repository.deleteAll();
        return repository.save(devs);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Devs update(@RequestBody @Valid Devs devs) {
        logger.info("Updating devs " + devs.getId());
        repository.deleteAll();
        return repository.save(devs);
    }
    
    @RequestMapping(method = RequestMethod.DELETE)
    public void clear() {
        logger.info("Deleting all tracks");
        repository.deleteAll();;
    }
}