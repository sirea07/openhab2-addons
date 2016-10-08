package org.openhab.binding.rothenergylogic.internal.model;

public class Thermostat {
    private String id;
    private String ownerId;
    private String name;
    private double actualTemperature;
    private double setPointTemperature;
    private double setPointMinTemperature;
    private double setPointMaxTemperature;
    private double setPointStepValue;
    private String temperatureSIUnit;
    private String weekProgram;
    private boolean weekProgramEnabled;
    private String opMode;
    private boolean opModeEnabled;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getActualTemperature() {
        return actualTemperature;
    }

    public void setActualTemperature(double temperature) {
        this.actualTemperature = temperature;
    }

    public double getSetPointTemperature() {
        return setPointTemperature;
    }

    public void setSetPointTemperature(double setPointTemperature) {
        this.setPointTemperature = setPointTemperature;
    }

    public double getSetPointMinTemperature() {
        return setPointMinTemperature;
    }

    public void setSetPointMinTemperature(double setPointMinTemperature) {
        this.setPointMinTemperature = setPointMinTemperature;
    }

    public double getSetPointMaxTemperature() {
        return setPointMaxTemperature;
    }

    public void setSetPointMaxTemperature(double setPointMaxTemperature) {
        this.setPointMaxTemperature = setPointMaxTemperature;
    }

    public double getSetPointStepValue() {
        return setPointStepValue;
    }

    public void setSetPointStepValue(double setPointStepValue) {
        this.setPointStepValue = setPointStepValue;
    }

    public String getWeekProgram() {
        return weekProgram;
    }

    public void setWeekProgram(String weekProgram) {
        this.weekProgram = weekProgram;
    }

    public boolean isWeekProgramEnabled() {
        return weekProgramEnabled;
    }

    public void setWeekProgramEnabled(boolean weekProgramEnabled) {
        this.weekProgramEnabled = weekProgramEnabled;
    }

    public String getOpMode() {
        return opMode;
    }

    public void setOpMode(String opMode) {
        this.opMode = opMode;
    }

    public boolean isOpModeEnabled() {
        return opModeEnabled;
    }

    public void setOpModeEnabled(boolean opModeEnabled) {
        this.opModeEnabled = opModeEnabled;
    }

    public String getTemperatureSIUnit() {
        return temperatureSIUnit;
    }

    public void setTemperatureSIUnit(String temperatureSIUnit) {
        this.temperatureSIUnit = temperatureSIUnit;
    }
}
