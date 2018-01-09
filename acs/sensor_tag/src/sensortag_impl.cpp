#include "sensortag_impl.h"
#include "mqtt_devio.h"

sensortag_impl::sensortag_impl(
                const ACE_CString name,
                maci::ContainerServices *containerServices) :
        CharacteristicComponentImpl(name, containerServices),
        temperature_m(this), light_m(this), humidity_m(this),
        refresh_thread(NULL)

{
        component_name = name.c_str();
}

sensortag_impl::~sensortag_impl()
{
    delete temperature_devio_m;
    delete light_devio_m;
    delete humidity_devio_m;
}

void sensortag_impl::initialize()
        throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl)
{
//        on();
        temperature_devio_m = new mqtt::mqtt_devio("tcp://localhost:1883", 
                            (component_name + ":temperature").c_str());
        light_devio_m = new mqtt::mqtt_devio("tcp://localhost:1883", 
                            (component_name + ":light").c_str());
        humidity_devio_m = new mqtt::mqtt_devio("tcp://localhost:1883", 
                            (component_name + ":humidity").c_str());
        temperature_m =  new baci::ROdouble(
			(component_name + ":temperature").c_str(),
                        getComponent(), 
                        temperature_devio_m);
        light_m = new baci::ROdouble(
			(component_name + ":light").c_str(),
                        getComponent(), 
                        light_devio_m);
        humidity_m = new baci::ROdouble (
			(component_name + ":humidity").c_str(),
                        getComponent(), 
                        humidity_devio_m);
//        temperature_m =  new baci::ROdouble(
//			(component_name + ":temperature").c_str(),
//                        getComponent(), new sensortag_devio(sensortag_devio::temperatue_t, refresh_thread));
//        light_m = new baci::ROdouble(
//			(component_name + ":light").c_str(),
//                        getComponent(), new sensortag_devio(sensortag_devio::light_t, refresh_thread));
//        humidity_m = new baci::ROdouble (
//			(component_name + ":humidity").c_str(),
//                        getComponent(), new sensortag_devio(sensortag_devio::humidity_t, refresh_thread));
	
}

void sensortag_impl::execute()
        throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl)
{
}

void sensortag_impl::cleanUp()
{
//        AUTO_TRACE(__PRETTY_FUNCTION__);
//        if (refresh_thread) {
//                try {
//                    this->off();
//                } catch(...) {
//                        ACS_SHORT_LOG((LM_WARNING, "Something went wrong with thr thread deactivation :("));
//                }
//        }
//        getContainerServices()->getThreadManager()->stopAll();
}

void sensortag_impl::aboutToAbort()
{
}

ACS::ROdouble_ptr sensortag_impl::temperature()
{
        if (temperature_m == 0)
                return ACS::ROdouble::_nil();
        ACS::ROdouble_var prop = ACS::ROdouble::_narrow(temperature_m->getCORBAReference());
        return prop._retn();
}

ACS::ROdouble_ptr sensortag_impl::light()
{
        if (light_m == 0)
                return ACS::ROdouble::_nil();
        ACS::ROdouble_var prop = ACS::ROdouble::_narrow(light_m->getCORBAReference());
        return prop._retn();
}

ACS::ROdouble_ptr sensortag_impl::humidity()
{
        if (humidity_m == 0)
                return ACS::ROdouble::_nil();
        ACS::ROdouble_var prop = ACS::ROdouble::_narrow(humidity_m->getCORBAReference());
        return prop._retn();
}

void sensortag_impl::on()
{
//        AUTO_TRACE(__PRETTY_FUNCTION__);
//        if (refresh_thread == NULL) {
//                refresh_thread = getContainerServices()->getThreadManager()->
//                        create<sensortag_thread>(ACE_CString("sensortag_refresh_thread"));
//                        refresh_thread->resume();
//        } else {
//                refresh_thread->resume();
//        }
}

void sensortag_impl::off()
{
//        AUTO_TRACE(__PRETTY_FUNCTION__);
//        if(refresh_thread != NULL) {
//                refresh_thread->suspend();
//        }
}

/* --------------- [ MACI DLL support functions ] -----------------*/
#include <maciACSComponentDefines.h>
MACI_DLL_SUPPORT_FUNCTIONS(sensortag_impl)
/* ----------------------------------------------------------------*/

