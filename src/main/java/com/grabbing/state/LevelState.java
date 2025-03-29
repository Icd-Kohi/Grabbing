package com.grabbing.state;

import com.grabbing.Grabbing;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.awt.Component;


/**
 * This is the Main Class of your Game. It should boot up your game and do initial initialisation
 * Move your Logic into AppStates or Controls or other java classes
 * TODO:
 * ADD CHARACTER ASSET; OK
 * ADD ITEM ASSET; OK
 * ADD INVENTORY;
 * MOVE CHARACTER; OK
 * DRAG ITEM ON THE FLOOR; OK
 * DRAG ITEM INTO/OUT INVENTORY;
 * POLISH DRAG;
 * REFACTOR TO INDIVIDUAL CLASSES;
 * REMOVE MAGIC NUMBERS;
 */

public class LevelState extends AbstractAppState {
    // NODES
    public final Node rootNode;
    public final Node guiNode;
    final Node pivot = new Node("pivot");
    Node itemNode = new Node("item");
    // INPUT
    final AssetManager assetManager;
    private final InputManager inputManager;
    // CAM
    public final Vector3f cameraOffset = new Vector3f(0,18,5);
    private final FlyByCamera flyByCamera;
    public final Camera camera;
    // WORLD SIZE
    final int worldHeight = 30;
    final int worldWidth = 30;
    // MATERIAL
    private final BitmapFont guiFont;
    public Component settings;
    private Material highlightMat;
    private Material mat;
    private Material user;
    // SPATIAL
    private Geometry currentHighlight;
    private final int[] GREEN = {152,188,111,80}; //GREEN
    //PLAYER
    public Geometry player;
    public Vector3f playerPosition;
    private BitmapText positionText;
    // WALK DIRECTIONS
    private final float moveSpeed = 5.0f;
    // DIRECTIONS
    private boolean left = false, right = false, up = false, down = false;
    // RESOLUTION
    private static final int resolutionWidth = Grabbing.resolutionWidth;
    private static final int resolutionHeight = Grabbing.resolutionHeight;
    // INPUT STATE
    private int pressingP = 0;
    public boolean isDragging = false;

    // ITEM
    private Item item;
    // INVENTORY
    private Inventory inventory;

    public LevelState(SimpleApplication app) {
        rootNode = app.getRootNode();
        guiNode = app.getGuiNode();
        guiFont = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        assetManager = app.getAssetManager();
        inputManager = app.getInputManager();
        flyByCamera = app.getFlyByCamera();
        camera = app.getCamera();
    }
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);
        item = new Item((SimpleApplication) app,this);
        itemNode = item.itemNode;
        rootNode.attachChild(pivot);
        rootNode.attachChild(itemNode);
        // MATERIALS
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.fromRGBA255(GREEN[0],GREEN[1],GREEN[2],GREEN[3]));

        highlightMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        highlightMat.setColor("Color", ColorRGBA.Gray);

        worldSize(worldWidth, worldHeight);

        // PLAYER
        createPlayer();

        // BUTTONS
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Position", new KeyTrigger(KeyInput.KEY_P));

        inputManager.addListener(actionListener, "Reset", "Up", "Down", "Left", "Right", "Position");
        // GRAB INPUT MAP/LISTENER
        item.initInput();
        item.spawnItem();

        // SUN
