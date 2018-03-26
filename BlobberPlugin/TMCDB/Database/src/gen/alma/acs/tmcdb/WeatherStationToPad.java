package alma.acs.tmcdb;
// Generated Jan 25, 2018 5:26:44 PM by Hibernate Tools 4.3.1.Final


import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * WeatherStationToPad generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@Table(name="`WEATHERSTATIONTOPAD`"
)
public class WeatherStationToPad extends alma.acs.tmcdb.translator.TmcdbObject implements java.io.Serializable {


     protected WeatherStationToPadId id;
     protected Pad pad;
     protected WeatherStationController weatherStationController;
     protected Long endTime;
     protected Boolean planned;

    public WeatherStationToPad() {
    }
   
       @EmbeddedId

    
    @AttributeOverrides( {
        @AttributeOverride(name="`weatherStationId`", column=@Column(name="WEATHERSTATIONID`", nullable=false) ), 
        @AttributeOverride(name="padId`", column=@Column(name="PADID`", nullable=false) ), 
        @AttributeOverride(name="startTime`", column=@Column(name="STARTTIME`", nullable=false) ) } )
    public WeatherStationToPadId getId() {
        return this.id;
    }
    
    public void setId(WeatherStationToPadId id) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
        else
            this.id = id;
    }


@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`PADID`", nullable=false, insertable=false, updatable=false)
    public Pad getPad() {
        return this.pad;
    }
    
    public void setPad(Pad pad) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("pad", this.pad, this.pad = pad);
        else
            this.pad = pad;
    }


@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`WEATHERSTATIONID`", nullable=false, insertable=false, updatable=false)
    public WeatherStationController getWeatherStationController() {
        return this.weatherStationController;
    }
    
    public void setWeatherStationController(WeatherStationController weatherStationController) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("weatherStationController", this.weatherStationController, this.weatherStationController = weatherStationController);
        else
            this.weatherStationController = weatherStationController;
    }


    
    @Column(name="`ENDTIME`")
    public Long getEndTime() {
        return this.endTime;
    }
    
    public void setEndTime(Long endTime) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("endTime", this.endTime, this.endTime = endTime);
        else
            this.endTime = endTime;
    }


    
    @Column(name="`PLANNED`", nullable=false)
    public Boolean getPlanned() {
        return this.planned;
    }
    
    public void setPlanned(Boolean planned) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("planned", this.planned, this.planned = planned);
        else
            this.planned = planned;
    }





}


