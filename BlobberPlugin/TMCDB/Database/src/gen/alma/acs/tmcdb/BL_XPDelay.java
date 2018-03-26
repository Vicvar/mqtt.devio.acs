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
 * BL_XPDelay generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@Table(name="`BL_XPDELAY`"
)
@TypeDef(name="BL_XPDelayOp", typeClass=StringEnumUserType.class,
   parameters={ @Parameter(name="enumClassName", value="alma.acs.tmcdb.BL_XPDelayOp") })
public class BL_XPDelay extends alma.acs.tmcdb.translator.TmcdbObject implements java.io.Serializable {


     protected BL_XPDelayId id;
     protected String who;
     protected String changeDesc;
     protected Integer configurationId;
     protected String receiverBand;
     protected String sideBand;
     protected String baseBand;
     protected Double delay;

    public BL_XPDelay() {
    }
   
       @EmbeddedId

    
    @AttributeOverrides( {
        @AttributeOverride(name="`version`", column=@Column(name="VERSION`", nullable=false) ), 
        @AttributeOverride(name="modTime`", column=@Column(name="MODTIME`", nullable=false) ), 
        @AttributeOverride(name="operation`", column=@Column(name="OPERATION`", nullable=false, length=1) ), 
        @AttributeOverride(name="XPDelayId`", column=@Column(name="XPDELAYID`", nullable=false) ) } )
    public BL_XPDelayId getId() {
        return this.id;
    }
    
    public void setId(BL_XPDelayId id) {    
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


    
    @Column(name="`CONFIGURATIONID`", nullable=false)
    public Integer getConfigurationId() {
        return this.configurationId;
    }
    
    public void setConfigurationId(Integer configurationId) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("configurationId", this.configurationId, this.configurationId = configurationId);
        else
            this.configurationId = configurationId;
    }


    
    @Column(name="`RECEIVERBAND`", nullable=false, length=128)
    public String getReceiverBand() {
        return this.receiverBand;
    }
    
    public void setReceiverBand(String receiverBand) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("receiverBand", this.receiverBand, this.receiverBand = receiverBand);
        else
            this.receiverBand = receiverBand;
    }


    
    @Column(name="`SIDEBAND`", nullable=false, length=128)
    public String getSideBand() {
        return this.sideBand;
    }
    
    public void setSideBand(String sideBand) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("sideBand", this.sideBand, this.sideBand = sideBand);
        else
            this.sideBand = sideBand;
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


