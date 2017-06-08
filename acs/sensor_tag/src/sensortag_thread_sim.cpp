#include "sensortag_impl.h"
#include <math.h>

sensortag_thread::sensortag_thread(const ACE_CString& name):
        ACS::Thread(name), temperature(0.0D), light(0.0D), humidity(0.0D)
{

}

sensortag_thread::~sensortag_thread()
{
}

void sensortag_thread::runLoop() 
{
        while(true) {
		temperature = 30.0D * sin(2 * 3.1415D/1800.0D * time(NULL)) + 45.0D;
		light = 70.0D * cos(2 * 3.1415D/600.0D * time(NULL)) + 100.0D;
		humidity = 50.0D * sin(2 * 3.1415D/1800.0D * time(NULL)) * cos(2 * 3.1415D/600.0D * time(NULL)) + 50.0D;
                sleep(10);
        }
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
        return light;
}

int sensortag_thread::createChildProcess(
                const char* command, char* const arguments[], char* const environment[])
{
	//NO-OP
	return 0;
}
