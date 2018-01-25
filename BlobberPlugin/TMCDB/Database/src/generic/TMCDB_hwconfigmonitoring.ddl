TMCDB SQL TABLE DEFINITIONS     VERSION 2.2.1  2010-08-22T0000:00:00.0
NOTE
"
==========================================
|  NON-SW-CONFIGURATION TABLES FOR TMCDB |
|   (mainly HW-CONFIG and MONITORING)    |
==========================================
" 
ENDNOTE

INCLUDE "classpath:/generic/TMCDB_swconfigcore.ddl"
INCLUDE "classpath:/generic/TMCDB_swconfigext.ddl"

MODELNAME HwConfigMonitoring

// Unless otherwise stated, all units are SI.

// The HWConfiguration is the root table for all the other hardware configuration
// tables. This allows to define different hw configurations in the TMCDB, which
// are independent of each other.
TABLE HWConfiguration
    ConfigurationId         INTEGER                 NOT NULL
    GlobalConfigId          INTEGER                 NULL
    SwConfigurationId       INTEGER                 NOT NULL
    TelescopeName           NAME                    NOT NULL
    ArrayReferenceX         DOUBLE                  NULL
    ArrayReferenceY         DOUBLE                  NULL
    ArrayReferenceZ         DOUBLE                  NULL
    XPDelayBLLocked           BOOLEAN                 NULL
    XPDelayBLIncreaseVersion  BOOLEAN                 NULL
    XPDelayBLCurrentVersion   INTEGER                 NULL
    XPDelayBLWho              NAME                    NULL
    XPDelayBLChangeDesc       TEXT                    NULL
    KEY ConfigurationId GENERATED FROM SwConfigurationId
    CONSTRAINT SwConfigId FOREIGN KEY (SwConfigurationId) REFERENCES Configuration
ENDTABLE

// The SystemCounters table records keeps track of counters that are
// used by Control to generate names of arrays and Data Capture
// components.
TABLE SystemCounters
     ConfigurationId         INTEGER                     NOT NULL
     UpdateTime              TIME                    NOT NULL
     AutoArrayCount          SMALLINT                NOT NULL
     ManArrayCount           SMALLINT                NOT NULL
     DataCaptureCount        SMALLINT                NOT NULL
     KEY ConfigurationId
     CONSTRAINT SystemCountersConfig FOREIGN KEY (ConfigurationId) REFERENCES HWConfiguration
ENDTABLE

// ============================================================================
// Assembly/BaseElement tables

// LRUType represents the types of Line Replaceable Units (LRU).
// These are hardware units that are taken out of field, carried back
// to the lab, repaired or replaced and brought back to the field.
TABLE LRUType
     LRUName                 NAME                    NOT NULL
     FullName                LONGNAME                NOT NULL
     ICD                     LONGNAME                NOT NULL
     ICDDate                 TIME                    NOT NULL
     Description             TEXT                    NOT NULL
     Notes                   TEXT                    NULL
     KEY LRUName
ENDTABLE

// AssemblyType represents assemblies that are part of an LRU.  All
// LRUs are made up of one or more assemblies.  All monitored
// properties are tied to specific assemblies.
TABLE AssemblyType
    AssemblyTypeName        LONGNAME                NOT NULL
    // Used by the TmcdbExplorer to classify the Assemblies.
    BaseElementType         LONGVARCHAR (24)        NOT NULL
    LRUName                 NAME                    NOT NULL
    FullName                LONGNAME                NOT NULL
    Description             TEXT                    NOT NULL
    Notes                   TEXT                    NULL
    ComponentTypeId         INTEGER                 NOT NULL
    // There are normally two implementations of a Control hardware
    // device: the production implementation, which interacts with
    // the hardware, and a simulation implementation, to be used for
    // testing. 
    // The following two columns are used by the TmcdbExplorer to
    // change the Code column in the Component table depending on
    // the user selecting to start a simulated device in the
    // AssemblyStartup table. 
    ProductionCode          LONGNAME                NOT NULL
    SimulatedCode           LONGNAME                NOT NULL
    KEY AssemblyTypeName
    CONSTRAINT AssemblyTypeLRUName FOREIGN KEY (LRUName) REFERENCES LRUType
    CONSTRAINT AssemblyTypeCompType FOREIGN KEY (ComponentTypeId) REFERENCES ComponentType
    CONSTRAINT AssemblyTypeBEType CHECK (BaseElementType IN ('Antenna',
        'Pad', 'FrontEnd', 'WeatherStationController', 'CorrQuadrant', 'AcaCorrSet',
        'CentralLO', 'AOSTiming', 'PhotonicReference', 'HolographyTower', 'Array'))
ENDTABLE

TABLE HwSchemas
    SchemaId                INTEGER                 NOT NULL
    URN                     LONGVARCHAR (512)       NOT NULL
    ConfigurationId         INTEGER                 NOT NULL
    AssemblyTypeName        LONGNAME                NOT NULL
    Schema                  XMLCLOB                 NULL
    KEY SchemaId GENERATED FROM URN ConfigurationId
    CONSTRAINT AssemblySchemasConfig FOREIGN KEY (ConfigurationId) REFERENCES HWConfiguration
    CONSTRAINT HwSchemaAssemblyType FOREIGN KEY (AssemblyTypeName) REFERENCES AssemblyType
ENDTABLE

// An Assembly is an instance of an assembly type.  All monitored
// property data are tied to an instance of an assembly.
TABLE Assembly
     AssemblyId              INTEGER                 NOT NULL
     AssemblyTypeName        LONGNAME                NOT NULL
     ConfigurationId         INTEGER                 NOT NULL
     SerialNumber            LONGNAME                NOT NULL
     Data                    XMLCLOB                 NULL
     KEY AssemblyId GENERATED FROM SerialNumber ConfigurationId
     CONSTRAINT AssemblyConfig FOREIGN KEY (ConfigurationId) REFERENCES HWConfiguration
     CONSTRAINT AssemblyName FOREIGN KEY (AssemblyTypeName) REFERENCES AssemblyType
ENDTABLE

// Role played by an AssemblyType in the system. Some types of
// assemblies are installed multiple times in the same
// BaseElement. For example, four SecondLOs are installed in an
// antenna. An assembly role can bee regarded as representing
// a specific place where an assembly can be installed in a
// telescope equipment.
TABLE AssemblyRole
     RoleName                NAME                    NOT NULL
     AssemblyTypeName        LONGNAME                NOT NULL
     KEY RoleName
     CONSTRAINT AssemblyRoleAssembly FOREIGN KEY (AssemblyTypeName) REFERENCES AssemblyType
ENDTABLE

// A BaseElement represents a piece of equipment that contains hardware
// assemblies and/or other equipment recursively. Antennas, FrondEnds, and even
// Pads (although they don't contain assemblies) for example, are all modeled
// as BaseElements.
// The relationship between specific BaseElements and this BaseElement table
// is of inheritance. The Antenna, for example, is a child or type of BaseElement.
TABLE BaseElement
     BaseElementId           INTEGER                     NOT NULL
     BaseType                LONGVARCHAR (24)            NOT NULL
     BaseElementName         LONGVARCHAR (24)            NOT NULL
     ConfigurationId         INTEGER                     NOT NULL
     KEY BaseElementId GENERATED FROM BaseElementName BaseType ConfigurationId
     CONSTRAINT BEConfig FOREIGN KEY (ConfigurationId) REFERENCES HWConfiguration
     CONSTRAINT BEType CHECK (BaseType IN ('Antenna', 'Pad', 
         'FrontEnd', 'WeatherStationController', 'CentralLO', 'AOSTiming',
         'HolographyTower', 'PhotonicReference', 'CorrQuadrant', 'AcaCorrSet',
         'CorrQuadrantRack', 'CorrStationBin', 'CorrBin'))
ENDTABLE

