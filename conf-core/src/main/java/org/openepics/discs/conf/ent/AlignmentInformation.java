package org.openepics.discs.conf.ent;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AlignmentInformation {
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "global_x")
    private Double globalX;

    @Column(name = "global_y")
    private Double globalY;

    @Column(name = "global_z")
    private Double globalZ;

    @Column(name = "global_pitch")
    private Double globalPitch;

    @Column(name = "global_yaw")
    private Double globalYaw;

    @Column(name = "global_roll")
    private Double globalRoll;

    public Double getGlobalX() { return globalX; }
    public void setGlobalX(Double globalX) { this.globalX = globalX; }

    public Double getGlobalY() { return globalY; }
    public void setGlobalY(Double globalY) { this.globalY = globalY; }

    public Double getGlobalZ() { return globalZ; }
    public void setGlobalZ(Double globalZ) { this.globalZ = globalZ; }

    public Double getGlobalPitch() { return globalPitch; }
    public void setGlobalPitch(Double globalPitch) { this.globalPitch = globalPitch; }

    public Double getGlobalYaw() { return globalYaw; }
    public void setGlobalYaw(Double globalYaw) { this.globalYaw = globalYaw; }

    public Double getGlobalRoll() { return globalRoll; }
    public void setGlobalRoll(Double globalRoll) { this.globalRoll = globalRoll; }
}