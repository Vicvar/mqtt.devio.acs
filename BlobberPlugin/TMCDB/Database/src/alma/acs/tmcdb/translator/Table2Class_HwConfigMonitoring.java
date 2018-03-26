package alma.acs.tmcdb.translator;

import java.util.ArrayList;
import java.util.HashMap;

public class Table2Class_HwConfigMonitoring extends AbstractTable2Class {

	public Table2Class_HwConfigMonitoring() {

		map = new HashMap<String, String>();
			map.put("hwconfiguration", PACKAGE + "HWConfiguration");
			map.put("systemcounters", PACKAGE + "SystemCounters");
			map.put("lrutype", PACKAGE + "LRUType");
			map.put("assemblytype", PACKAGE + "AssemblyType");
			map.put("hwschemas", PACKAGE + "HwSchemas");
			map.put("assembly", PACKAGE + "Assembly");
			map.put("assemblyrole", PACKAGE + "AssemblyRole");
			map.put("baseelement", PACKAGE + "BaseElement");
			map.put("acacorrset", PACKAGE + "AcaCorrSet");
			map.put("antenna", PACKAGE + "Antenna");
			map.put("acacorrdelays", PACKAGE + "AcaCorrDelays");
			map.put("pad", PACKAGE + "Pad");
			map.put("frontend", PACKAGE + "FrontEnd");
			map.put("photonicreference", PACKAGE + "PhotonicReference");
			map.put("weatherstationcontroller", PACKAGE + "WeatherStationController");
			map.put("centrallo", PACKAGE + "CentralLO");
			map.put("aostiming", PACKAGE + "AOSTiming");
			map.put("holographytower", PACKAGE + "HolographyTower");
			map.put("antennatopad", PACKAGE + "AntennaToPad");
			map.put("weatherstationtopad", PACKAGE + "WeatherStationToPad");
			map.put("holographytowertopad", PACKAGE + "HolographyTowerToPad");
			map.put("fedelay", PACKAGE + "FEDelay");
			map.put("ifdelay", PACKAGE + "IFDelay");
			map.put("lodelay", PACKAGE + "LODelay");
			map.put("xpdelay", PACKAGE + "XPDelay");
			map.put("corrquadrant", PACKAGE + "CorrQuadrant");
			map.put("corrquadrantrack", PACKAGE + "CorrQuadrantRack");
			map.put("corrstationbin", PACKAGE + "CorrStationBin");
			map.put("correlatorbin", PACKAGE + "CorrelatorBin");
			map.put("startup", PACKAGE + "Startup");
			map.put("baseelementstartup", PACKAGE + "BaseElementStartup");
			map.put("assemblystartup", PACKAGE + "AssemblyStartup");
			map.put("defaultcanaddress", PACKAGE + "DefaultCanAddress");
			map.put("pointingmodel", PACKAGE + "PointingModel");
			map.put("pointingmodelcoeff", PACKAGE + "PointingModelCoeff");
			map.put("pointingmodelcoeffoffset", PACKAGE + "PointingModelCoeffOffset");
			map.put("focusmodel", PACKAGE + "FocusModel");
			map.put("focusmodelcoeff", PACKAGE + "FocusModelCoeff");
			map.put("focusmodelcoeffoffset", PACKAGE + "FocusModelCoeffOffset");
			map.put("defaultcomponent", PACKAGE + "DefaultComponent");
			map.put("defaultbaciproperty", PACKAGE + "DefaultBaciProperty");
			map.put("defaultmonitorpoint", PACKAGE + "DefaultMonitorPoint");
			map.put("monitorpoint", PACKAGE + "MonitorPoint");
			map.put("monitordata", PACKAGE + "MonitorData");
			map.put("baseelementonline", PACKAGE + "BaseElementOnline");
			map.put("assemblyonline", PACKAGE + "AssemblyOnline");
			map.put("array", PACKAGE + "Array");
			map.put("antennatoarray", PACKAGE + "AntennaToArray");
			map.put("sbexecution", PACKAGE + "SBExecution");
			map.put("antennatofrontend", PACKAGE + "AntennaToFrontEnd");
			map.put("bl_versioninfo", PACKAGE + "BL_VersionInfo");
			map.put("bl_pointingmodelcoeff", PACKAGE + "BL_PointingModelCoeff");
			map.put("bl_pointingmodelcoeffoffset", PACKAGE + "BL_PointingModelCoeffOffset");
			map.put("bl_focusmodelcoeff", PACKAGE + "BL_FocusModelCoeff");
			map.put("bl_focusmodelcoeffoffset", PACKAGE + "BL_FocusModelCoeffOffset");
			map.put("bl_fedelay", PACKAGE + "BL_FEDelay");
			map.put("bl_ifdelay", PACKAGE + "BL_IFDelay");
			map.put("bl_lodelay", PACKAGE + "BL_LODelay");
			map.put("bl_xpdelay", PACKAGE + "BL_XPDelay");
			map.put("bl_antennadelay", PACKAGE + "BL_AntennaDelay");
			map.put("bl_antenna", PACKAGE + "BL_Antenna");
			map.put("bl_pad", PACKAGE + "BL_Pad");
			map.put("bl_antennatopad", PACKAGE + "BL_AntennaToPad");
			map.put("bl_acacorrdelays", PACKAGE + "BL_AcaCorrDelays");
			map.put("antennaefficiency", PACKAGE + "AntennaEfficiency");
			map.put("receiverquality", PACKAGE + "ReceiverQuality");
			map.put("receiverqualityparameters", PACKAGE + "ReceiverQualityParameters");
			map.put("holography", PACKAGE + "Holography");

		tablesWithGeneratedKeys = new ArrayList<String>();
			tablesWithGeneratedKeys.add("hwconfiguration");
			tablesWithGeneratedKeys.add("hwschemas");
			tablesWithGeneratedKeys.add("assembly");
			tablesWithGeneratedKeys.add("baseelement");
			tablesWithGeneratedKeys.add("antennatopad");
			tablesWithGeneratedKeys.add("holographytowertopad");
			tablesWithGeneratedKeys.add("fedelay");
			tablesWithGeneratedKeys.add("ifdelay");
			tablesWithGeneratedKeys.add("lodelay");
			tablesWithGeneratedKeys.add("xpdelay");
			tablesWithGeneratedKeys.add("startup");
			tablesWithGeneratedKeys.add("baseelementstartup");
			tablesWithGeneratedKeys.add("assemblystartup");
			tablesWithGeneratedKeys.add("pointingmodel");
			tablesWithGeneratedKeys.add("pointingmodelcoeff");
			tablesWithGeneratedKeys.add("focusmodel");
			tablesWithGeneratedKeys.add("focusmodelcoeff");
			tablesWithGeneratedKeys.add("monitorpoint");
			tablesWithGeneratedKeys.add("baseelementonline");
			tablesWithGeneratedKeys.add("assemblyonline");
			tablesWithGeneratedKeys.add("array");
			tablesWithGeneratedKeys.add("antennatofrontend");
			tablesWithGeneratedKeys.add("antennaefficiency");
			tablesWithGeneratedKeys.add("receiverquality");
			tablesWithGeneratedKeys.add("receiverqualityparameters");
			tablesWithGeneratedKeys.add("holography");

	}

}
