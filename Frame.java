
public class Frame {
	
	private int frame; //frame number
	private int dupe; //0: good; 1: dupe (set for deletion)
	private float difference; //difference metric
	
	// CONSTRUCTOR
	Frame(){
	}
	
	// SETTERS
	public void setFrame(int in){
		frame = in;
	}
	
	public void setStatus(int in){
		dupe = in;
	}
	
	public void setDiff(float in){
		difference = in;
	}
	
	// GETTERS
	public int number(){
		return frame;
	}
	
	public int status(){
		return dupe;
	}
	
	public float diff(){
		return difference;
	}

	public String toString(){
		String me = frame + "  " + dupe + "  " + String.format("%.6f", difference);
		return me;
	}
}
