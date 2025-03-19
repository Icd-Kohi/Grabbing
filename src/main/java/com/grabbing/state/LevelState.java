package com.grabbing.state;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * This is the Main Class of your Game. It should boot up your game and do initial initialisation
 * Move your Logic into AppStates or Controls or other java classes
 * TODO:
 * ADD CHARACTER ASSET; DONE
 * ADD ITEM ASSET;
 * ADD INVENTORY;
 * MOVE CHARACTER; DONE
 * DRAG ITEM ON THE FLOOR;
 * DRAG ITEM INTO/OUT INVENTORY; (ITEM -> INVENTORY SAME NODES IN ROOT)
 * POLISH DRAG;
 * REFACTOR TO INDIVIDUAL CLASSES;
 * REMOVE MAGIC NUMBERS;
 */

public class LevelState extends AbstractAppState {
    private final Node rootNode;
    private final AssetManager assetManager;
    private final InputManager inputManager;
    // CAM
    private final Vector3f cameraOffset = new Vector3f(0,15,10);
    private final FlyByCamera flyByCamera;
    private final Camera camera;
    // WORLD SIZE
    private final int worldHeight = 30;
    private final int worldWidth = 30;
    // MATERIAL
    Material highlightMat;
    Material mat;
    Material user;
    // SPATIAL
    private Geometry currentHighlight;
    private final int[] GREEN = {152,188,111,80}; //GREEN
    private final Node pivot = new Node("pivot");
    //PLAYER
    private Geometry player;
    // WALK DIRECTIONS
    private final float moveSpeed = 5f;
    // DIRECTIONS
    private boolean left = false, right = false, up = false, down = false;

    public LevelState(SimpleApplication app) {
        rootNode = app.getRootNode();
        assetManager = app.getAssetManager();
        inputManager = app.getInputManager();

        flyByCamera = app.getFlyByCamera();

        camera = app.getCamera();
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);
        rootNode.attachChild(pivot);

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

        inputManager.addListener(actionListener, "Reset", "Up", "Down", "Left", "Right");

        // SUN
//        DirectionalLight sun = new DirectionalLight();
//        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f).normalizeLocal());
//        pivot.addLight(sun);

        //

        flyByCamera.setEnabled(false);

    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            switch (name) {
                case "Reset" -> player.setLocalTranslation(worldWidth/2f, 1, worldHeight /2f);
                case "Up" -> up = keyPressed;
                case "Down" -> down = keyPressed;
                case "Left" -> left = keyPressed;
                case "Right" -> right = keyPressed;
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
        CollisionResults results = new CollisionResults();
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPos, 1f).subtractLocal(origin).normalizeLocal();

        Ray ray = new Ray(origin, direction);
        pivot.collideWith(ray, results);

        if (results.size() > 0) {
            Geometry hover = results.getClosestCollision().getGeometry();

            if (hover != currentHighlight) {
                // REMOVE
                if (currentHighlight != null) {
                    currentHighlight.setMaterial(mat);
                }
                // APPLY
                hover.setMaterial(highlightMat);
                currentHighlight = hover;
            }
        } else {
            if (currentHighlight != null) {
                currentHighlight.setMaterial(mat);
                currentHighlight = null;
            }
        }
    }
    @Override
    public void update(float tpf){
        // PLAYER POS
        Vector3f position = player.getLocalTranslation();

        if (left) position.x -= moveSpeed * tpf;
        if (right) position.x += moveSpeed * tpf;
        if (up)  position.z -= moveSpeed * tpf;
        if (down) position.z += moveSpeed * tpf;

        position.x = Math.max(0.5f, Math.min(worldWidth - 0.5f, position.x));
        position.z = Math.max(0.5f, Math.min(worldHeight - 0.5f, position.z));
        position.y = 1;
        player.setLocalTranslation(position);
        cameraPosition();

        // CURSOR HIGHLIGHTING
        hoverHighlight();
    }
    @Override
    public void cleanup(){
        rootNode.detachChild(pivot);
        super.cleanup();
    }


}
