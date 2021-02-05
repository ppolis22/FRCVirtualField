package test;

import org.buzz.projectiondemo.controller.GameStateCalculator;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Point;

public class GameStateCalculatorTest {
    @Test
    public void calibrateTest() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        GameStateCalculator gameStateCalculator = new GameStateCalculator();
        gameStateCalculator.calibrate(new Point(100, 100), new Point(400, 100),
                new Point(100, 400), new Point(400, 400));
        gameStateCalculator.getBoardZones();
    }
}
