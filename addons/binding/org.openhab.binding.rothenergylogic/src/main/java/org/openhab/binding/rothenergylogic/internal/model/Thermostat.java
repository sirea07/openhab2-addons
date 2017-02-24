package org.openhab.binding.rothenergylogic.internal.model;

public class Thermostat {
    private String webserverId;
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
    private String webserverIpAddress;

    public static final String KEY_INTERNAL_ID = "id";
    public static final String KEY_INTERNAL_OWNER_ID = "ownerId";
    public static final String KEY_INTERNAL_NAME = "name";
    public static final String KEY_INTERNAL_TEMPERATURE_ACTUAL = org.openhab.binding.rothenergylogic.RothEnergyLogicBindingConstants.CHANNEL_ACTUAL_TEMP;
    public static final String KEY_INTERNAL_TEMPERATURE_SETPOINT = org.openhab.binding.rothenergylogic.RothEnergyLogicBindingConstants.CHANNEL_SET_TEMP;
    public static final String KEY_INTERNAL_TEMPERATURE_SETPOINT_MIN = "setPointMin";
    public static final String KEY_INTERNAL_TEMPERATURE_SETPOINT_MAX = "setPointMax";
    public static final String KEY_INTERNAL_TEMPERATURE_SETPOINT_STEP = "setPointSteps";
    public static final String KEY_INTERNAL_TEMPERATURE_SI_UNIT = "tempSIUnit";
    public static final String KEY_INTERNAL_WEEK_PROGRAM = "weekProg";
    public static final String KEY_INTERNAL_WEEK_PROGRAM_ENABLED = "weekProgEnabled";
    public static final String KEY_INTERNAL_OPMODE = "opMode";
    public static final String KEY_INTERNAL_OPMODE_ENABLED = "opModeEnabled";
    public final static String KEY_INTERNAL_WEBSERVER_ID = "webserverId";
    public final static String KEY_INTERNAL_WEBSERVER_IP_ADDRESS = "webserverIPAddress";

    public String getWebserverId() {
        return webserverId;
    }

    public void setWebserverId(String webserverId) {
        this.webserverId = webserverId;
    }

    public String getUniqueId() {
        // id of thermostats that are registered to different modules might/will be equal; guarantee uniqueness through
        // coupling of id with webserver ip address
        // since "." is not allowed as UID for a thing, replace it with "x"
        return String.format("%s_%s", this.getWebserverIpAddress().replaceAll("\\.", "x"), this.getId());
    }

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

    public String getWebserverIpAddress() {
        return webserverIpAddress;
    }

    public void setWebserverIpAddress(String webserverIpAddress) {
        this.webserverIpAddress = webserverIpAddress;
    }
}