TABLE AcaCorrSet
     BaseElementId           INTEGER                 NOT NULL
     BaseBand                NAME                    NOT NULL
     IP                      NAME                    NOT NULL
     KEY BaseElementId
     CONSTRAINT AcaCSetBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
     CONSTRAINT AcaCSetBBEnum CHECK (BaseBand IN ('BB_1', 'BB_2', 'BB_3', 'BB_4'))
ENDTABLE

// The Antenna table represents the general properties of an ALMA
// antenna.  The x-y-z position is the position from the pad position
// to the point of rotation of the antenna.  The x-y-z offset is the
// offset, if any, from that position to the point from which the
// feeds offsets are measured.  Included is the name of the software
// component that executes the antenna.
TABLE Antenna
     BaseElementId             INTEGER                 NOT NULL
     AntennaName               NAME                    NULL
     AntennaType               LONGVARCHAR (4)         NOT NULL
     DishDiameter              LENGTH                  NOT NULL
     CommissionDate            TIME                    NOT NULL
     XPosition                 LENGTH                  NOT NULL
     YPosition                 LENGTH                  NOT NULL
     ZPosition                 LENGTH                  NOT NULL
     XPositionErr              LENGTH                  NULL
     YPositionErr              LENGTH                  NULL
     ZPositionErr              LENGTH                  NULL
     XOffset                   LENGTH                  NOT NULL
     YOffset                   LENGTH                  NOT NULL
     ZOffset                   LENGTH                  NOT NULL
     PosObservationTime        TIME                    NULL
     PosExecBlockUID           VARCHAR(100)            NULL
     PosScanNumber             INTEGER                 NULL
     Comments                  TEXT                    NULL
     Delay                     DOUBLE                  NOT NULL
     DelayError                DOUBLE                  NULL
     DelObservationTime        TIME                    NULL
     DelExecBlockUID           VARCHAR(100)            NULL
     DelScanNumber             INTEGER                 NULL
     XDelayRef                 DOUBLE                  NULL
     YDelayRef                 DOUBLE                  NULL
     ZDelayRef                 DOUBLE                  NULL
     LOOffsettingIndex         INTEGER                 NOT NULL
     WalshSeq                  INTEGER                 NOT NULL     
     CaiBaseline               INTEGER                 NULL     
     CaiAca                    INTEGER                 NULL     
     Locked                    BOOLEAN                 NULL
     IncreaseVersion           BOOLEAN                 NULL
     CurrentVersion            INTEGER                 NULL
     Who                       NAME                    NULL
     ChangeDesc                TEXT                    NULL
     DelayBLLocked             BOOLEAN                 NULL
     DelayBLIncreaseVersion    BOOLEAN                 NULL
     DelayBLCurrentVersion     INTEGER                 NULL
     DelayBLWho                NAME                    NULL
     DelayBLChangeDesc         TEXT                    NULL
     KEY BaseElementId
     CONSTRAINT AntennaBEId  FOREIGN KEY (BaseElementId) REFERENCES BaseElement
     CONSTRAINT AntennaType  CHECK (AntennaType IN ('VA', 'AEC', 'ACA'))
ENDTABLE

TABLE AcaCorrDelays
     AntennaId               INTEGER                 NOT NULL
     BbOneDelay              DOUBLE                  NOT NULL
     BbTwoDelay              DOUBLE                  NOT NULL
     BbThreeDelay            DOUBLE                  NOT NULL
     BbFourDelay             DOUBLE                  NOT NULL
     Locked                  BOOLEAN                     NULL
     IncreaseVersion         BOOLEAN                     NULL
     CurrentVersion          INTEGER                     NULL
     Who                     NAME                        NULL
     ChangeDesc              TEXT                        NULL
     KEY AntennaId 
     CONSTRAINT AcaCDelAntId  FOREIGN KEY (AntennaId) REFERENCES Antenna
ENDTABLE

// The most important thing about pads is their location.  Locations
// are in meters.
TABLE Pad
     BaseElementId           INTEGER                 NOT NULL
     PadName                 NAME                    NULL
     CommissionDate          TIME                    NOT NULL
     XPosition               LENGTH                  NOT NULL
     YPosition               LENGTH                  NOT NULL
     ZPosition               LENGTH                  NOT NULL
     XPositionErr            LENGTH                  NULL
     YPositionErr            LENGTH                  NULL
     ZPositionErr            LENGTH                  NULL
     PosObservationTime      TIME                    NULL
     PosExecBlockUID         VARCHAR(100)            NULL
     PosScanNumber           INTEGER                 NULL
     Delay                   DOUBLE                  NOT NULL
     DelayError              DOUBLE                  NULL
     DelObservationTime      TIME                    NULL
     DelExecBlockUID         VARCHAR(100)            NULL
     DelScanNumber           INTEGER                 NULL
     Locked                  BOOLEAN                 NULL
     IncreaseVersion         BOOLEAN                 NULL
     CurrentVersion          INTEGER                 NULL
     Who                     NAME                    NULL
     ChangeDesc              TEXT                    NULL
     KEY BaseElementId
     CONSTRAINT PadBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
ENDTABLE

// The front end is a base element because it can be moved from one
// antenna to another.  Included is the name of the software component
// that executes the front end.
TABLE FrontEnd
     BaseElementId           INTEGER                 NOT NULL
     CommissionDate          TIME                    NOT NULL
     KEY BaseElementId
     CONSTRAINT FrontEndBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
ENDTABLE

// The Photonic References integrates a Laser Synthesizer and
// a Central Variable Reference. Together these components generates
// a local oscillator signal, to be used to tune the frequencies used
// for downconversion in the antennas assigned to an array.
// There will be 6 Photonic References in the ALMA telescope.
TABLE PhotonicReference
     BaseElementId           INTEGER                 NOT NULL
     CommissionDate          TIME                    NOT NULL
     KEY BaseElementId
     CONSTRAINT PhotRefBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
ENDTABLE

// It is assumed that weather stations are stationary.  Included is
// the name of the software component that executes the weather
// station.
TABLE WeatherStationController
     BaseElementId           INTEGER                 NOT NULL
     CommissionDate          TIME                    NOT NULL
     KEY BaseElementId
     CONSTRAINT WeatherStationBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
ENDTABLE

TABLE CentralLO
     BaseElementId           INTEGER                 NOT NULL
     CommissionDate          TIME                    NOT NULL
     KEY BaseElementId
     CONSTRAINT CentralLOBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
ENDTABLE

TABLE AOSTiming
     BaseElementId           INTEGER                     NOT NULL
     CommissionDate          TIME                    NOT NULL
     KEY BaseElementId
     CONSTRAINT AOSTimingBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
ENDTABLE

// The most interesting about the holography tower is its location.
TABLE HolographyTower
     BaseElementId            INTEGER                    NOT NULL
     CommissionDate           TIME                   NOT NULL
     XPosition                LENGTH                 NOT NULL
     YPosition                LENGTH                 NOT NULL
     ZPosition                LENGTH                 NOT NULL
     KEY BaseElementId
     CONSTRAINT HolographyTowerBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
ENDTABLE

// The AntennaToPad table gives the pad that an antenna is on at the
// indicated time.  If the Planned flag is 'y', then it indicates this
// is a planned move and not an actual one.  Planned entries are
// ignored when determining what is or was actually online.
TABLE AntennaToPad
     AntennaToPadId          INTEGER                    NOT NULL
     AntennaId               INTEGER                     NOT NULL
     PadId                   INTEGER                     NOT NULL
     StartTime               TIME                        NOT NULL
     EndTime                 TIME                        NULL
     Planned                 BOOLEAN                     NOT NULL
     MountMetrologyAN0Coeff  DOUBLE                      NULL
     MountMetrologyAW0Coeff  DOUBLE                      NULL
     Locked                  BOOLEAN                     NULL
     IncreaseVersion         BOOLEAN                     NULL
     CurrentVersion          INTEGER                     NULL
     Who                     NAME                        NULL
     ChangeDesc              TEXT                        NULL
     KEY AntennaToPadId GENERATED FROM AntennaId PadId StartTime
     CONSTRAINT AntennaToPadAntennaId FOREIGN KEY (AntennaId) REFERENCES Antenna
     CONSTRAINT AntennaToPadPadId FOREIGN KEY (PadId) REFERENCES Pad
