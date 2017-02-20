package pair.rotation.app.random;

import java.util.List;

import org.springframework.data.annotation.Id;

public class Tracks {

	@Id
	private String id;
    private List<String> tracks;
    
    public Tracks() {
	}

    public Tracks(List<String> tracks) {
		this.tracks = tracks;
	}
    
	public List<String> getTracks() {
		return tracks;
	}
	public void setTracks(List<String> devs) {
		this.tracks = devs;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
    
	
	
}
