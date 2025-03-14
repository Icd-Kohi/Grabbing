package com.grabbing;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;



/**
 * This is the Main Class of your Game. It should boot up your game and do initial initialisation
 * Move your Logic into AppStates or Controls or other java classes

 * TODO:
 * ADD CHARACTER ASSET;
 * ADD ITEM ASSET;
 * ADD INVENTORY;
 * MOVE CHARACTER;
 * DRAG ITEM ON THE FLOOR;
 * DRAG ITEM INTO/OUT INVENTORY;
 * POLISH DRAG;
 * REFACTOR TO INDIVIDUAL CLASSES;
 * REMOVE MAGIC NUMBERS;
 */
public class Grabbing extends SimpleApplication {
    private final int worldHeigth = 30;
    private final int worldWidth = 30;
    Material highlightMat;
    Material mat;
    private Geometry currentHighlight;
    private final int[] GREEN = {152,188,111,80}; //GREEN
    private final Node pivot = new Node("pivot");

    public Grabbing() {
        super(new FlyCamAppState());
    }
    public static void main(String[] args) {
        Grabbing app = new Grabbing();

        app.start();
    }

    @Override
    public void simpleInitApp() {
        rootNode.attachChild(pivot);
        flyCam.setMoveSpeed(5);
        flyCam.setDragToRotate(true);
        flyCam.setRotationSpeed(5);

        // MATERIALS
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.fromRGBA255(GREEN[0],GREEN[1],GREEN[2],GREEN[3]));

        highlightMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        highlightMat.setColor("Color", ColorRGBA.Gray);
        worldSize(worldWidth, worldHeigth);

        // CAMERA POSITION
        cam.setLocation(new Vector3f(10, 0,10));
        cam.lookAt(new Vector3f(10, 10, 2),Vector3f.UNIT_Y);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f).normalizeLocal());
        pivot.addLight(sun);
    }


    private void worldSize(int width, int height) {
        Box box = new Box(0.5f, 0.5f, 0.5f);

        // BLOCKS
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Geometry geom = new Geometry("Block", box);
                geom.setMaterial(mat);
                geom.setLocalTranslation(x, y, 0);
                pivot.attachChild(geom);
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        //this method will be called every game tick and can be used to make updates
        CollisionResults results = new CollisionResults();
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = cam.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = cam.getWorldCoordinates(cursorPos, 1f).subtractLocal(origin).normalizeLocal();

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
    public void simpleRender(RenderManager rm) {
        //add render code here (if any)
    }
}