ENDTABLE

// The WeatherStationToPad table gives the pad that a weather station
// is on at the indicated time.  If the Planned flag is 'y', then it
// indicates this is a planned weather station and not an actual one.
// Planned entries are ignored when determining what is or was
// actually online.
TABLE WeatherStationToPad
     WeatherStationId        INTEGER                     NOT NULL
     PadId                   INTEGER                     NOT NULL
     StartTime               TIME                    NOT NULL
     EndTime                 TIME                    NULL
     Planned                 BOOLEAN                 NOT NULL
     KEY WeatherStationId PadId StartTime
     CONSTRAINT WSToPadWeatherStationId FOREIGN KEY (WeatherStationId) REFERENCES WeatherStationController
     CONSTRAINT WSToPadPadId FOREIGN KEY (PadId) REFERENCES Pad
ENDTABLE

TABLE HolographyTowerToPad
     TowerToPadId            INTEGER                 NOT NULL
     HolographyTowerId       INTEGER                 NOT NULL
     PadId                   INTEGER                 NOT NULL
     Azimuth                 DOUBLE                  NOT NULL
     Elevation               DOUBLE                  NOT NULL
     KEY TowerToPadId GENERATED FROM HolographyTowerId PadId
     CONSTRAINT HoloTowerToPadHoloTower FOREIGN KEY (HolographyTowerId) REFERENCES HolographyTower
     CONSTRAINT HoloTowerToPadPad FOREIGN KEY (PadId) REFERENCES Pad
ENDTABLE

// ============================================================================
// Delay tables.
//
// The total delay introduced by the different pieces of intrumentation
// located in the path that starts in the antenna receivers and goes all
// the way to the to the correlator is divided in the following way:
//
//   Ttotal = Tpad + Tant + Tfe + Tif + Tlo + Txp
//
// where:
//
//   Tpad: Delay contribution of the pad. This is the Delay column in the Pad
//         table.
//   Tant: Delay contribution of the antenna. This is the Delay column in the
//         Antenna table.
//   Tfe:  Delay contribution of the front end instrumentation. For each
//         front end, there are multiple delays, depending on the parameters
//         that define different paths. These parameters are frequency band,
//         polarization, and sideband. These delays could be associated with
//         each front end, however as the TMCDB doesn't track which front end
//         is installed in each antenna, these delays are associated directly
//         with the antenna.
//   Tif:  Delay contribution of the intermediate frequency (IF) processor.
//         The different delay paths are defined by the baseband, polarization
//         and switch (the first switch in the IFProcs), for each antenna.
//   Tlo:  Delay contribution of the local oscillators. For each antenna, there
//         are different delay values for each baseband. In this way, there
//         should be 4 LO delays for each antenna. A fifth delay could be
//         introduced in the future, to account for contributions of the first
//         LO.
//   Txp:  Cross polarization delay contributions. These delays are independent
//         of the antennas, depending on the frequency band, sideband and
//         baseband.
//
// This design is based on ALMA-80.00.00.00-0015-A-SPE, "Instrumental Delay", by R. Sramek.
// (See JIRA ticket SE-21, for discussions.)
//

// Delays introduced by the front end instrumentation.
TABLE FEDelay
     FEDelayId                INTEGER                NOT NULL
     AntennaId                INTEGER                NOT NULL
     ReceiverBand             NAME                   NOT NULL
     Polarization             NAME                   NOT NULL
     SideBand                 NAME                   NOT NULL
     Delay                    DOUBLE                 NOT NULL
     DelayError               DOUBLE                 NULL
     ObservationTime          TIME                   NULL
     ExecBlockUID             VARCHAR(100)           NULL
     ScanNumber               INTEGER                NULL
     KEY FEDelayId GENERATED FROM AntennaId ReceiverBand Polarization SideBand
     CONSTRAINT AntennaFEDelay FOREIGN KEY (AntennaId) REFERENCES Antenna
     CONSTRAINT FEDelRecBandEnum CHECK (ReceiverBand IN ('ALMA_RB_01', 'ALMA_RB_02', 'ALMA_RB_03',
         'ALMA_RB_04', 'ALMA_RB_05', 'ALMA_RB_06', 'ALMA_RB_07', 'ALMA_RB_08', 'ALMA_RB_09', 'ALMA_RB_10'))
     CONSTRAINT FEDelPolEnum CHECK (Polarization IN ('X', 'Y'))
     CONSTRAINT FEDelSideBandEnum CHECK (SideBand IN ('LSB', 'USB'))
ENDTABLE

// Delays introduced by the instrumentation that performs the down
// conversion to intermediate frequency (IF).
TABLE IFDelay
     IFDelayId                INTEGER                NOT NULL
     AntennaId                INTEGER                NOT NULL
     BaseBand                 NAME                   NOT NULL
     Polarization             NAME                   NOT NULL
     IFSwitch                 NAME                   NOT NULL
     Delay                    DOUBLE                 NOT NULL
     DelayError               DOUBLE                 NULL
     ObservationTime          TIME                   NULL
     ExecBlockUID             VARCHAR(100)           NULL
     ScanNumber               INTEGER                NULL
     KEY IFDelayId GENERATED FROM AntennaId BaseBand Polarization IFSwitch
     CONSTRAINT AntennaIFDelay FOREIGN KEY (AntennaId) REFERENCES Antenna
     CONSTRAINT IFDelBaseBandEnum CHECK (BaseBand IN ('BB_1', 'BB_2', 'BB_3', 'BB_4'))
     CONSTRAINT IFDelIFSwitchEnum CHECK (IFSwitch IN ('USB_HIGH', 'USB_LOW', 'LSB_HIGH', 'LSB_LOW'))
     CONSTRAINT IFDelPolEnum CHECK (Polarization IN ('X', 'Y'))
ENDTABLE

// Delays introduced by the local oscilators.
TABLE LODelay
     LODelayId                INTEGER                NOT NULL
     AntennaId                INTEGER                NOT NULL
     BaseBand                 NAME                   NOT NULL
     Delay                    DOUBLE                 NOT NULL
     DelayError               DOUBLE                 NULL
     ObservationTime          TIME                   NULL
     ExecBlockUID             VARCHAR(100)           NULL
     ScanNumber               INTEGER                NULL
     KEY LODelayId GENERATED FROM AntennaId BaseBand
     CONSTRAINT AntennaLODelay FOREIGN KEY (AntennaId) REFERENCES Antenna
     CONSTRAINT LODelBaseBandEnum CHECK (BaseBand IN ('BB_1', 'BB_2', 'BB_3', 'BB_4'))
ENDTABLE

// Cross polarization delays.
TABLE XPDelay
     XPDelayId                INTEGER                NOT NULL
     ConfigurationId          INTEGER                NOT NULL
     ReceiverBand            NAME                   NOT NULL
     SideBand                 NAME                   NOT NULL
     BaseBand                 NAME                   NOT NULL
     Delay                    DOUBLE                 NOT NULL
     DelayError               DOUBLE                 NULL
     ObservationTime          TIME                   NULL
     ExecBlockUID             VARCHAR(100)           NULL
     ScanNumber               INTEGER                NULL
     KEY XPDelayId GENERATED FROM ConfigurationId ReceiverBand SideBand BaseBand  
     CONSTRAINT HWConfigXPDelay FOREIGN KEY (ConfigurationId) REFERENCES HWConfiguration
     CONSTRAINT XPDelBaseBandEnum CHECK (BaseBand IN ('BB_1', 'BB_2', 'BB_3', 'BB_4'))
     CONSTRAINT XPDelSideBandEnum CHECK (SideBand IN ('LSB', 'USB'))
     CONSTRAINT XPDelFreqBandEnum CHECK (ReceiverBand IN ('ALMA_RB_01', 'ALMA_RB_02', 'ALMA_RB_03',
         'ALMA_RB_04', 'ALMA_RB_05', 'ALMA_RB_06', 'ALMA_RB_07', 'ALMA_RB_08', 'ALMA_RB_09', 'ALMA_RB_10'))
