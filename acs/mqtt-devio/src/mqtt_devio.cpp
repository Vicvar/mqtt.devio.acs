#include "mqtt_devio.h"


using namespace mqtt;



acs_callback::acs_callback(mqtt::async_client& cli, std::string& topic, mqtt_devio* devio) : 
cli_(cli), topic_(topic), devio_(devio) {}

void acs_callback::connected (const std::string& cause)
{

    std::cout << "\nConnected: " << cause << std::endl;
std::cout << "\nTopic: " << topic_ << std::endl;
    cli_.subscribe(topic_, 1);
    std::cout << "OK" <<std::endl;
}

void acs_callback::connection_lost(const std::string& cause)
{
    std::cout << "\nConnection lost";
    if (!cause.empty())
        std::cout << ": " << cause << std::endl;
    std::cout << std::endl;
}

void acs_callback::message_arrived(mqtt::const_message_ptr msg)
{
std::cout << "\nMessage arrived";
    try {
        CORBA::Double value = std::stod(msg->get_payload_str());
        devio_->value = value;
	std::cout << "Message sent ";
	
    }
    catch (...) {
        std::cout << "FAILED PARSING ";
        std::cout << msg->get_topic() << ": " << msg->get_payload_str() << std::endl;
    }
}

void acs_callback::delivery_complete(mqtt::delivery_token_ptr token)
{
}

mqtt_devio::mqtt_devio(const std::string& mqtt_brk_addr, const std::string& baci_name, const std::string& client_name):
        mqtt_brk_addr(mqtt_brk_addr), topic(baci_name), 
        client_name(baci_name + client_name), cb_(NULL), client_(NULL)
{
    connect_options connOpts;
    connOpts.set_keep_alive_interval(20);
    connOpts.set_clean_session(false);
    connOpts.set_automatic_reconnect(true);

    client_ = new mqtt::async_client(mqtt_brk_addr, topic);
    cb_ = new acs_callback(*client_, topic, this);
    client_->set_callback(*cb_);

    
}

bool mqtt_devio::initializeValue()
{
    return true;
}

CORBA::Double mqtt_devio::read(ACS::Time& timestamp)
{
    return value;
}


void mqtt_devio::write(const CORBA::Double& value, ACS::Time& timestamp)
{

}

mqtt_devio::~mqtt_devio()
{
    AUTO_TRACE(__PRETTY_FUNCTION__);
    try {
        std::cout << "\nDisconnecting from the MQTT server..." << std::flush;
        client_->disconnect()->wait();
        std::cout << "OK" << std::endl;
    }
    catch (const mqtt::exception& exc) {
        std::cerr << exc.what() << std::endl;
    }
    delete cb_;
    delete client_;
}

mqtt_read::mqtt_read(const std::string& mqtt_brk_addr, const std::string& baci_name, const std::string& client_name) : mqtt_devio(mqtt_brk_addr, baci_name, client_name)
{
    connect_options connOpts;
    connOpts.set_keep_alive_interval(20);
    connOpts.set_clean_session(false);
    connOpts.set_automatic_reconnect(true);
    client_->connect(connOpts)->wait();
    try {
        std::cout << "Connecting to the MQTT server..." << std::flush;
        
        client_->start_consuming();
        client_->subscribe(topic, 1)->wait();
        std::cout << "OK" << std::endl;
    }
    catch (const mqtt::exception& exc) {
        std::cerr << "\nERROR: Unable to connect to MQTT server: '"
            << mqtt_brk_addr << "'" << std::endl;
        throw exc;
    }
}

mqtt_write::mqtt_write(const std::string& mqtt_brk_addr, const std::string& baci_name, const std::string& client_name) : mqtt_devio(mqtt_brk_addr, baci_name, client_name)
{
    connect_options connOpts;
    connOpts.set_keep_alive_interval(20);
    connOpts.set_clean_session(true);
    connOpts.set_automatic_reconnect(true);   
    client_->connect(connOpts)->wait();
}

void mqtt_write::publish(const std::string& msg)
{
	
    try {
	
	std::cout << "HERE" << topic << std::endl;
        mqtt::message_ptr  pub_msg = mqtt::make_message(topic, msg);
	pub_msg->set_qos(1);
	client_->publish(pub_msg);
        std::cout << "OK" << std::endl;
    }
    catch (const mqtt::exception& exc) {
        std::cerr << "\nERROR: Unable to connect to MQTT server: '"
            << mqtt_brk_addr << "'" << std::endl;
        throw exc;
    }
}

mqtt_write::~mqtt_write()
{
try {
        client_->unsubscribe(topic)->wait();
        client_->stop_consuming();
    }
    catch (const mqtt::exception& exc) {
        std::cerr << exc.what() << std::endl;
    }
}
mqtt_read::~mqtt_read()
{

}
