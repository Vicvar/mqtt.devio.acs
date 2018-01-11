#ifndef __MQTT_DEVIO_H__
#define __MQTT_DEVIO_H__

#include <baciDevIO.h>

#include <MQTTClient.h>
#include <mqtt/async_client.h>


namespace mqtt
{
    class mqtt_devio;
    class mqtt_write;
    class mqtt_read;

    class acs_callback : public virtual mqtt::callback
    {
        mqtt::async_client& cli_;

        void connected (const std::string& cause) override;
        void connection_lost(const std::string& cause) override;
        void message_arrived(mqtt::const_message_ptr msg) override;
        void delivery_complete(mqtt::delivery_token_ptr token) override;

        std::string& topic_;
        mqtt_devio* devio_;

        public:
        acs_callback(mqtt::async_client& cli, std::string& topic, mqtt_devio* devio);
    };


    class mqtt_devio: public virtual DevIO<CORBA::Double>
    {
        friend class acs_callback;
        public:
        mqtt_devio(const std::string& mqtt_brk_addr,
                const std::string& topic);
        virtual ~mqtt_devio();

        virtual bool initializeValue();
        virtual CORBA::Double read(ACS::Time& timestamp);
        virtual void write(const CORBA::Double& value, ACS::Time& timestamp);

        protected:
        std::string mqtt_brk_addr;
        std::string topic;
        std::string client_name;

        acs_callback *cb_;
        mqtt::async_client *client_;
        MQTTClient_connectOptions conn_opts;
        double value;

//        int message_arrived(void* context, char* topicName, 
//                            int topicLen, MQTTClient_message* m);

    };

    class mqtt_read: public mqtt_devio
    {
	public:
	mqtt_read(const std::string& mqtt_brk_addr, const std::string& topic);
	virtual ~mqtt_read();

    };
    class mqtt_write:  public mqtt_devio
    {
	public:
	mqtt_write(const std::string& mqtt_brk_addr, const std::string& topic);
	virtual ~mqtt_write();
	void publish(const std::string& msg);

    };

};

#endif
