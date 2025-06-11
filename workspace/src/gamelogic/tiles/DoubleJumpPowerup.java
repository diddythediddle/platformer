package gamelogic.tiles;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

// Import RectHitbox specifically
import gameengine.hitbox.RectHitbox;
import gamelogic.level.Level;

// DoubleJumpPowerup represents a collectible tile that grants the player
// the ability to double jump.
public class DoubleJumpPowerup extends Tile {

    private BufferedImage image;
    private int col;
    private int row;
    private int size;

    // Constructor for the DoubleJumpPowerup tile.
    // Parameters:
    //   col: The column (x-coordinate in tile units) of the tile.
    //   row: The row (y-coordinate in tile units) of the tile.
    //   size: The size (width and height) of the tile in pixels.
    //   image: The BufferedImage to draw for this tile.
    //   level: The Level instance this tile belongs to.
    public DoubleJumpPowerup(int col, int row, int size, BufferedImage image, Level level) {
        // Call the superclass constructor.
        // `isSolid` is false as this is a collectible, not a barrier.
        super(col, row, size, image, false, level);
        this.col = col;
        this.row = row;
        this.size = size;
        this.image = image;

        // Initialize the hitbox for collision detection with the player.
        // We are now explicitly passing the pixel coordinates (col * size, row * size)
        // to the RectHitbox constructor. This ensures the hitbox's position is correct
        // even if the 'this' object's internal X and Y are not fully set yet in the
        // constructor chain.
        // The offsets (0, 0) and dimensions (size, size) ensure the hitbox covers
        // the entire tile area.
        this.hitbox = new RectHitbox(col * size, row * size, 0, 0, size, size);
    }

    // Overrides the draw method from the Tile superclass.
    // Draws the power-up image at its correct position on the screen.
    // Parameter:
    //   g: The Graphics context used for drawing.
    @Override
    public void draw(Graphics g) {
        // Ensure the image exists before attempting to draw it.
        if (image != null) {
            // Draw the image at the tile's calculated pixel position.
            // X-coordinate: column * tile size
            // Y-coordinate: row * tile size
            // Width and Height: tile size
            g.drawImage(image, col * size, row * size, size, size, null);
        }
    }
}