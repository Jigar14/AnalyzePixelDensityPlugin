import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class Analyze_PixelDensity implements PlugIn {

	public static final String IMAGE_LOCATION = "C:\\Users\\jigs\\Desktop\\johntest.tif";
	
	public static final String PROCESSED_IMAGE = "Processed Image";
	
	public static final String TOTAL_NUM_ROWS = "Total Number Of Rows : ";
	
	public static final String TOTAL_NUM_CIRCLE = "Total Number of Circle(s) Per Row : ";
	
	public static final String WHITE_STR = "White";
	
	public static final String BLACK_STR = "Black";
	
	public static final int WHITE = 0;
	
	public static final int BLACK = 225;
	
	public static final String EMPTY = " ";
	
	public static final String AUTO = "Automatic";
	
	public static final String USER_PREF = "User Preference";
	
	public static final String PLAIN = "Plain";
	
	public static final String RADIUS = "Radius : ";
	
	public static final String CIRCLE_COLOR = "Color of Circle : ";
	
	public static final String TEXT_SIZE = "Size of the text : ";
	
	public static final int RECOMMENDED_PIXEL_DISTANCE = 300;
	
	private HashMap<Point, CircleInfo> densityInfoMap = new HashMap<>();
		
	private int height = 0, width = 0;
	
	private int totalNumberOfPixels;
	
	private int totalCircles = 0, totalRows = 0, radius = 0;
	
	private String colorOfCircle = "";
	
	private int textSize = 10;
	
	byte[] pixels;
	
	private ImagePlus imagePlus;
	
	private ImageProcessor imageProcessor;
	
	
	@Override
	public void run(String arg0) {
		
		imagePlus = IJ.getImage();
		imageProcessor = new ByteProcessor(imagePlus.getImage());
		pixels = (byte[]) imageProcessor.getPixels();
		
		height = imageProcessor.getHeight();
		width = imageProcessor.getWidth();
		totalNumberOfPixels = imageProcessor.getPixelCount();
	
		setRecommendedParameters();
				
		GenericDialog gd = new GenericDialog(USER_PREF);
		gd.addNumericField(TOTAL_NUM_ROWS, totalRows, 0);
		gd.addNumericField(TOTAL_NUM_CIRCLE, totalCircles, 0);
		gd.addNumericField(RADIUS, 40, 0);
		gd.addNumericField(TEXT_SIZE, 10, 0);
		String[] options = new String[] {BLACK_STR, WHITE_STR, AUTO};
		gd.addRadioButtonGroup(CIRCLE_COLOR, options, 1, 1, AUTO);
		gd.showDialog();
		
		totalRows = (int) gd.getNextNumber();
		totalCircles = (int) gd.getNextNumber();
		radius = (int) gd.getNextNumber();
		colorOfCircle = (String) gd.getNextRadioButton();
		textSize = (int) gd.getNextNumber();
		
		
//		System.out.println(TOTAL_NUM_ROWS + totalRows);
//		System.out.println(TOTAL_NUM_CIRCLE + totalCircles);
//		System.out.println(RADIUS + radius);
//		System.out.println(CIRCLE_COLOR + colorOfCircle);
		
		ArrayList<Point> centerPoints = new ArrayList<Point>();
		
		for(int i = 0; i < totalCircles; i ++) {
			for(int j = 0; j < totalRows; j++) {
				
				if(! (calculateCordinatePointsForCenter(i, j) == null)) {
					Point temp = calculateCordinatePointsForCenter(i, j);
					centerPoints.add(temp);
				}
			}
		}
		
		ImagePlus updated = new ImagePlus(PROCESSED_IMAGE, imageProcessor); 
		
		for(Point c : densityInfoMap.keySet()) {
			
			CircleInfo cInfo = densityInfoMap.get(c);
			double densityValue = cInfo.GetDensityOfCircle();
			
			imageProcessor.setColor(Color.BLACK);
			imageProcessor.setFont(new Font(PLAIN, Font.BOLD, textSize));
			imageProcessor.drawString(String.valueOf(densityValue), c.x - 40, (int) c.y - radius - 20);
		}
		
		updated.show(PROCESSED_IMAGE);
	}
	
	public void setRecommendedParameters(){
		totalRows = height/RECOMMENDED_PIXEL_DISTANCE;
		totalCircles = width/RECOMMENDED_PIXEL_DISTANCE;
	}
	
	private Point calculateCordinatePointsForCenter(int circleNumber, int rowNumber) {

		int xCordinate, yCordinate;
		int xDistance =  width/totalCircles;
		int yDistance = height/totalRows;
		
		xCordinate = circleNumber * xDistance + xDistance;
		yCordinate = rowNumber * yDistance + yDistance;
		
		if((xCordinate + radius< totalNumberOfPixels ) && (yCordinate + radius < totalNumberOfPixels)) {
			imageProcessor.putPixel(xCordinate, yCordinate, 0);
			
			Point center = new Point(xCordinate, yCordinate);
			int pixelValue = getColorForPixel(center);
			
			int new_x = center.x - radius;
			int new_y = center.y - radius;
		
			Point leftTopRectanglePoint = new Point(new_x, new_y);
			Point rightTopRectanglePoint = new Point(new_x + 2 * radius, new_y);
			Point leftBottomRectanglePoint = new Point(new_x, new_y + 2 * radius);
//			Point rightBottomRectanglePoint = new Point(new_x + 2 * radius, new_y + 2 * radius);
			
			CircleInfo cInfo = new CircleInfo(center, radius);
			
			for(int i = leftTopRectanglePoint.x; i < rightTopRectanglePoint.x; i++) {
				for(int j = leftTopRectanglePoint.y; j < leftBottomRectanglePoint.y; j++) {
								
					if(i > width || j > height) continue;
					
					else {  
						int value = imageProcessor.getPixel(i, j);
						cInfo.addPixel(value, center);
					}
				}
			}
			
			densityInfoMap.put(center, cInfo);
			
			for (double counter = 0; counter < 10; counter = counter + 0.001) {
				
				double x = Math.sin(counter) * radius + center.x;
				double y = Math.cos(counter) * radius + center.y;
				imageProcessor.putPixel((int) x, (int) y, pixelValue);
	 		}
			return center;
		}
		
		else return null;
		
	}
	
	public int getColorForPixel(Point p) {
		
		int returnValue = 0;
		
		switch(colorOfCircle) {
		
		case AUTO: 
			int pixelValue = imageProcessor.getPixel(p.x, p.y);
			if(pixelValue < 100) 
				returnValue = 225;
			else
				returnValue = 0; 
		
		case WHITE_STR:
			returnValue = 225;
		
		case BLACK_STR:
			returnValue = 0;
			
		}
		return returnValue;
	}
	
	public static void main(String[] args) {
		new ImageJ();
		ImagePlus imagePlus = new ImagePlus(IMAGE_LOCATION);
		imagePlus.show();
		Class<?> clazz = Analyze_PixelDensity.class;
		IJ.runPlugIn(clazz.getName(), null);
	}
}