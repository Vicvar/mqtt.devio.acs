#include "sensortag_impl.h"


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
}

void sensortag_impl::initialize()
        throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl)
{
        on();
        temperature_m =  new baci::ROdouble(
			(component_name + ":temperature").c_str(),
                        getComponent(), new sensortag_devio(sensortag_devio::temperatue_t, refresh_thread));
        light_m = new baci::ROdouble(
			(component_name + ":light").c_str(),
                        getComponent(), new sensortag_devio(sensortag_devio::light_t, refresh_thread));
        humidity_m = new baci::ROdouble (
			(component_name + ":humidity").c_str(),
                        getComponent(), new sensortag_devio(sensortag_devio::humidity_t, refresh_thread));
	
}

void sensortag_impl::execute()
        throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl)
{
}

void sensortag_impl::cleanUp()
{
        AUTO_TRACE(__PRETTY_FUNCTION__);
        if (refresh_thread) {
                try {
                    this->off();
                } catch(...) {
                        ACS_SHORT_LOG((LM_WARNING, "Something went wrong with thr thread deactivation :("));
                }
        }
        getContainerServices()->getThreadManager()->stopAll();
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
        AUTO_TRACE(__PRETTY_FUNCTION__);
        if (refresh_thread == NULL) {
                refresh_thread = getContainerServices()->getThreadManager()->
                        create<sensortag_thread>(ACE_CString("sensortag_refresh_thread"));
                        refresh_thread->resume();
        } else {
                refresh_thread->resume();
        }
}

void sensortag_impl::off()
{
        AUTO_TRACE(__PRETTY_FUNCTION__);
        if(refresh_thread != NULL) {
                refresh_thread->suspend();
        }
}

sensortag_devio::sensortag_devio(
                sensortag_devio::sensor_t sensor,
                sensortag_thread * thread):
        sensor(sensor), thread(thread)
{

}

sensortag_devio::~sensortag_devio()
{
}

bool sensortag_devio::initilizeValue()
{
        return true;
}

CORBA::Double sensortag_devio::read(ACS::Time& timestamp)
{
        switch (sensor) {
                case temperatue_t: return thread->get_temperature();
                case humidity_t: return thread->get_humidity();
                case light_t: return thread->get_light();
        }
}

void sensortag_devio::write(const CORBA::Double &value, ACS::Time& timestamp)
{
        // NO-OP
}

/* --------------- [ MACI DLL support functions ] -----------------*/
#include <maciACSComponentDefines.h>
MACI_DLL_SUPPORT_FUNCTIONS(sensortag_impl)
/* ----------------------------------------------------------------*/

