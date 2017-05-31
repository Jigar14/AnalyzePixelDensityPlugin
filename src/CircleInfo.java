import java.awt.Point;

public class CircleInfo {

	/** The center of the circle*/
	private Point c;
	
	private int radius;
	
	private int totalNumberOfPixels = 0;
	
	private int totalValuesOfPixels = 0;
	
		
	public CircleInfo(Point c, int radius){
		
		this.c = c;
		this.radius = radius;
	}
	
	public double GetDensityOfCircle(){
		
		if(totalNumberOfPixels == 0) {
			return 0;
		}
		
		return this.totalValuesOfPixels/this.totalNumberOfPixels;
	}
	
	/**
	 * 
	 * @param ip : ImageProcessor  
	 * @param p  : Point<br>
	 * 
	 * This method will only add the pixel to the circleInfo 
	 * data class if it exists in the circle and ignore o.w.
	 * 
	 */
	public void addPixel(int pixelValue, Point p) {
		if(p.distance(c) < this.radius) {
			totalValuesOfPixels += pixelValue;
			totalNumberOfPixels += 1;
		}
	}
	
	public int getTotalNumberOfPixles()	{
		return this.totalNumberOfPixels;
	}
	
	public Point getC() {
		return c;
	}

	public void setC(Point c) {
		this.c = c;
	}

	@Override
	public String toString(){
		return this.getClass().getName() + " Circle with Center	(" + c.x + " , " + c.y + " )" + " Total Number of Pixles : " + totalNumberOfPixels + 
				" Total Value of Pixels : " + totalValuesOfPixels + " Density of pixels in circle : " + GetDensityOfCircle(); 
	}
}
