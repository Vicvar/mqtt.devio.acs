#ifndef _SENSORTAG_IMPL_H_
#define _SENSORTAG_IMPL_H_

#include <sensortagS.h>

#include <baciCharacteristicComponentImpl.h>
#include <baciSmartPropertyPointer.h>
#include <baciROdouble.h>
#include <baciDevIO.h>
#include <acsThread.h>

class sensortag_thread;

class sensortag_impl:   public virtual POA_Sensors::sensortag,
        public baci::CharacteristicComponentImpl
{
        public:
                sensortag_impl(const ACE_CString name, maci::ContainerServices * containerServices);
                virtual ~sensortag_impl();

                ACS::ROdouble_ptr temperature();
                ACS::ROdouble_ptr light();
                ACS::ROdouble_ptr humidity();

                virtual void initialize(void) throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl);
                virtual void execute(void) throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl);
                virtual void cleanUp(void);
                virtual void aboutToAbort(void);

        private:
                baci::SmartPropertyPointer<baci::ROdouble> temperature_m;
                baci::SmartPropertyPointer<baci::ROdouble> light_m;
                baci::SmartPropertyPointer<baci::ROdouble> humidity_m;

                std::string component_name;

                sensortag_thread * refresh_thread;
};


class sensortag_devio :     public virtual DevIO<CORBA::Double>
{
        public:
                enum sensor_t{temperatue_t, light_t, humidity_t};
                
                sensortag_devio(sensor_t sensor, sensortag_thread* thread);
                virtual ~sensortag_devio();
                virtual bool initilizeValue();
                virtual CORBA::Double read(unsigned long long& timestamp);
                virtual void write(const CORBA::Double &value, unsigned long long& timestamp);
        private:
                const sensor_t sensor;
                sensortag_thread * thread;

};

#define STMAC "B0:B4:48:C9:13:04"

class sensortag_thread : public ACS::Thread
{
        public:
                sensortag_thread(const ACE_CString& name );
                virtual ~sensortag_thread();
                virtual void runLoop();
                CORBA::Double get_temperature();
                CORBA::Double get_light();
                CORBA::Double get_humidity();

        private:
                CORBA::Double temperature;
                CORBA::Double light;
                CORBA::Double humidity;

                static const char* command;
                static const char* arguments[];

                int createChildProcess(
                    const char* command, char* const arguments[], char* const environment[]);


};

#endif
