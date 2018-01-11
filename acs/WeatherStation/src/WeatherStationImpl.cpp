#include "WeatherStationImpl.h"


WeatherStationImpl::WeatherStationImpl(
		    const ACE_CString name,
		    maci::ContainerServices *containerServices) :
        ACSComponentImpl(name, containerServices)
{
  ACS_SHORT_LOG((LM_WARNING, "WeatherStationImpl::WeatherStationImpl()"));

  component_name = name.c_str();
}

WeatherStationImpl::~WeatherStationImpl()
{
}

void WeatherStationImpl::initialize()
        throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl)
{
  ACS_SHORT_LOG((LM_WARNING, "WeatherStationImpl::initialize()"));
  
  const std::string currentComponent("SensorTag");
  sensortag_sp = getContainerServices()->getComponentSmartPtr<Sensors::sensortag>(currentComponent.c_str());
  ACS_SHORT_LOG((LM_WARNING, "SensorTag Component Retrieved Successfully!"));
}

void WeatherStationImpl::execute()
        throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl)
{
}

void WeatherStationImpl::cleanUp()
{
  ACS_SHORT_LOG((LM_WARNING, "WeatherStationImpl::cleanUp()"));
  
  sensortag_sp.release();
  ACS_SHORT_LOG((LM_WARNING, "SensorTag Component Released Successfully!"));
}

void WeatherStationImpl::aboutToAbort()
{
} 

double WeatherStationImpl::getTemperature()
{
  ACS_SHORT_LOG((LM_WARNING, "WeatherStationImpl::getTemperature()"));

  ACSErr::Completion_var completion;
  CORBA::Double var = sensortag_sp->temperature()->get_sync(completion.out());
  ACS_SHORT_LOG((LM_INFO,"Temperature: %f", var)); 
  return var;
}

double WeatherStationImpl::getHumidity()
{
  ACS_SHORT_LOG((LM_WARNING, "WeatherStationImpl::getHumidity()"));

  ACSErr::Completion_var completion;
  CORBA::Double var = sensortag_sp->humidity()->get_sync(completion.out());
  ACS_SHORT_LOG((LM_INFO,"Humidity: %f", var)); 
  return var;
}

double WeatherStationImpl::getLight()
{
  ACS_SHORT_LOG((LM_WARNING, "WeatherStationImpl::getLight()"));

  ACSErr::Completion_var completion;
  CORBA::Double var = sensortag_sp->light()->get_sync(completion.out());
  ACS_SHORT_LOG((LM_INFO,"Light: %f", var)); 
  return var;
}
void WeatherStationImpl::writeTemperature()
{
  ACS_SHORT_LOG((LM_WARNING, "WeatherStationImpl::writeTemperature()"));

  ACSErr::Completion_var completion;
  sensortag_sp->publishTemperature();
}
void WeatherStationImpl::writeHumidity()
{
  ACS_SHORT_LOG((LM_WARNING, "WeatherStationImpl::writeHumidity()"));

  ACSErr::Completion_var completion;
  sensortag_sp->publishHumidity();
}
void WeatherStationImpl::writeLight()
{
  ACS_SHORT_LOG((LM_WARNING, "WeatherStationImpl::writeLight()"));

  ACSErr::Completion_var completion;
  sensortag_sp->publishLight();
}


/* --------------- [ MACI DLL support functions ] -----------------*/
#include <maciACSComponentDefines.h>
MACI_DLL_SUPPORT_FUNCTIONS(WeatherStationImpl)
/* ----------------------------------------------------------------*/

