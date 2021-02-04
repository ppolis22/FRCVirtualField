package org.buzz.projectiondemo.model;

public class ProcessResult {

    public static ProcessResult EMPTY = new ProcessResult(new GameState(), new ConvertableMat(),
            new ConvertableMat(), new ConvertableMat(), "");

    public GameState gameState;
    public ConvertableMat mainMat, threshMat, denoiseMat;
    public String debugMessage;

    public ProcessResult(GameState gameState, ConvertableMat mainMat, ConvertableMat threshMat,
                         ConvertableMat denoiseMat, String debugMessage) {
        this.gameState = gameState;
        this.mainMat = mainMat;
        this.threshMat = threshMat;
        this.denoiseMat = denoiseMat;
        this.debugMessage = debugMessage;
    }

}
