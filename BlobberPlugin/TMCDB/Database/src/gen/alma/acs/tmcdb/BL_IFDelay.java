package alma.acs.tmcdb;
// Generated Jan 25, 2018 5:26:44 PM by Hibernate Tools 4.3.1.Final


import alma.hibernate.util.StringEnumUserType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;

/**
 * BL_IFDelay generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@Table(name="`BL_IFDELAY`"
)
@TypeDef(name="BL_IFDelayOp", typeClass=StringEnumUserType.class,
   parameters={ @Parameter(name="enumClassName", value="alma.acs.tmcdb.BL_IFDelayOp") })
public class BL_IFDelay extends alma.acs.tmcdb.translator.TmcdbObject implements java.io.Serializable {


     protected BL_IFDelayId id;
     protected String who;
     protected String changeDesc;
     protected Integer antennaId;
     protected String baseBand;
     protected String polarization;
     protected String IFSwitch;
     protected Double delay;

    public BL_IFDelay() {
    }
   
       @EmbeddedId

    
    @AttributeOverrides( {
        @AttributeOverride(name="`version`", column=@Column(name="VERSION`", nullable=false) ), 
        @AttributeOverride(name="modTime`", column=@Column(name="MODTIME`", nullable=false) ), 
        @AttributeOverride(name="operation`", column=@Column(name="OPERATION`", nullable=false, length=1) ), 
        @AttributeOverride(name="IFDelayId`", column=@Column(name="IFDELAYID`", nullable=false) ) } )
    public BL_IFDelayId getId() {
        return this.id;
    }
    
    public void setId(BL_IFDelayId id) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
        else
            this.id = id;
    }


    
    @Column(name="`WHO`", length=128)
    public String getWho() {
        return this.who;
    }
    
    public void setWho(String who) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("who", this.who, this.who = who);
        else
            this.who = who;
    }


    
    @Column(name="`CHANGEDESC`", length=16777216)
    public String getChangeDesc() {
        return this.changeDesc;
    }
    
    public void setChangeDesc(String changeDesc) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("changeDesc", this.changeDesc, this.changeDesc = changeDesc);
        else
            this.changeDesc = changeDesc;
    }


    
    @Column(name="`ANTENNAID`", nullable=false)
    public Integer getAntennaId() {
        return this.antennaId;
    }
    
    public void setAntennaId(Integer antennaId) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("antennaId", this.antennaId, this.antennaId = antennaId);
        else
            this.antennaId = antennaId;
    }


    
    @Column(name="`BASEBAND`", nullable=false, length=128)
    public String getBaseBand() {
        return this.baseBand;
    }
    
    public void setBaseBand(String baseBand) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("baseBand", this.baseBand, this.baseBand = baseBand);
        else
            this.baseBand = baseBand;
    }


    
    @Column(name="`POLARIZATION`", nullable=false, length=128)
    public String getPolarization() {
        return this.polarization;
    }
    
    public void setPolarization(String polarization) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("polarization", this.polarization, this.polarization = polarization);
        else
            this.polarization = polarization;
    }


    
    @Column(name="`IFSWITCH`", nullable=false, length=128)
    public String getIFSwitch() {
        return this.IFSwitch;
    }
    
    public void setIFSwitch(String IFSwitch) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("IFSwitch", this.IFSwitch, this.IFSwitch = IFSwitch);
        else
            this.IFSwitch = IFSwitch;
    }


    
    @Column(name="`DELAY`", nullable=false, precision=64, scale=0)
    public Double getDelay() {
        return this.delay;
    }
    
    public void setDelay(Double delay) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("delay", this.delay, this.delay = delay);
        else
            this.delay = delay;
    }





}


