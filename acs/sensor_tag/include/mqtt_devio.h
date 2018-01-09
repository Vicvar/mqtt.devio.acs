#ifndef __MQTT_DEVIO_H__
#define __MQTT_DEVIO_H__

#include <baciDevIO.h>

#include <MQTTClient.h>
#include <mqtt/client.h>


namespace mqtt
{
    class acs_callback : public virtual mqtt::callback
    {
        mqtt::client& cli_;

        void connected (const std::string& cause) override;
        void connection_lost(const std::string& cause) override;
        void message_arrived(mqtt::const_message_ptr msg) override;
        void delivery_complete(mqtt::delivery_token_ptr token) override;

        std::string& topic_;


        public:
        acs_callback(mqtt::client& cli, std::string& topic);
    };

    class mqtt_devio: public virtual DevIO<CORBA::Double>
    {
        public:
        mqtt_devio(const std::string& mqtt_brk_addr,
                const std::string& baci_name);
        virtual ~mqtt_devio();

        virtual bool initializeValue();
        virtual CORBA::Double read(ACS::Time& timestamp);
        virtual void write(const CORBA::Double& value, ACS::Time& timestamp);

        private:
        std::string mqtt_brk_addr;
        std::string topic;
        std::string client_name;

        acs_callback *cb_;
        mqtt::client *client_;
        MQTTClient_connectOptions conn_opts;
        double value;

//        int message_arrived(void* context, char* topicName, 
//                            int topicLen, MQTTClient_message* m);

    };
};

#endif
