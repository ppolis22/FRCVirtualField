package org.buzz.projectiondemo.model;

import java.util.ArrayList;
import java.util.List;

public class ProcessResult {

    public static ProcessResult EMPTY = new ProcessResult(new ArrayList<>(),
            new ConvertableMat(), new ConvertableMat(), "");

    public List<Contour> contours;
    public ConvertableMat threshMat, denoiseMat;
    public String debugMessage;

    public ProcessResult(List<Contour> contours, ConvertableMat threshMat,
                         ConvertableMat denoiseMat, String debugMessage) {
        this.contours = contours;
        this.threshMat = threshMat;
        this.denoiseMat = denoiseMat;
        this.debugMessage = debugMessage;
    }

}
