package gamelogic.player;

import java.awt.Color;
import java.awt.Graphics;

import gameengine.PhysicsObject;
import gameengine.graphics.MyGraphics;
import gameengine.hitbox.RectHitbox;
import gamelogic.Main;
import gamelogic.level.Level;
import gamelogic.tiledMap.Map; 
import gamelogic.tiles.Gas; 
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water; 

public class Player extends PhysicsObject {
    public float walkSpeed = 400;
    public float jumpPower = 1350;

    // Flag to indicate if player is currently ascending from a jump.
    private boolean isJumping = false; 

    // --- New fields for Water and Double Jump ---
    private boolean inWater; // True if the player is currently in water
    private float waterSpeedMultiplier = 0.6f; // Player moves at 60% of normal speed in water
    private float waterMaxFallSpeed = 150f; // Maximum downward velocity in water (simulates floating)

    private boolean isObscuredByGas; // True if the player is currently in gas, for screen effect

    private boolean hasDoubleJump; // True if the player has collected the double jump power-up
    private int jumpsRemaining; // Tracks available jumps (1 for normal, 2 for double jump)
    private boolean lastJumpInputState; // To detect the rising edge of a jump key press
    // --- End New fields ---

    public Player(float x, float y, Level level) {
        // Call the superclass constructor to initialize PhysicsObject properties
        super(x, y, level.getLevelData().getTileSize(), level.getLevelData().getTileSize(), level);
        
        // Calculate offset for the hitbox, which is 10% of the player's size
        int offset = (int) (level.getLevelData().getTileSize() * 0.1); 
        // Initialize the hitbox using the RectHitbox constructor that takes a GameObject
        this.hitbox = new RectHitbox(this, offset, offset, width - offset, height - offset);

        // --- Initialize new fields in the constructor ---
        this.inWater = false;
        this.isObscuredByGas = false;
        this.hasDoubleJump = false;
        this.jumpsRemaining = 1; // Player starts with 1 jump
        this.lastJumpInputState = false; // Initialize jump key state
        // --- End Initialization ---
    }

    @Override
    public void update(float tslf) {
        // Call the superclass update method. This will apply gravity and update
        // the collisionMatrix based on potential next position, for SOLID tiles.
        super.update(tslf); 

        // --- Environmental Effects (Water and Gas) ---
        // Reset environmental flags at the beginning of the frame
        this.inWater = false;
        this.isObscuredByGas = false;

        // Get the map from the level
        Map currentMap = getLevel().getMap(); // PhysicsObject has a getLevel() method

        // Determine the range of tiles to check around the player
        int tileSize = currentMap.getTileSize();
        int playerGridX = (int) (getX() / tileSize);
        int playerGridY = (int) (getY() / tileSize);

        // Check a small surrounding area (e.g., 2 tiles in each direction)
        // to avoid iterating through the entire map unnecessarily.
        int checkRadius = 2; 
        int startX = Math.max(0, playerGridX - checkRadius);
        int endX = Math.min(currentMap.getWidth() - 1, playerGridX + checkRadius);
        int startY = Math.max(0, playerGridY - checkRadius);
        int endY = Math.min(currentMap.getHeight() - 1, playerGridY + checkRadius);

        // Iterate through nearby tiles to check for intersection with Water and Gas
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                Tile tile = currentMap.getTiles()[x][y];
                if (tile != null && tile.getHitbox() != null && this.hitbox.isIntersecting(tile.getHitbox())) {
                    if (tile instanceof Water) {
                        this.inWater = true;
                    }
                    if (tile instanceof Gas) {
                        this.isObscuredByGas = true;
                    }
                }
            }
        }
        

        // Determine if the player is on solid ground
        // This is crucial for resetting jump count and `isJumping` flag
        boolean onGround = (collisionMatrix[BOT] != null);

        if (onGround) {
            // If on ground, reset the number of jumps available
            jumpsRemaining = hasDoubleJump ? 2 : 1; // 2 if double jump acquired, else 1
            isJumping = false; // Player is no longer actively jumping (has landed)
        } else {
            // If not on ground, player is airborne.
            // `isJumping` remains true only if the player is still moving upwards from a jump.
            if (movementVector.y >= 0) { // If falling or static vertically
                isJumping = false;
            } 
            // If movementVector.y < 0, `isJumping` implicitly stays true from the jump application below
        }

        // --- Apply Water-specific Physics Adjustments ---
        float currentWalkSpeed = walkSpeed;
        if (inWater) {
            currentWalkSpeed *= waterSpeedMultiplier; // Reduce horizontal speed in water
            // Apply water's "floating" effect by capping downward velocity
            if (movementVector.y > waterMaxFallSpeed) {
                movementVector.y = waterMaxFallSpeed; // Limit max fall speed in water
            }
        }
        // --- End Water-specific Physics Adjustments ---


        // Handle horizontal movement based on input keys
        movementVector.x = 0; // Reset horizontal movement each frame
        if (PlayerInput.isLeftKeyDown()) {
            movementVector.x = -currentWalkSpeed;
        }
        if (PlayerInput.isRightKeyDown()) {
            movementVector.x = +currentWalkSpeed;
        }

        // --- Handle Jumping (including Double Jump) ---
        boolean currentJumpKeyDown = PlayerInput.isJumpKeyDown(); // Get current state of jump key

        // Check for a new jump key press (rising edge detection) and available jumps
        if (currentJumpKeyDown && !lastJumpInputState && jumpsRemaining > 0) {
            movementVector.y = -jumpPower; // Apply upward velocity for jump
            jumpsRemaining--; // Consume one jump
            isJumping = true; // Player is now actively jumping
        }
        lastJumpInputState = currentJumpKeyDown; // Update last jump key state for next frame's edge detection
        // --- End Jumping ---
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        MyGraphics.fillRectWithOutline(g, (int) getX(), (int) getY(), width, height);

        // Debugging display for collision matrix (from original code)
        if (Main.DEBUGGING) {
            for (int i = 0; i < closestMatrix.length; i++) {
                Tile t = closestMatrix[i];
                if (t != null) {
                    g.setColor(Color.RED);
                    g.drawRect((int) t.getX(), (int) t.getY(), t.getSize(), t.getSize());
                }
            }
        }

        hitbox.draw(g); // Draw the player's hitbox
    }

    /**
     * This method is no longer used as Player determines its own state.
     * It's kept for API compatibility but its content is empty.
     */
    public void setInWater(boolean inWater) {
        // Logic moved to update()
    }

    /**
     * Grants the player the ability to perform a double jump.
     * Called by Level.java when the DoubleJumpPowerup is collected.
     */
    public void grantDoubleJump() {
        this.hasDoubleJump = true;
        // If the player is currently airborne when collecting, immediately grant the second jump.
        // If on ground, jumpsRemaining will be reset to 2 on next ground contact.
        if (jumpsRemaining < 2) { 
             jumpsRemaining = 2; // Ensure it doesn't go above 2 if already 2
        }
    }

    /**
     * Returns true if the player is currently ascending from a jump.
     * @return true if player is jumping, false otherwise.
     */
    public boolean isJumping() {
        return isJumping;
    }

    /**
     * Returns true if the player's vision should be obscured by gas.
     * @return true if player is in gas, false otherwise.
     */
    public boolean isObscuredByGas() {
        return isObscuredByGas;
    }
}
