#include "sensortag_impl.h"
#include <math.h>

sensortag_thread::sensortag_thread(const ACE_CString& name):
        ACS::Thread(name), temperature(0.0), light(0.0), humidity(0.0)
{

}

sensortag_thread::~sensortag_thread()
{
}

void sensortag_thread::runLoop() 
{
	int i = 0;
        while(check()) {
		if (isSuspended() || (i++%10 != 0)) {
			sleep(1);
			continue;
		}
		temperature = 30.0 * sin(2 * 3.1415/1800.0 * time(NULL)) + 45.0;
		light = 70.0 * cos(2 * 3.1415/600.0 * time(NULL)) + 100.0;
		humidity = 50.0 * sin(2 * 3.1415/1800.0 * time(NULL)) * cos(2 * 3.1415/600.0 * time(NULL)) + 50.0;
                sleep(1);
        }
	setStopped();
}

CORBA::Double sensortag_thread::get_temperature()
{
        return temperature;
}

CORBA::Double sensortag_thread::get_light()
{ 
        return light;
}

CORBA::Double sensortag_thread::get_humidity()
{
        return humidity;
}

int sensortag_thread::createChildProcess(
                const char* command, char* const arguments[], char* const environment[])
{
	//NO-OP
	return 0;
}
