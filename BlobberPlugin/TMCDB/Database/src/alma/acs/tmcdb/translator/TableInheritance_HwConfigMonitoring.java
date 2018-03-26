package alma.acs.tmcdb.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class TableInheritance_HwConfigMonitoring extends AbstractTableInheritance {

	public TableInheritance_HwConfigMonitoring() {

		Map<String, String> tmpMap;

		map = new HashMap<String, String>();

		tablesImplementingIdentifiable = new ArrayList<String>();

		tablesImplementingBackloggable = new ArrayList<String>();

		keymap = new HashMap<String, String>();

		List<String> keyColumns;
		keyColumnsMap = new HashMap<String, List<String>>();

		List<String> keyPieces;
		keyPiecesMap = new HashMap<String, List<String>>();
				keyPieces = new ArrayList<String>();
				keyPieces.add("swconfigurationid");
			keyPiecesMap.put("hwconfiguration", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("urn");
				keyPieces.add("configurationid");
			keyPiecesMap.put("hwschemas", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("serialnumber");
				keyPieces.add("configurationid");
			keyPiecesMap.put("assembly", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("baseelementname");
				keyPieces.add("basetype");
				keyPieces.add("configurationid");
			keyPiecesMap.put("baseelement", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("antennaid");
				keyPieces.add("padid");
				keyPieces.add("starttime");
			keyPiecesMap.put("antennatopad", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("holographytowerid");
				keyPieces.add("padid");
			keyPiecesMap.put("holographytowertopad", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("antennaid");
				keyPieces.add("receiverband");
				keyPieces.add("polarization");
				keyPieces.add("sideband");
			keyPiecesMap.put("fedelay", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("antennaid");
				keyPieces.add("baseband");
				keyPieces.add("polarization");
				keyPieces.add("ifswitch");
			keyPiecesMap.put("ifdelay", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("antennaid");
				keyPieces.add("baseband");
			keyPiecesMap.put("lodelay", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("configurationid");
				keyPieces.add("receiverband");
				keyPieces.add("sideband");
				keyPieces.add("baseband");
			keyPiecesMap.put("xpdelay", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("startupname");
				keyPieces.add("configurationid");
			keyPiecesMap.put("startup", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("startupid");
				keyPieces.add("baseelementid");
				keyPieces.add("parent");
				keyPieces.add("baseelementtype");
			keyPiecesMap.put("baseelementstartup", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("baseelementstartupid");
				keyPieces.add("rolename");
			keyPiecesMap.put("assemblystartup", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("antennaid");
			keyPiecesMap.put("pointingmodel", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("pointingmodelid");
				keyPieces.add("coeffname");
			keyPiecesMap.put("pointingmodelcoeff", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("antennaid");
			keyPiecesMap.put("focusmodel", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("focusmodelid");
				keyPieces.add("coeffname");
			keyPiecesMap.put("focusmodelcoeff", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("bacipropertyid");
				keyPieces.add("assemblyid");
				keyPieces.add("indice");
			keyPiecesMap.put("monitorpoint", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("baseelementid");
				keyPieces.add("configurationid");
				keyPieces.add("starttime");
			keyPiecesMap.put("baseelementonline", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("assemblyid");
				keyPieces.add("baseelementonlineid");
			keyPiecesMap.put("assemblyonline", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("starttime");
				keyPieces.add("baseelementid");
			keyPiecesMap.put("array", keyPieces);
				keyPieces = new ArrayList<String>();
				keyPieces.add("antennaid");
				keyPieces.add("frontendid");
				keyPieces.add("starttime");
			keyPiecesMap.put("antennatofrontend", keyPieces);

		cascadingTypes = new HashMap<String, CascadeType>();
			cascadingTypes.put("swconfigid", CascadeType.NONE);
			cascadingTypes.put("systemcountersconfig", CascadeType.NONE);
			cascadingTypes.put("assemblytypelruname", CascadeType.NONE);
			cascadingTypes.put("assemblytypecomptype", CascadeType.NONE);
			cascadingTypes.put("assemblyschemasconfig", CascadeType.NONE);
			cascadingTypes.put("hwschemaassemblytype", CascadeType.NONE);
			cascadingTypes.put("assemblyconfig", CascadeType.NONE);
			cascadingTypes.put("assemblyname", CascadeType.NONE);
			cascadingTypes.put("assemblyroleassembly", CascadeType.NONE);
			cascadingTypes.put("beconfig", CascadeType.NONE);
			cascadingTypes.put("acacsetbeid", CascadeType.NONE);
			cascadingTypes.put("antennabeid", CascadeType.NONE);
			cascadingTypes.put("acacdelantid", CascadeType.NONE);
			cascadingTypes.put("padbeid", CascadeType.NONE);
			cascadingTypes.put("frontendbeid", CascadeType.NONE);
			cascadingTypes.put("photrefbeid", CascadeType.NONE);
			cascadingTypes.put("weatherstationbeid", CascadeType.NONE);
			cascadingTypes.put("centrallobeid", CascadeType.NONE);
			cascadingTypes.put("aostimingbeid", CascadeType.NONE);
			cascadingTypes.put("holographytowerbeid", CascadeType.NONE);
			cascadingTypes.put("antennatopadantennaid", CascadeType.NONE);
			cascadingTypes.put("antennatopadpadid", CascadeType.NONE);
			cascadingTypes.put("wstopadweatherstationid", CascadeType.NONE);
			cascadingTypes.put("wstopadpadid", CascadeType.NONE);
			cascadingTypes.put("holotowertopadholotower", CascadeType.NONE);
			cascadingTypes.put("holotowertopadpad", CascadeType.NONE);
			cascadingTypes.put("antennafedelay", CascadeType.NONE);
			cascadingTypes.put("antennaifdelay", CascadeType.NONE);
			cascadingTypes.put("antennalodelay", CascadeType.NONE);
			cascadingTypes.put("hwconfigxpdelay", CascadeType.NONE);
			cascadingTypes.put("corrquadbeid", CascadeType.NONE);
			cascadingTypes.put("corrquadrackbeid", CascadeType.NONE);
			cascadingTypes.put("corrquad", CascadeType.NONE);
			cascadingTypes.put("corrstbinbeid", CascadeType.NONE);
			cascadingTypes.put("corrstbinrack", CascadeType.NONE);
			cascadingTypes.put("corrbinbeid", CascadeType.NONE);
			cascadingTypes.put("corrbinrack", CascadeType.NONE);
			cascadingTypes.put("startupconfig", CascadeType.NONE);
			cascadingTypes.put("bestartupid", CascadeType.NONE);
			cascadingTypes.put("bestartupidbe", CascadeType.NONE);
			cascadingTypes.put("bestartupparent", CascadeType.NONE);
			cascadingTypes.put("assemblystartuprole", CascadeType.NONE);
			cascadingTypes.put("assemblystartupbestartup", CascadeType.NONE);
			cascadingTypes.put("defcanaddcomp", CascadeType.NONE);
			cascadingTypes.put("antennapmantenna", CascadeType.NONE);
			cascadingTypes.put("antpmtermpointingmodelid", CascadeType.NONE);
			cascadingTypes.put("antpmcoeffofftocoeff", CascadeType.NONE);
			cascadingTypes.put("antennafmantenna", CascadeType.NONE);
			cascadingTypes.put("antfmtermfocusmodelid", CascadeType.NONE);
			cascadingTypes.put("antfmcoeffofftocoeff", CascadeType.NONE);
			cascadingTypes.put("defaultcomponenttypeid", CascadeType.NONE);
			cascadingTypes.put("defaultcomponentassemblyid", CascadeType.NONE);
			cascadingTypes.put("defbacidefaultcomponenttypeid", CascadeType.NONE);
			cascadingTypes.put("defaulpntid", CascadeType.NONE);
			cascadingTypes.put("monitorpointassemblyid", CascadeType.NONE);
			cascadingTypes.put("monitorpointbacipropertyid", CascadeType.NONE);
			cascadingTypes.put("monitordatamonitorpointid", CascadeType.NONE);
			cascadingTypes.put("beonlineid", CascadeType.NONE);
			cascadingTypes.put("beonlineconfig", CascadeType.NONE);
			cascadingTypes.put("beassemblylistid", CascadeType.NONE);
			cascadingTypes.put("beassemblylistassemblyid", CascadeType.NONE);
			cascadingTypes.put("arraybeid", CascadeType.NONE);
			cascadingTypes.put("antennatoarrayantennaid", CascadeType.NONE);
			cascadingTypes.put("antennatoarrayarrayid", CascadeType.NONE);
			cascadingTypes.put("sbexecutionarrayid", CascadeType.NONE);
			cascadingTypes.put("antennatofeantennaid", CascadeType.NONE);
			cascadingTypes.put("antennatofefrontendid", CascadeType.NONE);
			cascadingTypes.put("versioninfoswcnfid", CascadeType.NONE);
			cascadingTypes.put("antefftoantenna", CascadeType.NONE);
			cascadingTypes.put("recqualitytoantenna", CascadeType.NONE);
			cascadingTypes.put("recqualityparamtorecqual", CascadeType.NONE);
			cascadingTypes.put("holographytoantenna", CascadeType.NONE);
			cascadingTypes.put("holographyrefantenna", CascadeType.NONE);

		List<String> xmlClobColumns;
		xmlClobTableColumns = new HashMap<String, List<String>>();
				xmlClobColumns = new ArrayList<String>();
				xmlClobColumns.add("schema");
			xmlClobTableColumns.put("hwschemas", xmlClobColumns);
				xmlClobColumns = new ArrayList<String>();
				xmlClobColumns.add("data");
			xmlClobTableColumns.put("assembly", xmlClobColumns);
				xmlClobColumns = new ArrayList<String>();
				xmlClobColumns.add("xmldoc");
			xmlClobTableColumns.put("defaultcomponent", xmlClobColumns);

		sequences = new HashMap<String, String>();
			sequences.put("hwconfiguration", "HWConf_seq");
			sequences.put("hwschemas", "HwSchemas_seq");
			sequences.put("assembly", "Assembly_seq");
			sequences.put("baseelement", "BaseElement_seq");
			sequences.put("antennatopad", "AntennaToPad_seq");
			sequences.put("holographytowertopad", "HologrTTP_seq");
			sequences.put("fedelay", "FEDelay_seq");
			sequences.put("ifdelay", "IFDelay_seq");
			sequences.put("lodelay", "LODelay_seq");
			sequences.put("xpdelay", "XPDelay_seq");
			sequences.put("startup", "Startup_seq");
			sequences.put("baseelementstartup", "BaseElS_seq");
			sequences.put("assemblystartup", "AssembS_seq");
			sequences.put("pointingmodel", "PointiM_seq");
			sequences.put("pointingmodelcoeff", "PointiMC_seq");
			sequences.put("focusmodel", "FocusModel_seq");
			sequences.put("focusmodelcoeff", "FocusMC_seq");
			sequences.put("monitorpoint", "MonitorPoint_seq");
			sequences.put("baseelementonline", "BaseElO_seq");
			sequences.put("assemblyonline", "AssembO_seq");
			sequences.put("array", "Array_seq");
			sequences.put("antennatofrontend", "AntennTFE_seq");
			sequences.put("antennaefficiency", "AntennE_seq");
			sequences.put("receiverquality", "ReceivQ_seq");
			sequences.put("receiverqualityparameters", "ReceivQP_seq");
			sequences.put("holography", "Holography_seq");

		duplicatedForeignKeys = new HashMap<String, String>();

		checkConstraints = new HashMap<String, Map<String, String>>();

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("hwconfiguration", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("systemcounters", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("lrutype", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("baseelementtype", "alma.acs.tmcdb.AssemblyTypeBEType");
			checkConstraints.put("assemblytype", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("hwschemas", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("assembly", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("assemblyrole", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("basetype", "alma.acs.tmcdb.BEType");
			checkConstraints.put("baseelement", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("baseband", "alma.acs.tmcdb.AcaCSetBBEnum");
			checkConstraints.put("acacorrset", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("antennatype", "alma.acs.tmcdb.AntennaType");
			checkConstraints.put("antenna", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("acacorrdelays", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("pad", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("frontend", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("photonicreference", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("weatherstationcontroller", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("centrallo", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("aostiming", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("holographytower", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("antennatopad", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("weatherstationtopad", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("holographytowertopad", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("receiverband", "alma.acs.tmcdb.FEDelRecBandEnum");
		tmpMap.put("polarization", "alma.acs.tmcdb.FEDelPolEnum");
		tmpMap.put("sideband", "alma.acs.tmcdb.FEDelSideBandEnum");
			checkConstraints.put("fedelay", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("baseband", "alma.acs.tmcdb.IFDelBaseBandEnum");
		tmpMap.put("ifswitch", "alma.acs.tmcdb.IFDelIFSwitchEnum");
		tmpMap.put("polarization", "alma.acs.tmcdb.IFDelPolEnum");
			checkConstraints.put("ifdelay", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("baseband", "alma.acs.tmcdb.LODelBaseBandEnum");
			checkConstraints.put("lodelay", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("baseband", "alma.acs.tmcdb.XPDelBaseBandEnum");
		tmpMap.put("sideband", "alma.acs.tmcdb.XPDelSideBandEnum");
		tmpMap.put("receiverband", "alma.acs.tmcdb.XPDelFreqBandEnum");
			checkConstraints.put("xpdelay", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("baseband", "alma.acs.tmcdb.CorrQuadBBEnum");
			checkConstraints.put("corrquadrant", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("racktype", "alma.acs.tmcdb.CorrRackType");
			checkConstraints.put("corrquadrantrack", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("corrstationbin", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("correlatorbin", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("startup", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("baseelementtype", "alma.acs.tmcdb.BEStartupBEType");
			checkConstraints.put("baseelementstartup", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("assemblystartup", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("defaultcanaddress", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("pointingmodel", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("pointingmodelcoeff", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("receiverband", "alma.acs.tmcdb.AntennaPMCoeffOffBand");
			checkConstraints.put("pointingmodelcoeffoffset", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("focusmodel", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("focusmodelcoeff", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("receiverband", "alma.acs.tmcdb.AntennaFMCoeffOffBand");
			checkConstraints.put("focusmodelcoeffoffset", tmpMap);

			tmpMap = new HashMap<String,String>();
	tmpMap.put("impllang", "alma.acs.tmcdb.ImplLangEnum");
			checkConstraints.put("defaultcomponent", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("defaultbaciproperty", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("defaultmonitorpoint", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("datatype", "alma.acs.tmcdb.MonitorPointDatatype");
			checkConstraints.put("monitorpoint", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("monitordata", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("baseelementonline", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("assemblyonline", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("type", "alma.acs.tmcdb.ArrayType");
			checkConstraints.put("array", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("antennatoarray", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("sbexecution", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("antennatofrontend", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("bl_versioninfo", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("operation", "alma.acs.tmcdb.BL_PointingModelCoeffOp");
			checkConstraints.put("bl_pointingmodelcoeff", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("operation", "alma.acs.tmcdb.BL_AntennaPMCoeffOffOp");
		tmpMap.put("receiverband", "alma.acs.tmcdb.BL_AntennaPMCoeffOffBand");
			checkConstraints.put("bl_pointingmodelcoeffoffset", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("operation", "alma.acs.tmcdb.BL_FocusModelCoeffOp");
			checkConstraints.put("bl_focusmodelcoeff", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("operation", "alma.acs.tmcdb.BL_AntennaFMCoeffOffOp");
		tmpMap.put("receiverband", "alma.acs.tmcdb.BL_AntennaFMCoeffOffBand");
			checkConstraints.put("bl_focusmodelcoeffoffset", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("operation", "alma.acs.tmcdb.BL_FEDelayOp");
			checkConstraints.put("bl_fedelay", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("operation", "alma.acs.tmcdb.BL_IFDelayOp");
			checkConstraints.put("bl_ifdelay", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("operation", "alma.acs.tmcdb.BL_LODelayOp");
			checkConstraints.put("bl_lodelay", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("operation", "alma.acs.tmcdb.BL_XPDelayOp");
			checkConstraints.put("bl_xpdelay", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("bl_antennadelay", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("bl_antenna", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("bl_pad", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("bl_antennatopad", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("bl_acacorrdelays", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("antennaefficiency", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("receiverquality", tmpMap);

			tmpMap = new HashMap<String,String>();
			checkConstraints.put("receiverqualityparameters", tmpMap);

			tmpMap = new HashMap<String,String>();
		tmpMap.put("obsmode", "alma.acs.tmcdb.HolographyObsMode");
			checkConstraints.put("holography", tmpMap);

	}
}