ENDTABLE

// ============================================================================
// Correlator tables.

// Correlator quadrant belongs here rather than Correlator because
// each quadrant can separately be on-line or off-line.  A correlator
// quadrant is composed of racks.  In addition, each correlator
// quadrant has a number, a CAN channel, number of antennas, and bins
// associated with it.  Quadrants may be turned on/off independetly
// within a HW configuration (e.g. during maintenance, testing, etc.).
// The ON/OFF status of a given quadrant within a given correlator
// hardware configuration is indicated by the Active field.
TABLE CorrQuadrant
     BaseElementId           INTEGER                 NOT NULL
     BaseBand                NAME                    NOT NULL
     Quadrant                TINYINT                 NOT NULL
     ChannelNumber           TINYINT                 NOT NULL
     KEY BaseElementId
     CONSTRAINT CorrQuadBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
     CONSTRAINT CorrQuadNumber CHECK (Quadrant IN (0, 1, 2, 3))
     CONSTRAINT CorrQuadBBEnum CHECK (BaseBand IN ('BB_1', 'BB_2', 'BB_3', 'BB_4'))
ENDTABLE

// CorrQuadrantRack gives the racks that belong to a correlator
// quadrant.  There are two types of racks, those that contain station
// cards and those that contain correlator cards.
TABLE CorrQuadrantRack
     BaseElementId           INTEGER              NOT NULL
     CorrQuadrantId          INTEGER              NOT NULL
     RackName                NAME                 NOT NULL
     RackType                LONGVARCHAR (10)     NOT NULL
     KEY BaseElementId
     CONSTRAINT CorrQuadRackBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
     CONSTRAINT CorrRackType CHECK (RackType IN ('Station', 'Correlator'))
     CONSTRAINT CorrQuad FOREIGN KEY (CorrQuadrantId) REFERENCES CorrQuadrant
ENDTABLE

// CorrStationBin gives the station bins that belong to a correlator
// station rack. A station bin must contain at least a single station
// control card with a CAN node address.  Additionally, there are 5
// types of other cards (not in the CAN bus) with monitor points
// populating a station bin: station cards, TFB cards, DRX cards,
// station interface cards, and power supply cards.
TABLE CorrStationBin
     BaseElementId          INTEGER                  NOT NULL
     CorrQuadrantRackId     INTEGER                  NOT NULL
     StationBinName         NAME                     NOT NULL
     KEY BaseElementId
     CONSTRAINT CorrStBinBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
     CONSTRAINT CorrStBinRack FOREIGN KEY (CorrQuadrantRackId) REFERENCES CorrQuadrantRack
ENDTABLE

// CorrBin gives the bins that belong to a correlator rack.  A
// correlator bin must contain at least a single LTA with a CAN node
// address.  Additionally, there are 3 types of other cards (not in
// the CAN bus) with monitor points populating a correlator bin:
// correlator cards, correlator interface cards, and power supply
// cards
TABLE CorrelatorBin
     BaseElementId          INTEGER                  NOT NULL
     CorrQuadrantRackId     INTEGER                  NOT NULL
     CorrelatorBinName      NAME                     NOT NULL
     KEY BaseElementId
     CONSTRAINT CorrBinBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
     CONSTRAINT CorrBinRack FOREIGN KEY (CorrQuadrantRackId) REFERENCES CorrQuadrantRack
ENDTABLE

// ===========================================================================
// Startup tables.
//
// The following tables define which BaseElements and Assemblies should be
// started.

// The Startup table designates a startup configuration.
TABLE Startup
     StartupId               INTEGER                     NOT NULL
     ConfigurationId         INTEGER                     NOT NULL
     StartupName             LONGNAME                NOT NULL
     KEY StartupId GENERATED FROM StartupName ConfigurationId
     CONSTRAINT StartupConfig FOREIGN KEY (ConfigurationId) REFERENCES HWConfiguration
ENDTABLE

// The BaseElementStartup table specifies which base elements are to
// be started.
TABLE BaseElementStartup
      BaseElementStartupId    INTEGER                     NOT NULL
      BaseElementId           INTEGER                     NULL
      StartupId               INTEGER                     NULL
      BaseElementType         VARCHAR (24)            NOT NULL
      Parent                  INTEGER                     NULL
      IsGeneric               VARCHAR (5)             NOT NULL
      Simulated               BOOLEAN                 NOT NULL
      KEY BaseElementStartupId GENERATED FROM StartupId BaseElementId Parent BaseElementType
      CONSTRAINT BEStartupId FOREIGN KEY (StartupId) REFERENCES Startup
      CONSTRAINT BEStartupIdBE FOREIGN KEY (BaseElementId) REFERENCES BaseElement
      CONSTRAINT BEStartupParent FOREIGN KEY (Parent) REFERENCES BaseElementStartup
      CONSTRAINT BEStartupBEType CHECK (BaseElementType IN ('Antenna', 'Pad',
          'FrontEnd', 'WeatherStationController', 'CentralLO',
          'AOSTiming', 'HolographyTower', 'Array', 'PhotonicReference1',
          'PhotonicReference2', 'PhotonicReference3', 'PhotonicReference4',
          'PhotonicReference5', 'PhotonicReference6'))
ENDTABLE

// The AssemblyStartup table specifies which assemblies are to be
// started within a BaseElement. The specific component name is then
// deduced by the CONTROL subsystem by looking at the hierarchy of
// BaseElements and AssemblyRoles of this assembly startup
TABLE AssemblyStartup
      AssemblyStartupId       INTEGER                     NOT NULL
      RoleName                NAME                    NOT NULL
      BaseElementStartupId    INTEGER                     NOT NULL
      Simulated               BOOLEAN                 NOT NULL
      KEY AssemblyStartupId GENERATED FROM BaseElementStartupId RoleName
      CONSTRAINT AssemblyStartupRole FOREIGN KEY (RoleName) REFERENCES AssemblyRole
      CONSTRAINT AssemblyStartupBEStartup FOREIGN KEY (BaseElementStartupId) REFERENCES BaseElementStartup
ENDTABLE

// CONTROL gets the connection parameters for its devices (either CAN address,
// or the Ethernet parameters) directly from DAL. This is accomplished by
// means of a DAL plugin.
// The plugin populates this table from the CDB if the LOAD_FROM_XML option is
// set; and uses this table to form the alma/CONTROL/<component...>/Address nodes.
// Now that this table also supports Ethernet devices, it should be renamed.
TABLE DefaultCanAddress
     ComponentId             INTEGER                 NOT NULL
     IsEthernet              BOOLEAN                 NOT NULL
     NodeAddress             VARCHAR (16)            NULL
     ChannelNumber           TINYINT                 NULL
     Hostname                VARCHAR (80)            NULL
     Port                    INTEGER                 NULL
     MacAddress              VARCHAR (80)            NULL
     Retries                 SMALLINT                NULL
     TimeOutRxTx             DOUBLE                  NULL
     LingerTime              INTEGER                 NULL
     KEY ComponentId
     CONSTRAINT DefCanAddComp FOREIGN KEY (ComponentId) REFERENCES Component
ENDTABLE

// ============================================================================
// Focus/Pointing/Delay Correction models.
// Tables in this section define several coefficients to account for antenna
// deformations, temperature variations, different antenna types, etc. These
// coefficients specify corrections in the calculation of the instrumental
// delays, in the focus setting and in the pointing operation.
//
// This design is based on "Control of Antenna Focus - Updated Summary", by
// R. Hills, and subsequent email communications.