//        DirectionalLight sun = new DirectionalLight();
//        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f).normalizeLocal());
//        pivot.addLight(sun);

        //
        // ITEMS
        flyByCamera.setEnabled(false);

    }
    public final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            switch (name) {
                case "Reset":
                    player.setLocalTranslation(worldWidth / 2f, 1, worldHeight / 2f);
                    break;
                case "Up":
                    up = keyPressed;
                    break;
                case "Down":
                    down = keyPressed;
                    break;
                case "Left":
                    left = keyPressed;
                    break;
                case "Right":
                    right = keyPressed;
                    break;
                case "Position":
                    pressingP = keyPressed ? 1 : 0;
                    if(pressingP == 1){
                        initPositionText();
                    }else if(guiNode != null){
                        guiNode.detachAllChildren();
                    }
                    break;
            }
        }
    };

    private void createPlayer(){
        // PLACEHOLDER ASSET
        Box player1 = new Box(0.5f, 0.5f, 0.5f);
        player = new Geometry("Player",player1);
        user = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        user.setColor("Color", ColorRGBA.White);
        player.setMaterial(user);

        player.setLocalTranslation(new Vector3f(worldWidth/2f, 1, worldHeight /2f));
        rootNode.attachChild(player);
    }
    private void worldSize(int width, int height) {
        Box box = new Box(0.5f, 0.5f, 0.5f);
        // BLOCKS
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                Geometry geom = new Geometry("Block", box);
                geom.setMaterial(mat);
                geom.setLocalTranslation(x, 0, z);
                pivot.attachChild(geom);
            }
        }
    }
    private void cameraPosition(){
        Vector3f playerPos = player.getLocalTranslation();
        Vector3f cameraPos = playerPos.add(cameraOffset);
        camera.setLocation(cameraPos);
        camera.lookAt(playerPos, Vector3f.UNIT_Y);
    }
    private void hoverHighlight(){
        CollisionResults collisionResults = new CollisionResults();

        Vector2f cursorPosition = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPosition, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPosition, 1f).subtractLocal(origin).normalizeLocal();
        Ray rayCasting = new Ray(origin, direction);
        pivot.collideWith(rayCasting, collisionResults);

        if (collisionResults.size() > 0) {
            Geometry hoverClosest = collisionResults.getClosestCollision().getGeometry();
            pivot.attachChild(hoverClosest);
            if (hoverClosest != currentHighlight) {
                // REMOVE
                if (currentHighlight != null) {
                    currentHighlight.setMaterial(mat);
                }
                // APPLY
                hoverClosest.setMaterial(highlightMat);
                currentHighlight = hoverClosest;
            }
        }

    }
    @Override
    public void update(float tpf){
        // PLAYER POS
        playerPosition = player.getLocalTranslation();

        if (left) playerPosition.x -= moveSpeed * tpf;
        if (right) playerPosition.x += moveSpeed * tpf;
        if (up)  playerPosition.z -= moveSpeed * tpf;
        if (down) playerPosition.z += moveSpeed * tpf;

        playerPosition.x = Math.max(0.5f, Math.min(worldWidth - 0.5f, playerPosition.x));
        playerPosition.z = Math.max(0.5f, Math.min(worldHeight - 0.5f, playerPosition.z));
        playerPosition.y = 1;

        player.setLocalTranslation(playerPosition);
        cameraPosition();

        // CURSOR HIGHLIGHTING
        hoverHighlight();
        item.highlightHoveredItems();
        updatePositionText();
    }

    @Override
    public void cleanup(){
        rootNode.detachChild(pivot);
        super.cleanup();
    }
    public void initPositionText(){
        if(guiNode != null){
            positionText = new BitmapText(guiFont);
            positionText.setSize(guiFont.getCharSet().getRenderedSize());
            positionText.setColor(ColorRGBA.White);
            positionText.setLocalTranslation(10, resolutionHeight/2f, 0); // Top-left corner
            guiNode.attachChild(positionText);
        }
    }
    public void updatePositionText(){
        if(pressingP == 1){
            positionText.setText(
                    String.format("Position:\nX: %.1f\nZ: %.1f", playerPosition.x, playerPosition.z)
            );
        }

    }

    //TODO:
    //INVENTORY:
    //rootNode --> INVENTORY
    //        |--> pivot --> ITEM
    //        |         |--> HIGHLIGHT
    //        | ...
    //KEEP ITS STATE (ITEMS) WHEN OPENED/CLOSED
    //
    //GET CURSOR POSITION, IF SAME POS AS ITEM,
}
