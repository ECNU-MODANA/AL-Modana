package ecnu.oe.cosimulationMaster.model;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import org.ptolemy.fmi.*;
/**
 * @Author： aoyi
 * @Description: static information of a FMU
 * @Created by oe on 2018/5/2.
 */
public class FMU {
    private FMUType type;
    private String FMUAddress;
    private String FMUName;
    private FMIModelDescription fmiModelDescriptions;
    private NativeLibrary nativeLibrary;
    private Pointer fmiComponent;
    private Pointer currentState;

    public FMUType getType() {
        return type;
    }

    public String getFMUAddress() {
        return FMUAddress;
    }

    public String getFMUName() {
        return FMUName;
    }

    public FMIModelDescription getFmiModelDescriptions() {
        return fmiModelDescriptions;
    }

    public NativeLibrary getNativeLibrary() {
        return nativeLibrary;
    }

    public Pointer getFmiComponent() {
        return fmiComponent;
    }

    public Pointer getCurrentState() {
        return currentState;
    }

    public void setType(FMUType type) {
        this.type = type;
    }

    public void setFMUAddress(String FMUAddress) {
        this.FMUAddress = FMUAddress;
    }

    public void setFMUName(String FMUName) {
        this.FMUName = FMUName;
    }

    public void setFmiModelDescriptions(FMIModelDescription fmiModelDescriptions) {
        this.fmiModelDescriptions = fmiModelDescriptions;
    }

    public void setNativeLibrary(NativeLibrary nativeLibrary) {
        this.nativeLibrary = nativeLibrary;
    }

    public void setFmiComponent(Pointer fmiComponent) {
        this.fmiComponent = fmiComponent;
    }

    public void setCurrentState(Pointer currentState) {
        this.currentState = currentState;
    }

    public enum FMUType {ct, de}
}