TABLE PointingModel
      PointingModelId         INTEGER                 NOT NULL
      AntennaId               INTEGER                 NOT NULL
      ObservationTime         TIME                    NULL
      ExecBlockUID            VARCHAR(100)            NULL
      ScanNumber              INTEGER                 NULL
      SoftwareVersion         VARCHAR(100)            NULL
      Comments                TEXT                    NULL
      SourceNumber            INTEGER                 NULL
      MetrologyMode           VARCHAR(100)            NULL
      MetrologyFlag           VARCHAR(100)            NULL
      SourceDensity           DOUBLE                  NULL
      PointingRMS             DOUBLE                  NULL
      Locked                  BOOLEAN                 NULL
      IncreaseVersion         BOOLEAN                 NULL
      CurrentVersion          INTEGER                 NULL
      Who                     NAME                    NULL
      ChangeDesc              TEXT                    NULL
      KEY PointingModelId GENERATED FROM AntennaId
      CONSTRAINT AntennaPMAntenna FOREIGN KEY (AntennaId) REFERENCES Antenna
ENDTABLE

TABLE PointingModelCoeff
     PointingModelCoeffId    INTEGER                 NOT NULL
     PointingModelId         INTEGER                 NOT NULL
     CoeffName               NAME                    NOT NULL
     CoeffValue              DOUBLE                  NOT NULL
     KEY PointingModelCoeffId GENERATED FROM PointingModelId CoeffName
     CONSTRAINT AntPMTermPointingModelId FOREIGN KEY (PointingModelId) REFERENCES PointingModel
ENDTABLE

TABLE PointingModelCoeffOffset
     PointingModelCoeffId    INTEGER                 NOT NULL
     ReceiverBand            NAME                    NOT NULL
     Offset                  DOUBLE                  NOT NULL
     KEY PointingModelCoeffId ReceiverBand
     CONSTRAINT AntPMCoeffOffToCoeff FOREIGN KEY (PointingModelCoeffId) REFERENCES PointingModelCoeff
     CONSTRAINT AntennaPMCoeffOffBand CHECK (ReceiverBand IN ('ALMA_RB_01', 'ALMA_RB_02', 'ALMA_RB_03',
         'ALMA_RB_04', 'ALMA_RB_05', 'ALMA_RB_06', 'ALMA_RB_07', 'ALMA_RB_08', 'ALMA_RB_09', 'ALMA_RB_10'))
ENDTABLE

TABLE FocusModel
      FocusModelId            INTEGER                 NOT NULL
      AntennaId               INTEGER                 NOT NULL
      ObservationTime         TIME                    NULL
      ExecBlockUID            VARCHAR(100)            NULL
      ScanNumber              INTEGER                 NULL
      SoftwareVersion         VARCHAR(100)            NULL
      Comments                TEXT                    NULL
      SourceDensity           DOUBLE                  NULL
      Locked                  BOOLEAN                 NULL
      IncreaseVersion         BOOLEAN                 NULL
      CurrentVersion          INTEGER                 NULL
      Who                     NAME                    NULL
      ChangeDesc              TEXT                    NULL
      KEY FocusModelId GENERATED FROM AntennaId
      CONSTRAINT AntennaFMAntenna FOREIGN KEY (AntennaId) REFERENCES Antenna
ENDTABLE

TABLE FocusModelCoeff
     FocusModelCoeffId       INTEGER                 NOT NULL
     FocusModelId            INTEGER                 NOT NULL
     CoeffName               NAME                    NOT NULL
     CoeffValue              DOUBLE                  NOT NULL
     KEY FocusModelCoeffId GENERATED FROM FocusModelId CoeffName
     CONSTRAINT AntFMTermFocusModelId FOREIGN KEY (FocusModelId) REFERENCES FocusModel
ENDTABLE

TABLE FocusModelCoeffOffset
     FocusModelCoeffId       INTEGER                 NOT NULL
     ReceiverBand            NAME                    NOT NULL
     Offset                  DOUBLE                  NOT NULL
     KEY FocusModelCoeffId ReceiverBand
     CONSTRAINT AntFMCoeffOffToCoeff FOREIGN KEY (FocusModelCoeffId) REFERENCES FocusModelCoeff
     CONSTRAINT AntennaFMCoeffOffBand CHECK (ReceiverBand IN ('ALMA_RB_01', 'ALMA_RB_02', 'ALMA_RB_03',
         'ALMA_RB_04', 'ALMA_RB_05', 'ALMA_RB_06', 'ALMA_RB_07', 'ALMA_RB_08', 'ALMA_RB_09', 'ALMA_RB_10'))
ENDTABLE

// ============================================================================
// Monitoring tables.
//
// These tables are used by the monitoring system.
//

// The Default Component table have the static information coming from
// ALMA Components
TABLE DefaultComponent
    DefaultComponentId      INTEGER                 NOT NULL
    ComponentTypeId         INTEGER                 NOT NULL
    AssemblyTypeName        LONGNAME                NOT NULL
    ImplLang                ImplLangEnum
    RealTime                BOOLEAN                 NOT NULL
    Code                    LONGNAME                NOT NULL
    Path                    LONGNAME                NOT NULL 
    IsAutostart             BOOLEAN                 NOT NULL
    IsDefault               BOOLEAN                 NOT NULL
    IsStandaloneDefined     BOOLEAN                 NULL
    KeepAliveTime           INTEGER                 NOT NULL      
    MinLogLevel             TINYINT                 DEFAULT -1
    MinLogLevelLocal        TINYINT                 DEFAULT -1
    XMLDoc                  XMLCLOB                 NULL
    KEY DefaultComponentId
    CONSTRAINT DefaultComponentTypeId   FOREIGN KEY (ComponentTypeId)  REFERENCES ComponentType
    CONSTRAINT DefaultComponentAssemblyId FOREIGN KEY (AssemblyTypeName) REFERENCES AssemblyType
ENDTABLE

// For supporting dynamic discovery of device-serial number, next two
// tables with default information was added
TABLE DefaultBaciProperty
     DefaultBaciPropId     INTEGER                    NOT NULL
     DefaultComponentId    INTEGER                    NOT NULL
     PropertyName          NAME                   NOT NULL
     description           TEXT                   NOT NULL
     format                LONGVARCHAR (16)       NOT NULL
     units                 LONGVARCHAR (24)       NOT NULL
     resolution            LONGVARCHAR (10)       NOT NULL
     archive_priority      INTEGER                    NOT NULL
     archive_min_int       DOUBLE                 NOT NULL
     archive_max_int       DOUBLE                 NOT NULL
     archive_mechanism     LONGVARCHAR (24)       NOT NULL
     archive_suppress      BOOLEAN                NOT NULL
     default_timer_trig    DOUBLE                 NOT NULL
     min_timer_trig        DOUBLE                 NOT NULL
     initialize_devio      BOOLEAN                NOT NULL
     min_delta_trig        DOUBLE                 NULL
     default_value         TEXT                   NOT NULL
     graph_min             DOUBLE                 NULL
     graph_max             DOUBLE                 NULL
     min_step              DOUBLE                 NULL
     archive_delta         DOUBLE                 NOT NULL
     archive_delta_percent DOUBLE                 NULL
     alarm_high_on         DOUBLE                 NULL
     alarm_low_on          DOUBLE                 NULL
     alarm_high_off        DOUBLE                 NULL
     alarm_low_off         DOUBLE                 NULL
     alarm_timer_trig      DOUBLE                 NULL
     min_value             DOUBLE                 NULL
     max_value             DOUBLE                 NULL
     bitDescription        TEXT                   NULL
     whenSet               TEXT                   NULL
     whenCleared           TEXT                   NULL
     statesDescription     TEXT                   NULL
     condition             TEXT                   NULL
     alarm_on              TEXT                   NULL
     alarm_off             TEXT                   NULL
     alarm_fault_family    TEXT                   NULL
     alarm_fault_member    TEXT                   NULL
     alarm_level           INTEGER                NULL
     Data                  TEXT                   NULL
     KEY DefaultBaciPropId
     CONSTRAINT DefBACIDefaultComponentTypeId   FOREIGN KEY (DefaultComponentId)  REFERENCES DefaultComponent
