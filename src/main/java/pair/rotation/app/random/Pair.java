package pair.rotation.app.random;

import org.springframework.data.annotation.Id;

public class Pair {

	@Id
	private String id;

    private String date;
    private String track;
    private String firstPairName;
    private String secondPairName;

    public Pair() {
    }

    public Pair(String date, String track, String firstPairName, String secondPairName) {
        this.setDate(date);
		this.setTrack(track);
		this.setFirstPairName(firstPairName);
		this.setSecondPairName(secondPairName);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}

	public String getFirstPairName() {
		return firstPairName;
	}

	public void setFirstPairName(String firstPairName) {
		this.firstPairName = firstPairName;
	}

	public String getSecondPairName() {
		return secondPairName;
	}

	public void setSecondPairName(String secondPairName) {
		this.secondPairName = secondPairName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
