#ifndef _WEATHERSTATION_IMPL_H_
#define _WEATHERSTATION_IMPL_H_

#include <WeatherStationS.h>
#include <sensortagS.h>
#include <acscomponentImpl.h>
#include <acsComponentSmartPtr.h>
#include <baciROdouble.h>
#include <maciSimpleClient.h>
#include <acsThread.h>

class WeatherStationImpl: public virtual acscomponent::ACSComponentImpl,
  public POA_Weather::WeatherStation
{
        public:
                WeatherStationImpl(const ACE_CString name, maci::ContainerServices * containerServices);
                virtual ~WeatherStationImpl();

		virtual void initialize(void) throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl);
		virtual void execute(void) throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl);
		virtual void cleanUp(void);
		virtual void aboutToAbort(void);

		double getHumidity();
		double getTemperature();
		double getLight(); 
		void writeTemperature(); 
		void writeHumidity(); 
		void writeLight(); 

        private:
                std::string component_name;
		maci::SmartPtr< Sensors::sensortag > sensortag_sp;
};

#endif
