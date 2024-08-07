from confluent_kafka import Producer, KafkaException
import configparser
import time

# Read configuration from client.properties
config = configparser.ConfigParser()
try:
    config.read('client.properties')
except configparser.Error as e:
    print(f"Error reading configuration file: {e}")
    exit(1)

try:
    conf = {
        'bootstrap.servers': config['default']['bootstrap.servers'],
        'security.protocol': config['default']['security.protocol'],
        'sasl.mechanisms': config['default']['sasl.mechanism'],
        'sasl.username': config['default']['sasl.username'],
        'sasl.password': config['default']['sasl.password']
    }
except KeyError as e:
    print(f"Missing configuration parameter: {e}")
    exit(1)

try:
    producer = Producer(conf)
except KafkaException as e:
    print(f"Failed to create producer: {e}")
    exit(1)

def produce_events(err, msg):
    if err is not None:
        print(f"Message delivery failed: {err}")
    else:
        print(f"Message delivered to {msg.topic()} [{msg.partition()}]")

try:
    topic = config['default']['topic']
    test_data = [
        "<Event RegId=\"371\" Time=\"20240601122254\" Type=\"Ev_Custom\" storeId=\"25001000\">\n          \n    <Ev_Custom>\n                \n        <Info code=\"0000\" data=\"%7B%22firstName%22%3A%22GILLES%22%2C%22customerActiveMembership%22%3Atrue%2C%22restaurantActiveLoyaltyProgram%22%3Afalse%2C%22customerRewards%22%3A%7B%22rewards%22%3A%5B%7B%22rewardRef%22%3A%2283874595%22%2C%22rewardType%22%3A%22POINT%22%2C%22choices%22%3A%5B%7B%22label%22%3A%22PETITE%20BOISSON%20CHAUDE%22%2C%22pmixs%22%3A%5B%225551%22%2C%225503%22%2C%225555%22%2C%225500%22%2C%225550%22%2C%225520%22%2C%225552%22%2C%225510%22%2C%225553%22%2C%225501%22%2C%225554%22%2C%225502%22%2C%225504%22%2C%225556%22%2C%229994%22%2C%2210537%22%2C%2210665%22%2C%2210662%22%2C%225521%22%2C%2210658%22%2C%2210656%22%5D%7D%2C%7B%22label%22%3A%22PETITE%20BOISSON%22%2C%22pmixs%22%3A%5B%226012%22%2C%226000%22%2C%226181%22%2C%229487%22%2C%2211076%22%2C%222870%22%2C%2213052%22%5D%7D%5D%2C%22channels%22%3A%5B%22KIOSK%22%2C%22WEB%22%2C%22COUNTER%22%2C%22DRIVE%22%5D%2C%22validUntil%22%3A%222024%2F12%2F31%22%2C%22points%22%3A15%7D%2C%7B%22rewardRef%22%3A%2283874596%22%2C%22rewardType%22%3A%22POINT%22%2C%22choices%22%3A%5B%7B%22label%22%3A%22PETITS%20SANDWICHS%22%2C%22pmixs%22%3A%5B%222010%22%2C%222020%22%2C%222000%22%5D%7D%2C%7B%22label%22%3A%22ACCOMPAGNEMENTS%22%2C%22pmixs%22%3A%5B%223010%22%2C%223015%22%5D%7D%2C%7B%22label%22%3A%224%20CHICKEN%20McNUGGETS%E2%84%A2%22%2C%22pmixs%22%3A%5B%222078%22%5D%7D%5D%2C%22channels%22%3A%5B%22KIOSK%22%2C%22WEB%22%2C%22COUNTER%22%2C%22DRIVE%22%5D%2C%22validUntil%22%3A%222024%2F12%2F31%22%2C%22points%22%3A30%7D%2C%7B%22rewardRef%22%3A%2283874597%22%2C%22rewardType%22%3A%22POINT%22%2C%22choices%22%3A%5B%7B%22label%22%3A%22SUNDAE%22%2C%22pmixs%22%3A%5B%229798%22%5D%7D%2C%7B%22label%22%3A%22McFLURRY%E2%84%A2%22%2C%22pmixs%22%3A%5B%229836%22%5D%7D%2C%7B%22label%22%3A%22DESSERT%22%2C%22pmixs%22%3A%5B%222290%22%2C%222325%22%2C%224383%22%2C%224079%22%2C%2210552%22%2C%2211480%22%2C%228000%22%2C%228020%22%2C%228040%22%2C%2210888%22%2C%2210887%22%2C%2210886%22%2C%2210885%22%2C%229203%22%2C%229251%22%2C%229250%22%2C%229210%22%2C%229253%22%2C%229252%22%2C%229260%22%2C%229262%22%2C%229261%22%2C%229201%22%2C%229195%22%2C%229194%22%2C%2210951%22%2C%2211439%22%2C%2211495%22%2C%22502421%22%5D%7D%5D%2C%22channels%22%3A%5B%22KIOSK%22%2C%22WEB%22%2C%22COUNTER%22%2C%22DRIVE%22%5D%2C%22validUntil%22%3A%222024%2F12%2F31%22%2C%22points%22%3A45%7D%2C%7B%22rewardRef%22%3A%2283874598%22%2C%22rewardType%22%3A%22POINT%22%2C%22choices%22%3A%5B%7B%22label%22%3A%22SANDWICHS%22%2C%22pmixs%22%3A%5B%222050%22%2C%224950%22%2C%222154%22%2C%222258%22%2C%2210732%22%2C%2210734%22%2C%225390%22%2C%223479%22%2C%222137%22%2C%222120%22%2C%222070%22%2C%222080%22%2C%222030%22%2C%2213111%22%2C%2213145%22%5D%7D%5D%2C%22channels%22%3A%5B%22KIOSK%22%2C%22WEB%22%2C%22COUNTER%22%2C%22DRIVE%22%5D%2C%22validUntil%22%3A%222024%2F12%2F31%22%2C%22points%22%3A60%7D%2C%7B%22rewardRef%22%3A%2283874599%22%2C%22rewardType%22%3A%22POINT%22%2C%22choices%22%3A%5B%7B%22label%22%3A%22MENU%20BEST%20OF%E2%84%A2%22%2C%22pmixs%22%3A%5B%2213463%22%2C%2213464%22%2C%2213465%22%2C%2213466%22%2C%2213467%22%2C%2213469%22%2C%2213470%22%2C%2213471%22%2C%2213472%22%2C%2213474%22%2C%2213475%22%2C%2213687%22%5D%7D%2C%7B%22label%22%3A%22MENU%20MCFIRST%E2%84%A2%22%2C%22pmixs%22%3A%5B%2213476%22%2C%2213477%22%2C%2213478%22%5D%7D%5D%2C%22channels%22%3A%5B%22KIOSK%22%2C%22WEB%22%2C%22COUNTER%22%2C%22DRIVE%22%5D%2C%22validUntil%22%3A%222024%2F12%2F31%22%2C%22points%22%3A75%7D%5D%2C%22balance%22%3A101%7D%2C%22orders%22%3A%5B%5D%2C%22resultCode%22%3A%22Ok%22%7D\"/>\n              \n    </Ev_Custom>\n        \n</Event>\n",

    ]
    volumetrics = int(config['default']['volumetrics'])
except KeyError as e:
    print(f"Missing configuration parameter: {e}")
    exit(1)

for data in test_data:
    print(f"Producing message to topic {topic}")
    start_time = time.time()
    for i in range(volumetrics):
        try:
            producer.produce(topic, key="250001", value=data.encode('utf-8'), callback=produce_events)
            producer.poll(0)
        except KafkaException as e:
            print(f"Failed to produce message: {e}")

try:
    print("Flushing producer...")
    producer.flush()
    print("Producer flush complete")
    end_time = time.time()
    print(f"Message {volumetrics} produced & flushed in {end_time - start_time:.2f} seconds")
except KafkaException as e:
    print(f"Failed to flush producer: {e}")
