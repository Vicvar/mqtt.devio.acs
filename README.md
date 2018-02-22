# mqtt.devio.acs
ACSMQTT consists of a way to use some characteristics of ACS to manage the monitoring of data published ina mqtt broker.

This repository consists of 3 parts

#### acs
Contains a test component, sensor_tag, where the subscription to topics is implemented, and WeaterStation where it is possible to remotely call some methods previously defined. By default the component has 3 baci properties (temperature, light and humidity) implementing reading and writing channels for them.If you want to define a new baci it is necessary to declare it in sensortag/sensortag.idl and in sensortag/config/CDB/schemas/SensorTag.xsd, besides defining it in $ACS_CDB/CDB/alma/<<componentName>>/SensorTag. xml (here is also defined the address of the mosquito broker to use), and then add the subscription to the topic using the DevIo in sensortag_impl.cpp. The CBD also defines the components of ACS that allow the collection of the data of all the defined baci properties and the sending of these to an ActiveMQ intermediary (Collector, Controller and Blobber).

#### BlobberPlugin
Corresponds to an instance that is called from Blobber and in this particular case allows the publication of the data received and stored to a topic of a broker activeMQ.
Parameters like activemq broker address, topical, etc. they are configurable from a configuration file, which is read from /alma/ACS _ $ {version}/acsdata/config.

#### MonitoringAccessSystem
Allows the monitoring of the data published in activemq, subscribes to the topic and filters the data of each message transforming them to redis channels for each baci properties, then it extracts the data from each of these channels and transforms them into text files stored in specific routes according to the activemq topic, the MQTT topic (component name) and its date. It is also possible to publish the data in influxdb and the analysis of these as time series.
Parameters such as subscription topical address, influxdb url, archiver and influxdb status can be modified from a configuration file.
The script is installed in $ INTROOT, so the destination folder must have that type of structure.

## Dependencies
  * ACS (https://github.com/ACS-Community/ACS)
  * mosquitto (https://mosquitto.org/)
  * paho.mqtt.cpp (https://github.com/eclipse/paho.mqtt.cpp)
  * Apache activeMQ (http://activemq.apache.org/)
  * InfluxDB (https://www.influxdata.com/)
  * Redis (https://redis.io/)
