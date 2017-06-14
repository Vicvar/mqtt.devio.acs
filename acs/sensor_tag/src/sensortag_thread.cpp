#include "sensortag_impl.h"


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
			usleep(1000000);
			continue;
		}
                createChildProcess(
                                sensortag_thread::command,
                                sensortag_thread::arguments, 
                                NULL
                                );
                usleep(10000000);
		i = 0;
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

#define PIPE_READ 0
#define PIPE_WRITE 1

const char* sensortag_thread::command = "/usr/bin/gatttool";
const char* sensortag_thread::arguments[] = {"gatttool", "-b", STMAC, "-I"};

int sensortag_thread::createChildProcess(
                const char* command, char* const arguments[], char* const environment[])
{
        int stdin_pipe[2];
        int stdout_pipe[2];
        int child_num;
        int execve_result;
        char act_char;

        if (pipe(stdin_pipe) < 0)
        {
                perror("allocating pipe for child input redirect");
                return -1;
        }
        if (pipe(stdout_pipe) < 0)
        {
                close(stdin_pipe[PIPE_READ]);
                close(stdin_pipe[PIPE_WRITE]);
                perror("allocating pipe for child output redirect");
                return -1;
        }

        child_num = fork();
        if (0 == child_num)
        {
                // child continues here
                // redirect stdin
                if (dup2(stdin_pipe[PIPE_READ], STDIN_FILENO) == -1)
                {
                        perror("redirecting stdin");
                        return -1;
                }

                // redirect stdout
                if (dup2(stdout_pipe[PIPE_WRITE], STDOUT_FILENO) == -1)
                {
                        perror("redirecting stdout");
                        return -1;
                }

                // redirect stderr
                if (dup2(stdout_pipe[PIPE_WRITE], STDERR_FILENO) == -1)
                {
                        perror("redirecting stderr");
                        return -1;
                }

                // all these are for use by parent only
                close(stdin_pipe[PIPE_READ]);
                close(stdin_pipe[PIPE_WRITE]);
                close(stdout_pipe[PIPE_READ]);
                close(stdout_pipe[PIPE_WRITE]);

                // run child process image
                execve_result = execve(command, arguments, environment);

                // if we get here at all, an error occurred, but we are in the child
                // process, so just exit
                perror("exec of the child process");
                throw execve_result;
        }
        else if (child_num > 0)
        {
                // parent continues here

                // close unused file descriptors, these are for child only
                close(stdin_pipe[PIPE_READ]);
                close(stdout_pipe[PIPE_WRITE]);

                //==============================================================
                // Connect
                //==============================================================
                const char* connect_message = "connect \r\n";
                if (NULL != connect_message)
                        write(stdin_pipe[PIPE_WRITE], connect_message, strlen(connect_message));

                //Read console output
                do {
                        read(stdout_pipe[PIPE_READ], &act_char, 1);
                        write(STDOUT_FILENO, &act_char, 1);
                } while (act_char != '\n');

                usleep(1000000);

                //==============================================================
                // Start Temperature measurement
                //==============================================================

                //Start measurement
                const char* start_temperature = "char-write-req 0x24 01 \r\n";
                if (NULL != start_temperature)
                        write(stdin_pipe[PIPE_WRITE], start_temperature, strlen(start_temperature));

                //Turn on notifications
                const char* read_temperature = "char-write-req 0x22 0100 \r\n";
                if (NULL != read_temperature)
                        write(stdin_pipe[PIPE_WRITE], read_temperature, strlen(read_temperature));

                //Set notification period to 2.5 sec
                const char* temperature_period = "char-write-req 0x26 0xFA \r\n";
                if (NULL != temperature_period)
                        write(stdin_pipe[PIPE_WRITE], temperature_period, strlen(temperature_period));


                //==============================================================
                // Start Light measurement
                //==============================================================

                //Start measurement
                const char* start_light = "char-write-req 0x44 01 \r\n";
                if (NULL != start_light)
                        write(stdin_pipe[PIPE_WRITE], start_light, strlen(start_light));

                //Turn on notifications
                const char* read_light = "char-write-req 0x42 0100 \r\n";
                if (NULL != read_light)
                        write(stdin_pipe[PIPE_WRITE], read_light, strlen(read_light));

                //Set notification period to 2.5 sec
                const char* light_period = "char-write-req 0x46 0xFA \r\n";
                if (NULL != light_period)
                        write(stdin_pipe[PIPE_WRITE], light_period, strlen(light_period));


                //==============================================================
                // Start Humidity measurement
                //==============================================================

                //Start measurement
                const char* start_humidity = "char-write-req 0x2c 01 \r\n";
                if (NULL != start_humidity)
                        write(stdin_pipe[PIPE_WRITE], start_humidity, strlen(start_humidity));

                //Turn on notifications
                const char* read_humidity = "char-write-req 0x2A 0100 \r\n";
                if (NULL != read_humidity)
                        write(stdin_pipe[PIPE_WRITE], read_humidity, strlen(read_humidity));

                //Set notification period to 2.5 sec
                const char* humidity_period = "char-write-req 0x2e 0xFA \r\n";
                if (NULL != humidity_period)
                        write(stdin_pipe[PIPE_WRITE], humidity_period, strlen(humidity_period));

                /*   //==============================================================
                // Start Barometer measurement
                //==============================================================

                //Start measurement
                const char* start_barometer = "char-write-req 0x34 01 \r\n";
                if (NULL != start_barometer)
                write(aStdinPipe[PIPE_WRITE], start_barometer, strlen(start_barometer));

                //Turn on notifications
                const char* read_barometer = "char-write-req 0x32 0100 \r\n";
                if (NULL != read_barometer)
                write(aStdinPipe[PIPE_WRITE], read_barometer, strlen(read_barometer));

                //Set notification period to 2.5 sec
                const char* barometer_period = "char-write-req 0x36 0xFA \r\n";
                if (NULL != barometer_period)
                write(aStdinPipe[PIPE_WRITE], barometer_period, strlen(barometer_period));
                 */

                //==============================================================
                // Process notifications
                //==============================================================
                int min_intents = 5;
                int intents = 0;

                const std::string temp_handle = "0x0021";
                const std::string light_handle = "0x0041";
                const std::string humidity_handle = "0x0029";
                const std::string barometer_handle = "0x0031";
                bool new_measurement = false;

                while(intents <= min_intents)
                {
                        std::string actual_line;

                        do {
                                read(stdout_pipe[PIPE_READ], &act_char, 1);
                                actual_line += act_char;
                        } while (act_char != '\n');

                        //Temperature notification
                        int temperature_index = actual_line.find(temp_handle);

                        if(temperature_index != -1)
                        {
                                new_measurement = true;
                                std::string temp;
                                unsigned int temperature_bytes[2];

                                //Extract bytes from string notifications
                                temp = actual_line.substr(temperature_index + 20, 2);
                                temperature_bytes[0] =  strtoul(temp.c_str(), 0, 16);

                                temp = actual_line.substr(temperature_index + 23,2);
                                temperature_bytes[1] =  strtoul(temp.c_str(), 0, 16);

                                //Merge bytes
                                unsigned int temperature_raw = (temperature_bytes[1] << 8) + temperature_bytes[0];
                                double temperature_double = static_cast<double>(temperature_raw);

                                //Compute and filter final value
                                if(temperature_double < 32768)
                                        temperature = temperature_double/128.0;              //Positive temperature values
                                else if (temperature_double > 32768)
                                        temperature = (temperature_double - 65536) / 128.0;  //Negative temperature values
                        }

                        //Light notification
                        int light_index = actual_line.find(light_handle);
                        if(light_index != -1)
                        {
                                new_measurement = true;
                                std::string temp;
                                unsigned int light_bytes[2];

                                //Extract bytes from string notifications
                                temp = actual_line.substr(light_index + 14, 2);
                                light_bytes[0] =  strtoul(temp.c_str(), 0, 16);

                                temp = actual_line.substr(light_index + 17, 2);
                                light_bytes[1] =  strtoul(temp.c_str(), 0, 16);

                                //Compute exponent and fraction according to formula
                                unsigned int exponent = (light_bytes[1] & 0b11110000) >> 4;
                                unsigned int fraction = ((light_bytes[1] & 0b00001111) << 8) + light_bytes[0];

                                //Compute final value
                                light = 0.01 * pow(2,exponent) * fraction;
                        }

                        //Humidity notification
                        int humidity_index = actual_line.find(humidity_handle);
                        if(humidity_index != -1)
                        {
                                new_measurement = true;
                                std::string temp;
                                unsigned int humidity_bytes[2];

                                //Extract bytes from string notification
                                temp = actual_line.substr(humidity_index + 20, 2);
                                humidity_bytes[0] =  strtoul(temp.c_str(), 0, 16);

                                temp = actual_line.substr(humidity_index + 23,2);
                                humidity_bytes[1] =  strtoul(temp.c_str(), 0, 16);

                                //Merge bytes
                                unsigned int humidity_raw = (humidity_bytes[1] << 8) + humidity_bytes[0];

                                //Clear 2 LSB
                                humidity_raw &= ~0x0003;

                                //Cast to double
                                humidity = static_cast<double>(humidity_raw);

                                //Compute final value
                                humidity = 125*(humidity/65536) - 6;
                        }

                        //Print if new measurement occured
                        if(new_measurement == true)
                        {
                                if (intents == min_intents)
                                {
                                        std::cout << "Refreshing values on cache..." << std::endl; 
                                        std::cout << "Temp: "        << std::setprecision(4)  << std::setw(5) << std::setfill(' ') << temperature << " [°C]"
                                                << "   Light: "    << std::setprecision(4) << std::setw(5) << std::setfill(' ') << light << " [lux]"
                                                << "   Humidity: " << std::setprecision(4) << std::setw(5) << std::setfill(' ') << humidity << " [%]"  << std::endl;
                                }
                                //Clear the flag
                                new_measurement = false;
                                intents++;

                                std::ofstream temperature_minute;
                                std::ofstream humidity_minute;
                                std::ofstream light_minute;

                                //Try to open the _MINUTE file for writing
                                /*      do {
                                        temperature_minute.open(TEMPERATURE_MINUTE_FILEPATH);
                                        } while((temperature_minute.is_open()) == 0);

                                        temperature_minute << temperature;
                                        temperature_minute.close();

                                        do {
                                        humidity_minute.open(HUMIDITY_MINUTE_FILEPATH);
                                        } while((humidity_minute.is_open()) == 0);

                                        humidity_minute << humidity;
                                        humidity_minute.close();

                                        do {
                                        light_minute.open(LIGHT_MINUTE_FILEPATH);
                                        } while((light_minute.is_open()) == 0);

                                        light_minute << light;
                                        light_minute.close();
                                 */
                        }
                        usleep(100000);
                }

                //==============================================================
                // Exit
                //==============================================================
                const char* exit_communication = "exit\r\n";
                if (NULL != exit_communication) {
                        write(stdin_pipe[PIPE_WRITE], exit_communication, strlen(exit_communication));
                }

                // done with these in this example program, you would normally keep these
                // open of course as long as you want to talk to the child
                close(stdin_pipe[PIPE_WRITE]);
                close(stdout_pipe[PIPE_READ]);
        }
        else
        {
                // failed to create child
                close(stdin_pipe[PIPE_READ]);
                close(stdin_pipe[PIPE_WRITE]);
                close(stdout_pipe[PIPE_READ]);
                close(stdout_pipe[PIPE_WRITE]);
        }
        return child_num;
}

