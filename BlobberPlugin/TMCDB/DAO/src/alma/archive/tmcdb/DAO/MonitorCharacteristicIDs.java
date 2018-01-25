/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.archive.tmcdb.DAO;


public class MonitorCharacteristicIDs{
	private Integer configurationId = -1;
	private Integer hwConfigurationId = -1;
	private Integer assemblyId = -1;
	private Integer componentId = -1;
	private Integer baciPropertyId = -1;
	private Integer monitorPointId = -1;
	private String serialNumber = "";
	private int index = -1;
	private boolean isOnDB = false;
	private String monitorPointName = "generic";

	public MonitorCharacteristicIDs(){}

	public Integer getConfigurationId(){
		return this.configurationId;
	}

	public void setConfigurationId(Integer configuration){
		this.configurationId=configuration;
	}

	public Integer getHwConfigurationId(){
		return this.hwConfigurationId;
	}

	public void setHwConfigurationId(Integer hwConfiguration){
		this.hwConfigurationId=hwConfiguration;
	}

	public Integer getAssemblyId(){
		return this.assemblyId;
	}

	public void setAssemblyId(Integer assembly){
		this.assemblyId=assembly;
	}

	public String getSerialNumber(){
		return this.serialNumber;
	}

	public void setSerialNumber(String serialNumber){
		this.serialNumber = serialNumber;
	}

	 public Integer getComponentId(){
		return this.componentId;
	}

	public void setComponentId(Integer component){
		this.componentId=component;
	}
	 public Integer getBACIPropertyId(){
		return this.baciPropertyId;
	}

	public void setBACIPropertyId(Integer baciProperty){
		this.baciPropertyId=baciProperty;
	}

	 public Integer getMonitorPointId(){
		return this.monitorPointId;
	}

	public void setMonitorPointId(Integer monitorPointId){
		this.monitorPointId=monitorPointId ;
	}

	 public int getIndex(){
		return this.index;
	}

	public void setIndex(int index){
		this.index=index ;
	}

	public boolean isOnDB(){
		return this.isOnDB;
	}

	public void setIsOnDB(boolean isOnDB){
		this.isOnDB=isOnDB;
	}

	public String getMonitorPointName(){
		return this.monitorPointName;
	}

	public void setMonitorPointName(String monitorPointName){
		this.monitorPointName = monitorPointName;
	}
}