ENDTABLE

// Next table allows for the feature Dynamic discovery of
// Device-SerialNumber
TABLE DefaultMonitorPoint
    DefaultMonitorPointId          INTEGER                     NOT NULL
    DefaultBACIPropertyId          INTEGER                     NOT NULL
    MonitorPointName         NAME                   NOT NULL
    Indice                  INTEGER                     NOT NULL
    DataType                LONGVARCHAR (16)            NOT NULL
    RCA                     LONGVARCHAR (16)            NOT NULL
    TeRelated               BOOLEAN                 NOT NULL
    RawDataType             LONGVARCHAR (24)            NOT NULL
    WorldDataType           LONGVARCHAR (24)            NOT NULL
    Units                   LONGVARCHAR (24)            NULL
    Scale                   DOUBLE                  NULL
    Offset                  LENGTH                  NULL
    MinRange                LONGVARCHAR (24)        NULL
    MaxRange                LONGVARCHAR (24)        NULL
    Description             TEXT                    NOT NULL
    KEY DefaultMonitorPointId
    CONSTRAINT DefaulPntId FOREIGN KEY (DefaultBACIPropertyId) REFERENCES DefaultBaciProperty
ENDTABLE

// MonitorPoint represents the monitored properties of an assembly.
// MonitorPointId is a generated unique key.  The real requirement is
// that combination of AssemblyName and PropertyName are unique.  The
// reason for choosing a generated key is that this key is referenced
// in the property tables and we want these to be as small as possible
// because this is where the bulk of the data is located.  The number
// of property types will be in the thousands, but will not exceed
// 10,000.
TABLE MonitorPoint
     MonitorPointId          INTEGER                     NOT NULL
     BACIPropertyId          INTEGER                     NOT NULL
     MonitorPointName        NAME                    NOT NULL
     AssemblyId              INTEGER                	 NOT NULL
     Indice                  INTEGER                	 NOT NULL
     DataType                LONGVARCHAR (16)        NOT NULL
     RCA                     LONGVARCHAR (16)        NOT NULL
     TeRelated               BOOLEAN                 NOT NULL
     RawDataType             LONGVARCHAR (24)        NOT NULL
     WorldDataType           LONGVARCHAR (24)        NOT NULL
     Units                   LONGVARCHAR (24)        NULL
     Scale                   DOUBLE                  NULL
     Offset                  LENGTH                  NULL
     MinRange                LONGVARCHAR (24)        NULL
     MaxRange                LONGVARCHAR (24)        NULL
     Description             TEXT                    NOT NULL
     KEY MonitorPointId GENERATED FROM BACIPropertyId AssemblyId Indice 
     CONSTRAINT MonitorPointAssemblyId FOREIGN KEY (AssemblyId) REFERENCES Assembly
     CONSTRAINT MonitorPointDatatype CHECK (DataType IN ('float', 'double', 'boolean', 'string', 'integer', 'enum', 'clob'))
     CONSTRAINT MonitorPointBACIPropertyId FOREIGN KEY (BACIPropertyId) REFERENCES BACIProperty
ENDTABLE

TABLE MonitorData
     MonitorPointId          INTEGER                     NOT NULL
     StartTime               TIME                    NOT NULL
     EndTime                 TIME                   NOT NULL
     MonitorTS                TSTAMP               NOT NULL
     SampleSize              INTEGER                     NOT NULL
     MonitorClob             CLOB                    NOT NULL
     MinStat                     DOUBLE                   NULL
     MaxStat                     DOUBLE                   NULL
     MeanStat                    DOUBLE                   NULL
     StdDevStat                  DOUBLE                   NULL
     KEY MonitorPointId  MonitorTS
     CONSTRAINT MonitorDataMonitorPointId FOREIGN KEY (MonitorPointId) REFERENCES MonitorPoint
ENDTABLE

// ============================================================================
// Historical tables.
//
// These tables record the occurrence of various events in the system.
// These tables are not being filled currently.
//

// BaseElements that came online, and the interval of time that they stayed
// in this state.
TABLE BaseElementOnline
     BaseElementOnlineId     INTEGER                     NOT NULL
     BaseElementId           INTEGER                     NOT NULL
     ConfigurationId         INTEGER                     NOT NULL
     StartTime               TIME                        NOT NULL
     EndTime                 TIME                        NULL
     NormalTermination       BOOLEAN                     NOT NULL
     KEY BaseElementOnlineId GENERATED FROM BaseElementId ConfigurationId StartTime
     CONSTRAINT BEOnlineId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
     CONSTRAINT BEOnlineConfig FOREIGN KEY (ConfigurationId) REFERENCES HWConfiguration
ENDTABLE

// The assemblies in each baseelement that came online.
TABLE AssemblyOnline
     AssemblyOnlineId        INTEGER                     NOT NULL
     AssemblyId              INTEGER                     NOT NULL
     BaseElementOnlineId     INTEGER                     NOT NULL
     RoleName                NAME                        NOT NULL
     StartTime               TIME                        NOT NULL
     EndTime                 TIME                        NULL
     KEY AssemblyOnlineId GENERATED FROM AssemblyId BaseElementOnlineId
     CONSTRAINT BEAssemblyListId FOREIGN KEY (BaseElementOnlineId) REFERENCES BaseElementOnline
     CONSTRAINT BEAssemblyListAssemblyId FOREIGN KEY (AssemblyId) REFERENCES Assembly
ENDTABLE

TABLE Array
     ArrayId                 INTEGER                     NOT NULL
     BaseElementId           INTEGER                     NOT NULL
     Type                    LONGVARCHAR (9)             NOT NULL
     UserId                  LONGNAME                NULL
     StartTime               TIME                    NOT NULL
     EndTime                 TIME                    NULL
     NormalTermination       BOOLEAN                 NOT NULL
     KEY ArrayId GENERATED FROM StartTime BaseElementId
     CONSTRAINT ArrayBEId FOREIGN KEY (BaseElementId) REFERENCES BaseElement
     CONSTRAINT ArrayType CHECK (Type IN ('automatic', 'manual'))
ENDTABLE

// The AntennaToArray table give the antennas that belong to an array.
TABLE AntennaToArray
     AntennaId               INTEGER                     NOT NULL
     ArrayId                 INTEGER                     NOT NULL
     KEY AntennaId ArrayId
     CONSTRAINT AntennaToArrayAntennaId FOREIGN KEY (AntennaId) REFERENCES Antenna
     CONSTRAINT AntennaToArrayArrayid FOREIGN KEY (ArrayId) REFERENCES Array
ENDTABLE

// The SBExecution table gives the UIDs of the scheduling blocks
// executed by the indicated array.
TABLE SBExecution
     ArrayId                 INTEGER                     NOT NULL
     SbUID                   LONGNAME                NOT NULL
     StartTime               TIME                    NOT NULL
     EndTime                 TIME                    NULL
     NormalTermination       BOOLEAN                 NOT NULL
     KEY ArrayId SbUID StartTime
     CONSTRAINT SBExecutionArrayId FOREIGN KEY (ArrayId) REFERENCES Array
ENDTABLE

