#ifndef _SENSORTAG_IMPL_H_
#define _SENSORTAG_IMPL_H_

#include <sensortagS.h>

#include <baciCharacteristicComponentImpl.h>
#include <baciSmartPropertyPointer.h>
#include <baciROdouble.h>

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
};

#endif
