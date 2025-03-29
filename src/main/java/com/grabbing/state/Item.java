package com.grabbing.state;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

import java.util.ArrayList;
import static java.lang.Math.round;

public class Item extends AbstractAppState {
    // NODES
    Node itemNode = new Node("Item");
    private final Node rootNode;
    // SETTINGS
    private final AppSettings settings;
    // PLAYER
    public Vector3f playerPosition;
    private final AssetManager assetManager;
    private final Camera camera;
    // INPUT
    private final InputManager inputManager;
    private int toggle;
    // LevelState
    private LevelState levelState;
    // ITEM STATE
    private NodeItem draggedItem;
    private Vector3f dragStartPosition;
    private boolean isDragging = false;
    private final float pickupRange = 1.5f;
    private final float throwRange = 5.0f;
    public ArrayList<NodeItem> worldItems = new ArrayList<>();
    /*
    // "CLOSEST IMPLEMENTATION"
    // CollisionResults results = new CollisionResults();
    // CollisionResults closest = results.getClosestCollision();
    */
    public Item(SimpleApplication app, LevelState levelState) {
            rootNode = app.getRootNode();

            assetManager = app.getAssetManager();
            inputManager = app.getInputManager();
            camera = app.getCamera();
            settings = app.getContext().getSettings();
            this.levelState = levelState;
    }
    public class NodeItem{
        Geometry geometry;
        Material material;
        Vector3f originalPosition;
        ColorRGBA baseColor;
        public NodeItem(String name, Vector3f position, ColorRGBA color){
            Box box = new Box(0.5f, 0.5f, 0.5f);
            geometry = new Geometry("Item", box);

            material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", color);
            geometry.setMaterial(material);
            geometry.setLocalTranslation(position);
            geometry.setName(name);

            itemNode.attachChild(geometry);
            originalPosition = position.clone();
            baseColor = color.clone();
        }
        public void resetColor() {
            material.setColor("Color", baseColor);
        }

    }
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        levelState = new LevelState((SimpleApplication) app);
        this.playerPosition = levelState.player.getLocalTranslation();

    }
    public void spawnItem() {
        worldItems.add(new NodeItem("second", new Vector3f(3, 1, 3), ColorRGBA.Yellow));
    }
    public void initInput() {
        inputManager.addMapping("Grab", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "Grab");
    }
    private final ActionListener actionListener = (String name, boolean keyPressed, float tpf) -> {
        if (name.equals("Grab")) {
            toggle = keyPressed ? 1 : 0;
            if (keyPressed) {
                startDrag();
                System.out.println("pressing");
                if(isDragging && draggedItem != null) {
                    draggedItem.material.setColor("Color", ColorRGBA.White);
                }
            } else {
                if (isDragging && draggedItem != null) {
                    System.out.println("dragging");
                    updateDragPosition();
                }
                endDrag();
                System.out.println("end drag");
            }
        }
    };
    public void startDrag() {
        Vector2f cursorPosition = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPosition, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPosition, 1f).subtractLocal(origin).normalizeLocal();

        Ray ray = new Ray(origin, direction);

        CollisionResults results = new CollisionResults();

        itemNode.collideWith(ray, results);

        if (results.size() > 0) {
            Geometry closestTarget = results.getClosestCollision().getGeometry();
            for (NodeItem item : this.worldItems) {
                if (item.geometry == closestTarget && isInPickupRange(item)) {
                    draggedItem = item;
                    dragStartPosition = item.geometry.getLocalTranslation().clone();
                    isDragging = true;
                    return;
                }
            }
        }
    }
    // final position must be rounded
    public void endDrag() {
        if (draggedItem == null) return;
        // ROUND
        Vector3f finalPosition = draggedItem.geometry.getLocalTranslation();
        float distance = finalPosition.distance(dragStartPosition);

        if (distance <= throwRange) {
            draggedItem.originalPosition = finalPosition.clone();
            System.out.println("final position: " + finalPosition);
        } else {
            draggedItem.geometry.setLocalTranslation(draggedItem.originalPosition);
        }
        draggedItem.resetColor();
        draggedItem = null;
        isDragging = false;
    }
    // TODO
    public void updateDragPosition() {
        if (draggedItem != null) {
            draggedItem.geometry.setLocalTranslation(calculateDragPosition());
        }
    }
    // TODO
    private boolean isInPickupRange(NodeItem item) {
        playerPosition = levelState.player.getLocalTranslation();

        Vector3f itemPosition = item.geometry.getLocalTranslation();
        float distance = (float) Math.sqrt(Math.pow(playerPosition.x - itemPosition.x, 2) + Math.pow(playerPosition.z - itemPosition.z, 2)
        );
        return distance <= this.pickupRange;
    }

    private Vector3f calculateDragPosition() {
        Vector2f cursorPosition = inputManager.getCursorPosition();

        Vector3f origin = camera.getWorldCoordinates(cursorPosition, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPosition, 1f).subtractLocal(origin).normalizeLocal();

        float nullY = -origin.y / direction.y;
        Vector3f intersection = origin.add(direction.mult(nullY));
        float intersectionX=  (round(intersection.x));
        float intersectionZ=  (round(intersection.z));
        Vector3f roundedIntersection = new Vector3f(intersectionX, intersection.y, intersectionZ);
        float distance = intersection.distance(dragStartPosition);

        if (distance > throwRange
                || roundedIntersection.x < 0.5f
                || roundedIntersection.z < 0.5f
                || roundedIntersection.x > levelState.worldHeight-0.5f
                || roundedIntersection.z > levelState.worldHeight-0.5f
        ){
            return new Vector3f(dragStartPosition);
        }

        return new Vector3f(intersectionX, 1.0f,intersectionZ);

    }
    public void highlightHoveredItems() {
        Vector2f cursorPosition = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPosition, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPosition, 1f).subtractLocal(origin).normalizeLocal();

        Ray ray = new Ray(origin, direction);

        CollisionResults results = new CollisionResults();
        itemNode.collideWith(ray, results);

        for (NodeItem item : worldItems) {
            if (isInPickupRange(item) && results.size() > 0) {
                if(toggle == 1){
                    item.material.setColor("Color", ColorRGBA.White);
                } else {
                    item.material.setColor("Color", ColorRGBA.Red);
                }
            } else {
               item.resetColor();
            }
        }
    }
}

