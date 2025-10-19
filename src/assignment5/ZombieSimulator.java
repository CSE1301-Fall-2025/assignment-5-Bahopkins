package assignment5;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Scanner;

import javax.swing.JFileChooser;

import edu.princeton.cs.introcs.StdDraw;

/**
 * A Zombie Simulator!
 */
public class ZombieSimulator {
	public static final int X = 0;
	public static final int Y = 1;
	private static final String ZOMBIE_TOKEN_VALUE = "Zombie";

	private static final Color ZOMBIE_COLOR = new Color(146, 0, 0);
	private static final Color NONZOMBIE_COLOR = new Color(0, 0, 0);
	private static final Color TEXT_COLOR = new Color(73, 0, 146);
	public static final double ENTITY_RADIUS = 0.008;

	public static final double RANDOM_DELTA_HALF_RANGE = 0.006;

	/**
	 * Read entities from a file.
	 */
	public static void readEntities(Scanner in, boolean[] areZombies, double[][] positions) {

		for (int i = 0; i < areZombies.length; i++) {
        // Read whether the entity is a zombie
        	String type = in.next(); // "Zombie" or "Nonzombie"
        	if (type.equals(ZOMBIE_TOKEN_VALUE)) {
            	areZombies[i] = true;
       		} 
			else {
            	areZombies[i] = false;
        	}

        	// Read x and y positions
			double xPosition = in.nextDouble();
			double yPosition = in.nextDouble();

			// Store positions
			positions[i][X] = xPosition;
			positions[i][Y] = yPosition;
		}
	}	

	/**
	 * Draw all the entities. Zombies are drawn as ZOMBIE_COLOR filled circles of
	 * radius ENTITY_RADIUS and non-zombies with filled NONZOMBIE_COLOR filled
	 * circles of radius ENTITY_RADIUS). Further, add feedback for nonzombie count
	 * (when ready to do so), and any additional desired drawing features.
	 * 
	 * @param areZombies the zombie state of each entity
	 * @param positions  the (x,y) position of each entity
	 */
	public static void drawEntities(boolean[] areZombies, double[][] positions) {
		// DONE: Clear the frame
		StdDraw.clear();

		for (int i = 0; i < areZombies.length; i++) {
			if (areZombies[i]) {
				StdDraw.setPenColor(ZOMBIE_COLOR);
				double xPosition = positions[i][X];  // column 0
        		double yPosition = positions[i][Y];  // column 1
				StdDraw.filledCircle(xPosition, yPosition, ENTITY_RADIUS);
			}
			else {
				StdDraw.setPenColor(NONZOMBIE_COLOR);
				double xPosition = positions[i][X];  // column 0
        		double yPosition = positions[i][Y];  // column 1
				StdDraw.filledCircle(xPosition, yPosition, ENTITY_RADIUS);
			}
		}
		StdDraw.setPenColor(TEXT_COLOR);
		int nonzombies = nonzombieCount(areZombies);
		int total = areZombies.length;
		StdDraw.text(0.1, 0.98, nonzombies + "/" + total);

		// DONE: Show everything that was drawn (show the updated frame). This should be
		// the only "show()" command!
		StdDraw.show();
	}

	/**
	 * Check if the entity at the given index is touching a zombie. (HINT: You know
	 * the location of the center of each entity and that they all have a radius of
	 * ENTITY_RADIUS. If the circles representing two entities overlap they are
	 * considered to be touching. Consider using the distance formula.)
	 *
	 * @param index      the index of the entity to check
	 * @param areZombies the zombie state of each entity
	 * @param positions  the (x,y) position of each entity
	 * @return true if the entity at index is touching a zombie, false otherwise
	 */
	public static boolean touchingZombie(int index, boolean[] areZombies, double[][] positions) {
		double x1 = positions[index][X];
    	double y1 = positions[index][Y];
		for (int i = 0; i < areZombies.length; i++) {
			if (areZombies[i]) {
				double x2 = positions[i][X];
                double y2 = positions[i][Y];
				double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
				if (distance <= 2 * ENTITY_RADIUS) {
                    return true;
				}
			}
		}
		return false;
	}