// The AntennaToFrontEnd table gives the front end that is on an
// antenna at the indicated time.
TABLE AntennaToFrontEnd
     AntennaToFrontEndId     INTEGER                    NOT NULL
     AntennaId               INTEGER                     NOT NULL
     FrontEndId              INTEGER                     NOT NULL
     StartTime               TIME                    NOT NULL
     EndTime                 TIME                    NULL
     KEY AntennaToFrontEndId GENERATED FROM AntennaId FrontEndId StartTime
     CONSTRAINT AntennaToFEAntennaId FOREIGN KEY (AntennaId) REFERENCES Antenna
     CONSTRAINT AntennaToFEFrontEndId FOREIGN KEY (FrontEndId) REFERENCES FrontEnd
ENDTABLE

// ============================================================================
// Backlog tables.
//
// Tracking log tables are used to keep the history of modifications made
// in the database for some tables.

TABLE BL_VersionInfo
      TableName               NAME                    NOT NULL 
      SwConfigurationId       INTEGER                 NOT NULL
      EntityId                INTEGER                 NOT NULL
      Locked                  BOOLEAN                 NOT NULL
      IncreaseVersion         BOOLEAN                 NOT NULL
      CurrentVersion          INTEGER                 NOT NULL
      Who                     NAME                    NOT NULL
      ChangeDesc              TEXT                    NOT NULL
      KEY TableName SwConfigurationId EntityId
      CONSTRAINT VersionInfoSwCnfId FOREIGN KEY (SwConfigurationId) REFERENCES Configuration
ENDTABLE

TABLE BL_PointingModelCoeff
      Version                 INTEGER                 NOT NULL
      ModTime                 TIME                    NOT NULL
      Operation               CHAR(1)                 NOT NULL
      Who                     NAME                    NULL
      ChangeDesc              TEXT                    NULL
      PointingModelId         INTEGER                 NOT NULL
      CoeffName               NAME                    NOT NULL
      CoeffValue              DOUBLE                  NOT NULL
      KEY Version ModTime Operation PointingModelId CoeffName
      CONSTRAINT BL_PointingModelCoeffOp CHECK (Operation IN ('I', 'U', 'D'))
ENDTABLE

TABLE BL_PointingModelCoeffOffset
      Version                 INTEGER                 NOT NULL
      ModTime                 TIME                    NOT NULL
      Operation               CHAR(1)                 NOT NULL
      Who                     NAME                    NULL
      ChangeDesc              TEXT                    NULL
      PointingModelId         INTEGER                 NOT NULL
      CoeffName               NAME                    NOT NULL
      ReceiverBand            NAME                    NOT NULL
      Offset                  DOUBLE                  NOT NULL
      KEY Version ModTime Operation PointingModelId CoeffName ReceiverBand
      CONSTRAINT BL_AntennaPMCoeffOffOp CHECK (Operation IN ('I', 'U', 'D'))
      CONSTRAINT BL_AntennaPMCoeffOffBand CHECK (ReceiverBand IN ('ALMA_RB_01', 'ALMA_RB_02', 'ALMA_RB_03',
         'ALMA_RB_04', 'ALMA_RB_05', 'ALMA_RB_06', 'ALMA_RB_07', 'ALMA_RB_08', 'ALMA_RB_09', 'ALMA_RB_10'))
ENDTABLE

TABLE BL_FocusModelCoeff
      Version                 INTEGER                 NOT NULL
      ModTime                 TIME                    NOT NULL
      Operation               CHAR(1)                 NOT NULL
      Who                     NAME                    NULL
      ChangeDesc              TEXT                    NULL
      FocusModelId            INTEGER                 NOT NULL
      CoeffName               NAME                    NOT NULL
      CoeffValue              DOUBLE                  NOT NULL
      KEY Version ModTime Operation FocusModelId CoeffName
      CONSTRAINT BL_FocusModelCoeffOp CHECK (Operation IN ('I', 'U', 'D'))
ENDTABLE

TABLE BL_FocusModelCoeffOffset
      Version                 INTEGER                 NOT NULL
      ModTime                 TIME                    NOT NULL
      Operation               CHAR(1)                 NOT NULL
      Who                     NAME                    NULL
      ChangeDesc              TEXT                    NULL
      FocusModelId            INTEGER                 NOT NULL
      CoeffName               NAME                    NOT NULL
      ReceiverBand            NAME                    NOT NULL
      Offset                  DOUBLE                  NOT NULL
      KEY Version ModTime Operation FocusModelId CoeffName ReceiverBand
      CONSTRAINT BL_AntennaFMCoeffOffOp CHECK (Operation IN ('I', 'U', 'D'))
      CONSTRAINT BL_AntennaFMCoeffOffBand CHECK (ReceiverBand IN ('ALMA_RB_01', 'ALMA_RB_02', 'ALMA_RB_03',
         'ALMA_RB_04', 'ALMA_RB_05', 'ALMA_RB_06', 'ALMA_RB_07', 'ALMA_RB_08', 'ALMA_RB_09', 'ALMA_RB_10'))
ENDTABLE

TABLE BL_FEDelay
      Version                  INTEGER                NOT NULL
      ModTime                  TIME                   NOT NULL
      Operation                CHAR(1)                NOT NULL
      Who                      NAME                   NULL
      ChangeDesc               TEXT                   NULL
      FEDelayId                INTEGER                NOT NULL
      AntennaId                INTEGER                NOT NULL
      ReceiverBand             NAME                   NOT NULL
      Polarization             NAME                   NOT NULL
      SideBand                 NAME                   NOT NULL
      Delay                    DOUBLE                 NOT NULL
      KEY Version ModTime Operation FEDelayId
      CONSTRAINT BL_FEDelayOp CHECK (Operation IN ('I', 'U', 'D'))
ENDTABLE

TABLE BL_IFDelay
      Version                  INTEGER                NOT NULL
      ModTime                  TIME                   NOT NULL
      Operation                CHAR(1)                NOT NULL
      Who                      NAME                   NULL
      ChangeDesc               TEXT                   NULL
      IFDelayId                INTEGER                NOT NULL
      AntennaId                INTEGER                NOT NULL
      BaseBand                 NAME                   NOT NULL
      Polarization             NAME                   NOT NULL
      IFSwitch                 NAME                   NOT NULL
      Delay                    DOUBLE                 NOT NULL
      KEY Version ModTime Operation IFDelayId
      CONSTRAINT BL_IFDelayOp CHECK (Operation IN ('I', 'U', 'D'))
ENDTABLE

TABLE BL_LODelay
      Version                  INTEGER                NOT NULL
      ModTime                  TIME                   NOT NULL
      Operation                CHAR(1)                NOT NULL
      Who                      NAME                   NULL
      ChangeDesc               TEXT                   NULL
      LODelayId                INTEGER                NOT NULL
      AntennaId                INTEGER                NOT NULL
      BaseBand                 NAME                   NOT NULL
      Delay                    DOUBLE                 NOT NULL
      KEY Version ModTime Operation LODelayId
      CONSTRAINT BL_LODelayOp CHECK (Operation IN ('I', 'U', 'D'))
ENDTABLE

TABLE BL_XPDelay
      Version                  INTEGER                NOT NULL
      ModTime                  TIME                   NOT NULL
      Operation                CHAR(1)                NOT NULL
      Who                      NAME                   NULL
      ChangeDesc               TEXT                   NULL
      XPDelayId                INTEGER                NOT NULL
      ConfigurationId          INTEGER                NOT NULL
      ReceiverBand             NAME                   NOT NULL
      SideBand                 NAME                   NOT NULL
      BaseBand                 NAME                   NOT NULL
      Delay                    DOUBLE                 NOT NULL
      KEY Version ModTime Operation XPDelayId
      CONSTRAINT BL_XPDelayOp CHECK (Operation IN ('I', 'U', 'D'))
ENDTABLE

TABLE BL_AntennaDelay
      Version                  INTEGER                NOT NULL
      ModTime                  TIME                   NOT NULL
      Operation                CHAR(1)                NOT NULL
      Who                      NAME                   NULL
      ChangeDesc               TEXT                   NULL
      BaseElementId            INTEGER                NOT NULL
      Delay                    DOUBLE                 NOT NULL
      KEY Version ModTime Operation BaseElementId
