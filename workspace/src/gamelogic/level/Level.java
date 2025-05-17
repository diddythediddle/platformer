package gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import gameengine.PhysicsObject;
import gameengine.graphics.Camera;
import gameengine.loaders.Mapdata;
import gameengine.loaders.Tileset;
import gamelogic.GameResources;
import gamelogic.Main;
import gamelogic.enemies.Enemy;
import gamelogic.player.Player;
import gamelogic.tiledMap.Map;
import gamelogic.tiles.Flag;
import gamelogic.tiles.Flower;
import gamelogic.tiles.Gas;
import gamelogic.tiles.SolidTile;
import gamelogic.tiles.Spikes;
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water;

public class Level {

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;

	private boolean active;
	private boolean playerDead;
	private boolean playerWin;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
	}

	public LevelData getLevelData(){
		return leveldata;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];

		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);

				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition*tileSize, yPosition*tileSize, this)); // TODO: objects vs tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
				else if (values[x][y] == 16)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
				else if (values[x][y] == 17)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
				else if (values[x][y] == 18)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
				else if (values[x][y] == 19)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);
				else if (values[x][y] == 20)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);
				else if (values[x][y] == 21)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);
			}

		}
		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}
		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(),
				this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		if (active) {
			// Update the player
			player.update(tslf);

			// Player death
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			for (int i = 0; i < flowers.size(); i++) {
				if (flowers.get(i).getHitbox().isIntersecting(player.getHitbox())) {
					if(flowers.get(i).getType() == 1)
						water(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 3);
//					else
//						addGas(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 20, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			}

			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();
				}
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
		}
	}
	
	
	//#############################################################################################################
	//Your code goes here! 
	//Please make sure you read the rubric/directions carefully and implement the solution recursively!
    
	
	// Method to simulate the flow and spreading of water on a map
// Precondition: col and row must be valid indices within the bounds of the map; fullness must be 0 to 3.
// Postcondition: A Water tile is placed on the map at (col, row), and water may recursively spread to neighboring tiles based on fullness and terrain.
private void water(int col, int row, Map map, int fullness) {
    String name = "";
    if (fullness == 3) {
        name = "Full_water";
    } else if (fullness == 2) {
        name = "Half_water";
    } else if (fullness == 1) {
        name = "Quarter_water";
    } else if (fullness == 0) {
        name = "Falling_water";
    }

    Water w = new Water(col, row, tileSize, tileset.getImage(name), this, fullness);
    map.addTile(col, row, w);

    if (map.getTiles()[col][row] instanceof Water && fullness == 0 && row + 1 < map.getTiles()[0].length
            && !(map.getTiles()[col][row + 1] instanceof Water) && map.getTiles()[col][row + 1].isSolid()) {
        water(col, row, map, 3);
    }

    if (row + 1 < map.getTiles()[0].length && !(map.getTiles()[col][row + 1] instanceof Water)
            && !map.getTiles()[col][row + 1].isSolid()) {
        water(col, row + 1, map, 0);
    } else {

        if (col + 1 < map.getTiles().length && !(map.getTiles()[col + 1][row] instanceof Water)
                && !map.getTiles()[col + 1][row].isSolid()) {
            if (fullness == 3) {
                water(col + 1, row, map, 2);
            }
            if (fullness == 2) {
                water(col + 1, row, map, 1);
            }
            if (fullness == 1) {
                water(col + 1, row, map, 1);
            }
        }

        if (col - 1 >= 0 && !(map.getTiles()[col - 1][row] instanceof Water)
                && !map.getTiles()[col - 1][row].isSolid()) {
            if (fullness == 3) {
                water(col - 1, row, map, 2);
            }
            if (fullness == 2) {
                water(col - 1, row, map, 1);
            }
            if (fullness == 1) {
                water(col - 1, row, map, 1);
            }
        }
    }

    if (row + 1 < map.getTiles()[0].length && !(map.getTiles()[col][row + 1] instanceof Water)
            && !map.getTiles()[col][row + 1].isSolid()) {
        water(col, row + 1, map, 0);
    }
}

// Draws the entire game scene (tiles, player, enemies, camera view)
// Precondition: Graphics object g must not be null, and camera, player, map, and enemies must be initialized.
// Postcondition: The current frame of the game scene is rendered on the screen.
public void draw(Graphics g) {
    g.translate((int) -camera.getX(), (int) -camera.getY());

    for (int x = 0; x < map.getWidth(); x++) {
        for (int y = 0; y < map.getHeight(); y++) {
            Tile tile = map.getTiles()[x][y];
            if (tile == null)
                continue;
            if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize()))
                tile.draw(g);
        }
    }

    for (int i = 0; i < enemies.length; i++) {
        enemies[i].draw(g);
    }

    player.draw(g);

    if (Camera.SHOW_CAMERA)
        camera.draw(g);

    g.translate((int) +camera.getX(), (int) +camera.getY());
}

// Triggers an event notifying all registered listeners that the player has died
// Precondition: dieListeners must be initialized.
// Postcondition: All listeners are notified of the player's death.
public void throwPlayerDieEvent() {
    for (PlayerDieListener playerDieListener : dieListeners) {
        playerDieListener.onPlayerDeath();
    }
}

// Adds a listener to be notified when the player dies
// Precondition: listener must not be null.
// Postcondition: The listener is added to dieListeners.
public void addPlayerDieListener(PlayerDieListener listener) {
    dieListeners.add(listener);
}

// Triggers an event notifying all registered listeners that the player has won
// Precondition: winListeners must be initialized.
// Postcondition: All listeners are notified of the player's win.
public void throwPlayerWinEvent() {
    for (PlayerWinListener playerWinListener : winListeners) {
        playerWinListener.onPlayerWin();
    }
}

// Adds a listener to be notified when the player wins
// Precondition: listener must not be null.
// Postcondition: The listener is added to winListeners.
public void addPlayerWinListener(PlayerWinListener listener) {
    winListeners.add(listener);
}

// Checks if the game is currently active
// Precondition: None.
// Postcondition: Returns true if the game is active, false otherwise.
public boolean isActive() {
    return active;
}

// Checks if the player is dead
// Precondition: None.
// Postcondition: Returns true if the player is dead, false otherwise.
public boolean isPlayerDead() {
    return playerDead;
}

// Checks if the player has won the game
// Precondition: None.
// Postcondition: Returns true if the player has won, false otherwise.
public boolean isPlayerWin() {
    return playerWin;
}

// Returns the current game map
// Precondition: None.
// Postcondition: Returns the map object being used in the game.
public Map getMap() {
    return map;
}

// Returns the player object
// Precondition: None.
// Postcondition: Returns the player instance currently in the game.
public Player getPlayer() {
    return player;
}
}