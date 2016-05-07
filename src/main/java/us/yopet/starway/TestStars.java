/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package us.yopet.starway;

import com.phono.srtplight.Log;
import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author tim
 */
public class TestStars extends Controller {

    boolean _done = false;

    public static void main(String args[]) {
        Log.setLevel(Log.DEBUG);
        String filename = "starway.json";
        if (args.length == 1) {
            filename = args[0];
        }
        TestStars ren;
        try {
            ren = new TestStars(filename);
            ren.setName("TestStars-main");
            Log.debug("starting message thread");
            ren.start();
        } catch (FileNotFoundException ex) {
            System.err.println("Config File " + filename + " not found - giving up");
        }
    }
    Star _currentStar;
    int _starno = 0;

    public TestStars(String confFile) throws FileNotFoundException {
        super(confFile);
        final Timer t = new Timer();
        TimerTask tick = new TimerTask() {
            void colourStarBySize() {
                switch (_currentStar.getSize()) {
                    case 1:
                        _currentStar.setColour(255, 255, 0);
                        break;
                    case 2:
                        _currentStar.setColour(0, 0, 230);
                        break;
                    case 3:
                        _currentStar.setColour(255, 0, 0);
                        break;
                }
            }

            @Override
            public void run() {
                if (_currentStar != null) {
                    colourStarBySize();
                    //_onStars.remove(_currentStar); //make a snake.
                }
                if (_starno < _stars.length) {
                    _currentStar = _stars[_starno++];
                    _currentStar.setColour(255, 255, 255);
                    Log.debug(_currentStar.getName() + " size = "+_currentStar.getSize());
                    _onStars.add(_currentStar);
                } else {
                    _currentStar = null;
                    t.cancel();
                }

            }
        };
        t.schedule(tick, 500, 500);
    }

    @Override
    boolean hasSelectedCard() {
        return !_done;
    }

}
