package br.gov.caixa.sispl.infra.ui;

import java.awt.Dimension;
import java.awt.Point;


public class EFLVirtualKeyboardCalculator {

    private String keyLabel;
    private String keySkin;
    private int keyCode;
    private Point location;
    private Dimension size;

    public EFLVirtualKeyboardCalculator(String keyLabel, String keySkin, int keyCode, Point location, Dimension size) {
        this.keyLabel = keyLabel;
        this.keySkin = keySkin;
        this.keyCode = keyCode;
        this.location = location;
        this.size = size;
    }


    public int getKeyCode() {
        return keyCode;
    }


    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }


    public String getKeyLabel() {
        return keyLabel;
    }


    public void setKeyLabel(String keyLabel) {
        this.keyLabel = keyLabel;
    }


    public String getKeySkin() {
        return keySkin;
    }


    public void setKeySkin(String keySkin) {
        this.keySkin = keySkin;
    }


    public Point getLocation() {
        return location;
    }


    public void setLocation(Point location) {
        this.location = location;
    }


    public Dimension getSize() {
        return size;
    }


    public void setSize(Dimension size) {
        this.size = size;
    }
}