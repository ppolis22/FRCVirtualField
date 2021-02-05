package org.buzz.projectiondemo.model;

import java.util.ArrayList;
import java.util.List;

public class ProcessResult {

    public static ProcessResult EMPTY = new ProcessResult(new ArrayList<>(), new ConvertableMat(),
            new ConvertableMat(), new ConvertableMat(), "");

    public List<Contour> contours;
    public ConvertableMat mainMat, threshMat, denoiseMat;
    public String debugMessage;

    public ProcessResult(List<Contour> contours, ConvertableMat mainMat, ConvertableMat threshMat,
                         ConvertableMat denoiseMat, String debugMessage) {
        this.contours = contours;
        this.mainMat = mainMat;
        this.threshMat = threshMat;
        this.denoiseMat = denoiseMat;
        this.debugMessage = debugMessage;
    }

}
