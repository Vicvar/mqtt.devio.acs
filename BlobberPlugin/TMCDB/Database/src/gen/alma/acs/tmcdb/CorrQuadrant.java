package alma.acs.tmcdb;
// Generated Jan 25, 2018 5:26:44 PM by Hibernate Tools 4.3.1.Final


import alma.hibernate.util.StringEnumUserType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

/**
 * CorrQuadrant generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@Table(name="`CORRQUADRANT`"
)
@TypeDef(name="CorrQuadBBEnum", typeClass=StringEnumUserType.class,
   parameters={ @Parameter(name="enumClassName", value="alma.acs.tmcdb.CorrQuadBBEnum") })
public class CorrQuadrant extends alma.acs.tmcdb.translator.TmcdbObject implements java.io.Serializable {


     protected Integer baseElementId;
     protected BaseElement baseElement;
     protected CorrQuadBBEnum baseBand;
     protected Byte quadrant;
     protected Byte channelNumber;
     private Set<CorrQuadrantRack> corrQuadrantRacks = new HashSet<CorrQuadrantRack>(0);

    public CorrQuadrant() {
    }
   
       @GenericGenerator(name="generator", strategy="foreign", parameters=@Parameter(name="property", value="baseElement"))@Id @GeneratedValue(generator="generator")

    
    @Column(name="`BASEELEMENTID`", unique=true, nullable=false)
    public Integer getBaseElementId() {
        return this.baseElementId;
    }
    
    public void setBaseElementId(Integer baseElementId) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("baseElementId", this.baseElementId, this.baseElementId = baseElementId);
        else
            this.baseElementId = baseElementId;
    }


@OneToOne(fetch=FetchType.LAZY)@PrimaryKeyJoinColumn
    public BaseElement getBaseElement() {
        return this.baseElement;
    }
    
    public void setBaseElement(BaseElement baseElement) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("baseElement", this.baseElement, this.baseElement = baseElement);
        else
            this.baseElement = baseElement;
    }


    
    @Column(name="`BASEBAND`", nullable=false, length=128)
	@Type(type="CorrQuadBBEnum")
    public CorrQuadBBEnum getBaseBand() {
        return this.baseBand;
    }
    
    public void setBaseBand(CorrQuadBBEnum baseBand) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("baseBand", this.baseBand, this.baseBand = baseBand);
        else
            this.baseBand = baseBand;
    }


    
    @Column(name="`QUADRANT`", nullable=false)
    public Byte getQuadrant() {
        return this.quadrant;
    }
    
    public void setQuadrant(Byte quadrant) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("quadrant", this.quadrant, this.quadrant = quadrant);
        else
            this.quadrant = quadrant;
    }


    
    @Column(name="`CHANNELNUMBER`", nullable=false)
    public Byte getChannelNumber() {
        return this.channelNumber;
    }
    
    public void setChannelNumber(Byte channelNumber) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("channelNumber", this.channelNumber, this.channelNumber = channelNumber);
        else
            this.channelNumber = channelNumber;
    }


@OneToMany(fetch=FetchType.LAZY, mappedBy="corrQuadrant")
    public Set<CorrQuadrantRack> getCorrQuadrantRacks() {
        return this.corrQuadrantRacks;
    }
    
    public void setCorrQuadrantRacks(Set<CorrQuadrantRack> corrQuadrantRacks) {    
    	this.corrQuadrantRacks = corrQuadrantRacks;
    }

	public void addCorrQuadrantRacks(Set<CorrQuadrantRack> elements) {
		if( this.corrQuadrantRacks != null )
			for(Iterator<CorrQuadrantRack> it = elements.iterator(); it.hasNext(); )
				addCorrQuadrantRackToCorrQuadrantRacks((CorrQuadrantRack)it.next());
	}

	public void addCorrQuadrantRackToCorrQuadrantRacks(CorrQuadrantRack element) {
		if( !this.corrQuadrantRacks.contains(element) ) {
			this.corrQuadrantRacks.add(element);
		}
	}





}


