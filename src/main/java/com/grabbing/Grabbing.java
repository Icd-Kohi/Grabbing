package com.grabbing;

import com.grabbing.state.LevelState;
import com.jme3.app.SimpleApplication;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;

/**
 * This is the Main Class of your Game. It should boot up your game and do initial initialisation
 * Move your Logic into AppStates or Controls or other java classes
 * TODO:
 * ADD CHARACTER ASSET; OK
 * ADD ITEM ASSET;
 * ADD INVENTORY;
 * MOVE CHARACTER; OK
 * DRAG ITEM ON THE FLOOR;
 * DRAG ITEM INTO/OUT INVENTORY;
 * POLISH DRAG;
 * REFACTOR TO INDIVIDUAL CLASSES;
 * REMOVE MAGIC NUMBERS;
 */

public class Grabbing extends SimpleApplication {

    public Grabbing() {

    }
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1600,900);
        settings.setTitle("Grabbing");
        Grabbing app = new Grabbing();
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        stateManager.attach(new LevelState(this));

    }

    @Override
    public void simpleUpdate(float tpf) {
        //this method will be called every game tick and can be used to make updates

    }

    @Override
    public void simpleRender(RenderManager rm) {
        //add render code here (if any)

    }
}