ENDTABLE

TABLE BL_Antenna
     Version                   INTEGER                 NOT NULL
     ModTime                   TIME                    NOT NULL
     Operation                 CHAR(1)                 NOT NULL
     Who                       NAME                    NULL
     ChangeDesc                TEXT                    NULL
     BaseElementId             INTEGER                 NOT NULL
     AntennaType               LONGVARCHAR (4)         NOT NULL
     DishDiameter              LENGTH                  NOT NULL
     CommissionDate            TIME                    NOT NULL
     XPosition                 LENGTH                  NOT NULL
     YPosition                 LENGTH                  NOT NULL
     ZPosition                 LENGTH                  NOT NULL
     XOffset                   LENGTH                  NOT NULL
     YOffset                   LENGTH                  NOT NULL
     ZOffset                   LENGTH                  NOT NULL
     LOOffsettingIndex         INTEGER                 NOT NULL
     WalshSeq                  INTEGER                 NOT NULL     
     CaiBaseline               INTEGER                 NULL     
     CaiAca                    INTEGER                 NULL     
     KEY Version ModTime Operation BaseElementId
ENDTABLE

TABLE BL_Pad
      Version                  INTEGER                NOT NULL
      ModTime                  TIME                   NOT NULL
      Operation                CHAR(1)                NOT NULL
      Who                      NAME                   NULL
      ChangeDesc               TEXT                   NULL
      BaseElementId            INTEGER                NOT NULL
      CommissionDate           TIME                   NOT NULL
      XPosition                LENGTH                 NOT NULL
      YPosition                LENGTH                 NOT NULL
      ZPosition                LENGTH                 NOT NULL
      Delay                    DOUBLE                 NOT NULL
      KEY Version ModTime Operation BaseElementId
ENDTABLE

TABLE BL_AntennaToPad
      Version                  INTEGER                NOT NULL
      ModTime                  TIME                   NOT NULL
      Operation                CHAR(1)                NOT NULL
      Who                      NAME                   NULL
      ChangeDesc               TEXT                   NULL
      AntennaToPadId           INTEGER                NOT NULL
      MountMetrologyAN0Coeff   DOUBLE                 NULL
      MountMetrologyAW0Coeff   DOUBLE                 NULL
      KEY Version ModTime Operation AntennaToPadId
ENDTABLE

TABLE BL_AcaCorrDelays
    Version                    INTEGER                NOT NULL
    ModTime                    TIME                   NOT NULL
    Operation                  CHAR(1)                NOT NULL
    Who                        NAME                   NULL
    ChangeDesc                 TEXT                   NULL
    AntennaId                  INTEGER                NOT NULL
    BbOneDelay                 DOUBLE                 NOT NULL
    BbTwoDelay                 DOUBLE                 NOT NULL
    BbThreeDelay               DOUBLE                 NOT NULL
    BbFourDelay                DOUBLE                 NOT NULL
    KEY Version ModTime Operation AntennaId
ENDTABLE

// ============================================================================
// QA1 Tables

TABLE AntennaEfficiency
    AntennaEfficiencyId INTEGER NOT NULL
    AntennaId           INTEGER NOT NULL
    ObservationTime     TIME    NOT NULL
    ExecBlockUID        VARCHAR(100) NOT NULL
    ScanNumber          INTEGER NOT NULL
    ThetaMinorPolX      DOUBLE NOT NULL
    ThetaMinorPolY      DOUBLE NOT NULL
    ThetaMajorPolX      DOUBLE NOT NULL
    ThetaMajorPolY      DOUBLE NOT NULL
    PositionAngleBeamPolX DOUBLE NOT NULL
    PositionAngleBeamPolY DOUBLE NOT NULL
    SourceName          VARCHAR(100) NOT NULL
    SourceSize          DOUBLE  NOT NULL
    Frequency           DOUBLE  NOT NULL
    ApertureEff         DOUBLE  NOT NULL
    ApertureEffError    DOUBLE  NOT NULL
    ForwardEff          DOUBLE  NOT NULL
    ForwardEffError     DOUBLE  NOT NULL
    KEY AntennaEfficiencyId GENERATED
    CONSTRAINT AntEffToAntenna FOREIGN KEY (AntennaId) REFERENCES Antenna
ENDTABLE

TABLE ReceiverQuality
    ReceiverQualityId INTEGER NOT NULL
    AntennaId         INTEGER NOT NULL
    ObservationTime   TIME    NOT NULL
    ExecBlockUID      VARCHAR(100) NOT NULL
    ScanNumber        INTEGER NOT NULL
    KEY ReceiverQualityId GENERATED
    CONSTRAINT RecQualityToAntenna FOREIGN KEY (AntennaId) REFERENCES Antenna
ENDTABLE

TABLE ReceiverQualityParameters
    ReceiverQualityParamId INTEGER NOT NULL
    ReceiverQualityId INTEGER NOT NULL
    Frequency         DOUBLE  NOT NULL
    SidebandRatio     DOUBLE  NOT NULL
    Trx               DOUBLE  NOT NULL
    Polarization      DOUBLE  NOT NULL
    BandPassQuality   DOUBLE  NOT NULL
    KEY ReceiverQualityParamId GENERATED
    CONSTRAINT RecQualityParamToRecQual FOREIGN KEY (ReceiverQualityId) REFERENCES ReceiverQuality
ENDTABLE

TABLE Holography
    HolographyId     INTEGER NOT NULL
    AntennaId        INTEGER NOT NULL
    ObservationTime  TIME    NOT NULL
    ExecBlockUID     VARCHAR(100) NOT NULL
    ScanNumber       INTEGER NOT NULL
    ObservationDuration DOUBLE NOT NULL
    LowElevation     DOUBLE       NOT NULL
    HighElevation    DOUBLE       NOT NULL
    MapSize          DOUBLE       NOT NULL
    SoftwareVersion  VARCHAR(100) NOT NULL
    ObsMode          VARCHAR(80)  NOT NULL
    Comments         TEXT    NULL
    Frequency        DOUBLE  NOT NULL
    ReferenceAntenna INTEGER NOT NULL
    AstigmatismX2Y2  DOUBLE  NOT NULL
    AstigmatismXY    DOUBLE  NOT NULL
    AstigmatismErr   DOUBLE  NOT NULL
    PhaseRMS         DOUBLE  NOT NULL
    SurfaceRMS       DOUBLE  NOT NULL
    SurfaceRMSNoAstig DOUBLE NOT NULL
    Ring1RMS         DOUBLE  NOT NULL         
    Ring2RMS         DOUBLE  NOT NULL         
    Ring3RMS         DOUBLE  NOT NULL         
    Ring4RMS         DOUBLE  NOT NULL         
    Ring5RMS         DOUBLE  NOT NULL         
    Ring6RMS         DOUBLE  NOT NULL         
    Ring7RMS         DOUBLE  NOT NULL         
    Ring8RMS         DOUBLE  NOT NULL         
    BeamMapFitUID    VARCHAR(100) NOT NULL
    SurfaceMapFitUID VARCHAR(100) NOT NULL
    XFocus           DOUBLE  NOT NULL
    XFocusErr        DOUBLE  NOT NULL
    YFocus           DOUBLE  NOT NULL
    YFocusErr        DOUBLE  NOT NULL
    ZFocus           DOUBLE  NOT NULL
    ZFocusErr        DOUBLE  NOT NULL
    KEY HolographyId GENERATED
    CONSTRAINT HolographyToAntenna FOREIGN KEY (AntennaId) REFERENCES Antenna
    CONSTRAINT HolographyRefAntenna FOREIGN KEY (ReferenceAntenna) REFERENCES Antenna
    CONSTRAINT HolographyObsMode CHECK (ObsMode IN ('TOWER', 'ASTRO'))
ENDTABLE

// === oOo ===
