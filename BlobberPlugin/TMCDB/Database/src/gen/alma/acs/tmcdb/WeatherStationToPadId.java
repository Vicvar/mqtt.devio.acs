package alma.acs.tmcdb;
// Generated Jan 25, 2018 5:26:44 PM by Hibernate Tools 4.3.1.Final


import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * WeatherStationToPadId generated by hbm2java
 */
@SuppressWarnings("serial")
@Embeddable
public class WeatherStationToPadId  implements java.io.Serializable {


     private Integer weatherStationId;
     private Integer padId;
     private Long startTime;

    public WeatherStationToPadId() {
    }
   


    @Column(name="`WEATHERSTATIONID`", nullable=false)
    public Integer getWeatherStationId() {
        return this.weatherStationId;
    }
    
    public void setWeatherStationId(Integer weatherStationId) {    
    	this.weatherStationId = weatherStationId;
    }



    @Column(name="`PADID`", nullable=false)
    public Integer getPadId() {
        return this.padId;
    }
    
    public void setPadId(Integer padId) {    
    	this.padId = padId;
    }



    @Column(name="`STARTTIME`", nullable=false)
    public Long getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(Long startTime) {    
    	this.startTime = startTime;
    }



   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof WeatherStationToPadId) ) return false;
		 WeatherStationToPadId castOther = ( WeatherStationToPadId ) other;

		 return ( (this.getWeatherStationId()==castOther.getWeatherStationId()) || ( this.getWeatherStationId()!=null && castOther.getWeatherStationId()!=null && this.getWeatherStationId().equals(castOther.getWeatherStationId()) ) )
 && ( (this.getPadId()==castOther.getPadId()) || ( this.getPadId()!=null && castOther.getPadId()!=null && this.getPadId().equals(castOther.getPadId()) ) )
 && ( (this.getStartTime()==castOther.getStartTime()) || ( this.getStartTime()!=null && castOther.getStartTime()!=null && this.getStartTime().equals(castOther.getStartTime()) ) );
   }

   public int hashCode() {
         int result = 17;

         result = 37 * result + ( getWeatherStationId() == null ? 0 : this.getWeatherStationId().hashCode() );
         result = 37 * result + ( getPadId() == null ? 0 : this.getPadId().hashCode() );
         result = 37 * result + ( getStartTime() == null ? 0 : this.getStartTime().hashCode() );
         return result;
   }


}