	/**
	 * Update the areZombies states and positions of all entities (assume Brownian
	 * motion).
	 *
	 * The rules for an update are:
	 * 
	 * Each entity should move by a random value between -RANDOM_DELTA_HALF_RANGE 
	 * and +RANDOM_DELTA_HALF_RANGE in both the x and the y coordinates.
	 * 
	 * Entities should not be able to leave the screen. x and y coordinates should
	 * be kept between [0-1.0]
	 *
	 * If a non-zombie is touching a zombie it should change to a zombie. (HINT: you
	 * need to check all entities. On each one that is NOT a zombie, you can re-use
	 * code you've already written to see if it's "touching" a Zombie and, if so,
	 * change it to a zombie.)
	 *
	 * @param areZombies the zombie state of each entity
	 * @param positions  the (x,y) position of each entity
	 */
	public static void updateEntities(boolean[] areZombies, double[][] positions) {
		int n = areZombies.length;

    	// 1) Move everyone with Brownian motion
    	for (int i = 0; i < n; i++) {
        	// random in [-RANDOM_DELTA_HALF_RANGE, +RANDOM_DELTA_HALF_RANGE]
        	double dx = (Math.random() * 2.0 - 1.0) * RANDOM_DELTA_HALF_RANGE;
        	double dy = (Math.random() * 2.0 - 1.0) * RANDOM_DELTA_HALF_RANGE;
       		positions[i][X] += dx;
       		positions[i][Y] += dy;

        	// 2) Clamp to [0,1]
			if (positions[i][X] < 0.0) {
				positions[i][X] = 0.0;
			} 
			else if (positions[i][X] > 1.0) {
				positions[i][X] = 1.0;
			}
			if (positions[i][Y] < 0.0) {
				positions[i][Y] = 0.0;
			}
			else if (positions[i][Y] > 1.0) {
				positions[i][Y] = 1.0;
			}
    	}

    	// 3) Determine infections
    	boolean[] willTurn = new boolean[n];
    	for (int i = 0; i < n; i++) {
        	if (!areZombies[i]) { // only non-zombies can turn
            	if (touchingZombie(i, areZombies, positions)) {
                	willTurn[i] = true;
            	}
        	}
    	}

    	// Apply flips after the checks
    	for (int i = 0; i < n; i++) {
       		if (willTurn[i]) {
            	areZombies[i] = true;
        	}
    	}
	}	

	/**
	 * Return the number of nonzombies remaining
	 */

	public static int nonzombieCount(boolean[] areZombies) {		
		int count = 0;
    	for (int i = 0; i < areZombies.length; i++) {
        	if (!areZombies[i]) {   // if it's NOT a zombie
            	count++;
        	}
    	}
    	return count;
	}

	/**
	 * Run the zombie simulation.
	 */
	private static void runSimulation(Scanner in) {
		StdDraw.enableDoubleBuffering(); // reduce unpleasant drawing artifacts, speed things up
		int N = in.nextInt();
		boolean[] areZombies = new boolean[N];
		double[][] positions = new double[N][2];
		readEntities(in, areZombies, positions);
		drawEntities(areZombies, positions);
		StdDraw.pause(500);

		// Continue if nonzombies remain
		// Update zombie state and positions
		// Redraw
		while (nonzombieCount(areZombies) > 0) {   // keep going while humans remain
			updateEntities(areZombies, positions); // move + infect
			drawEntities(areZombies, positions);   // redraw
			StdDraw.pause(20);  
		}
	}


	public static void main(String[] args) throws FileNotFoundException {
		JFileChooser chooser = new JFileChooser("zombieSims");
		chooser.showOpenDialog(null);
		File f = new File(chooser.getSelectedFile().getPath());
		Scanner in = new Scanner(f); //making Scanner with a File
		runSimulation(in);
	}

}
