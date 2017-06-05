#include "sensortag_impl.h"


sensortag_impl::sensortag_impl(
                const ACE_CString name,
                maci::ContainerServices *containerServices) :
        CharacteristicComponentImpl(name, containerServices),
        temperature_m(this), light_m(this), humidity_m(this)

{
        component_name = name.c_str();
}

sensortag_impl::~sensortag_impl()
{
}

void sensortag_impl::initialize()
        throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl)
{
        temperature_m =  new baci::ROdouble(
			(component_name + ":temperature").c_str(),
                        getComponent() /* devIO here*/);
        light_m = new baci::ROdouble(
			(component_name + ":light").c_str(),
                        getComponent() /*devIO here*/);
        humidity_m = new baci::ROdouble (
			(component_name + ":humidity").c_str(),
                        getComponent() /*devIO here*/);
}

void sensortag_impl::execute()
        throw (acsErrTypeLifeCycle::acsErrTypeLifeCycleExImpl)
{
}

void sensortag_impl::cleanUp()
{
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
