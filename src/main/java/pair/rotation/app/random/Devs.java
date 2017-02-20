package pair.rotation.app.random;

import java.util.List;

import org.springframework.data.annotation.Id;

public class Devs {

	@Id
	private String id;
    private List<String> devs;
    
    public Devs() {
	}

    public Devs(List<String> devs) {
		this.devs = devs;
	}
    
	public List<String> getDevs() {
		return devs;
	}
	public void setDevs(List<String> devs) {
		this.devs = devs;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
    
	
	
}
