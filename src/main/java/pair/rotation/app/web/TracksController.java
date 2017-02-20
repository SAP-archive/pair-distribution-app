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

import pair.rotation.app.persistence.mongodb.TracksRepository;
import pair.rotation.app.random.Tracks;

@RestController
@RequestMapping(value = "/tracks")
public class TracksController {
    private static final Logger logger = LoggerFactory.getLogger(TracksController.class);
    private TracksRepository repository;

    @Autowired
    public TracksController(TracksRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Tracks tracks() {
        List<Tracks> allTracks = repository.findAll();
        return allTracks.isEmpty() ? null : allTracks.get(0);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Tracks add(@RequestBody @Valid Tracks tracks) {
        logger.info("Adding tracks " + tracks.getId());
        repository.deleteAll();
        return repository.save(tracks);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Tracks update(@RequestBody @Valid Tracks tracks) {
        logger.info("Updating tracks " + tracks.getId());
        repository.deleteAll();
        return repository.save(tracks);
    }
    
    @RequestMapping(method = RequestMethod.DELETE)
    public void clear() {
        logger.info("Deleting all tracks");
        repository.deleteAll();;
    }
}