insert into XML_OBSPROJECT_ENTITIES (ARCHIVE_UID, TIMESTAMP, XML, SCHEMAUID, OWNER, DELETED, READPERMISSIONS, WRITEPERMISSIONS, HIDDEN, DIRTY, VIRTUAL) values ('uid://A002/Xf6399/X8', TIMESTAMP '2010-07-30 07:39:58', '<?xml version="1.0" encoding="UTF-8"?>
<ObsProject xmlns="Alma/ObsPrep/ObsProject" xmlns:prj="Alma/ObsPrep/ObsProject" xmlns:val="Alma/ValueTypes" schemaVersion="13" revision="1.96" status="Phase2Submitted" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ObsProject">
  <ObsProjectEntity entityId="uid://A002/Xf6399/X8" entityIdEncrypted="-- id encryption not yet implemented --" entityTypeName="ObsProject" schemaVersion="13"/>
  <prj:ObsProposalRef entityId="uid://A002/Xf6399/X9" entityTypeName="ObsProposal" documentVersion="1"/>
  <prj:ProjectStatusRef entityId="uid://A002/Xf6399/Xb" entityTypeName="ProjectStatus"/>
  <prj:projectName>Serpens_10x10_SDmap</prj:projectName>
  <prj:pI>wdent</prj:pI>
  <prj:version>0</prj:version>
  <prj:code>None Assigned</prj:code>
  <prj:timeOfCreation>2010-07-30 07:25:07</prj:timeOfCreation>
  <prj:manualMode>false</prj:manualMode>
  <prj:simulationMode>false</prj:simulationMode>
  <prj:isCommissioning>false</prj:isCommissioning>
  <prj:isCalibration>false</prj:isCalibration>
  <prj:letterGrade>D</prj:letterGrade>
  <prj:ObsProgram>
    <prj:ObsPlan status="Phase2Submitted" entityPartId="X11534833" almatype="APDM::ObsUnitSet">
      <prj:name>Observing Program</prj:name>
      <prj:ObsProjectRef entityId="uid://A002/Xf6399/X8" entityTypeName="ObsProject" documentVersion="1"/>
      <prj:runSciencePipeline>false</prj:runSciencePipeline>
      <prj:ObsUnitSet status="Phase2Submitted" entityPartId="X28213154" almatype="APDM::ObsUnitSet">
        <prj:name>ObsUnitSet</prj:name>
        <prj:note>10x10 arcmin map using SD script.</prj:note>
        <ObsUnitControl arrayRequested="TWELVE-M">
          <prj:maximumTime unit="s">0.0</prj:maximumTime>
          <prj:estimatedExecutionTime unit="s">0.0</prj:estimatedExecutionTime>
          <prj:CalibrationRequirements>
            <prj:pointingAccuracy unit="arcsec">0.0</prj:pointingAccuracy>
          </prj:CalibrationRequirements>
          <aggregatedExecutionCount>0</aggregatedExecutionCount>
        </ObsUnitControl>
        <prj:UnitDependencies>
          <prj:executionCount>1</prj:executionCount>
          <prj:delay unit="s">0.0</prj:delay>
          <prj:expression/>
        </prj:UnitDependencies>
        <prj:ObsProjectRef entityId="uid://A002/Xf6399/X8" entityTypeName="ObsProject" documentVersion="1"/>
        <prj:scienceProcessingScript/>
        <prj:runSciencePipeline>false</prj:runSciencePipeline>
        <prj:DataProcessingParameters projectType="Continuum">
          <prj:angularResolution unit="arcsec">0.0</prj:angularResolution>
          <prj:velocityResolution referenceSystem="lsrk" dopplerCalcType="OPTICAL">
            <centerVelocity unit="km/s">
      0.0
   </centerVelocity>
          </prj:velocityResolution>
          <prj:tBSensitivityGoal unit="K">0.0</prj:tBSensitivityGoal>
          <prj:rMSGoal unit="Jy">0.0</prj:rMSGoal>
        </prj:DataProcessingParameters>
        <prj:FlowControl>
          <prj:controlScript/>
        </prj:FlowControl>
        <prj:SchedBlockRef entityId="uid://A002/Xf6399/Xa" entityTypeName="SchedBlock" documentVersion="1"/>
        <prj:OUSStatusRef entityId="uid://A002/Xf6399/Xd" entityTypeName="OUSStatus"/>
      </prj:ObsUnitSet>
      <prj:OUSStatusRef entityId="uid://A002/Xf6399/Xc" entityTypeName="OUSStatus"/>
    </prj:ObsPlan>
  </prj:ObsProgram>
  <!--Converted to V11 by ObsProject10-11.xslt-->
  <!--Converted to V12 by ObsProject11-12.xslt-->
  <!--Converted to V13 by ObsProject12-13.xslt-->
</ObsProject>
', 'uid://A002/X14/X39', 'wdent', 0, null, null, 0, 0, 0);

insert into BMMV_OBSPROPOSAL_AUTHORS (ARCHIVE_UID, DELETED, CYCLE, PROJECT_ARCHIVE_UID, USERID, ORGANISATION, EXECUTIVE, AUTHTYPE, ID) 
values ('uid://A002/Xf6399/X9', 0, '2010.2', 'uid://A002/Xf6399/X8', 'wdent', null, 'OTHER', 'PI', 1175890);

insert into OBS_PROJECT_STATUS (STATUS_ENTITY_ID, DOMAIN_ENTITY_ID, DOMAIN_ENTITY_STATE, OBS_PROJECT_STATUS_ID, OBS_PROGRAM_STATUS_ID, OBS_PROJECT_ID, PROJECT_WAS_TIMED_OUT, XML) values ('uid://A002/Xf6399/Xb', 'uid://A002/Xf6399/X8', 'Ready', 'uid://A002/Xf6399/Xb', 'uid://A002/Xf6399/Xc', 'uid://A002/Xf6399/X8', null, '<?xml version="1.0" encoding="UTF-8"?>
<ProjectStatus xmlns="Alma/Scheduling/ProjectStatus" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" schemaVersion="10" revision="1.96" almatype="APDM::ProjectStatus" xsi:type="ProjectStatus">
  <Status State="Ready"/>
  <ProjectStatusEntity entityId="uid://A002/Xf6399/Xb" entityIdEncrypted="-- id encryption not yet implemented --" entityTypeName="ProjectStatus" schemaVersion="10"/>
  <ObsProjectRef entityId="uid://A002/Xf6399/X8" entityTypeName="ObsProject" documentVersion="1"/>
  <ObsProposalRef entityId="uid://A002/Xf6399/X9" entityTypeName="ObsProposal" documentVersion="1"/>
  <ObsProgramStatusRef entityId="uid://A002/Xf6399/Xc" entityTypeName="OUSStatus"/>
</ProjectStatus>
');

insert into BMMV_OBSPROPOSAL (ARCHIVE_UID, DELETED, ABSTRACT_TEXT, SCIENTIFIC_CATEGORY, PROPOSAL_TYPE, PI_USERID, ASSOCIATEDEXEC, DATERECEIVED, OBSPROJECT_ARCHIVE_UID, PROJECTUID, PI_FULLNAME, ORGANIZATION, EMAIL, CYCLE, KEYWORD1, KEYWORD2, KEYWORDCODE1, KEYWORDCODE2) 
values ('uid://A002/Xf6399/X9', 0, null, '10', 'N', 'wdent', 'OTHER', null, 'uid://A002/Xf6399/X8', 'uid://A002/Xf6399/X8', 'Bill Dent', null, 'wdent@alma.cl', '2010.2', null, null, null, null);

