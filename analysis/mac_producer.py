import os
import json
import time
import threading
import requests

class MAC_Map:
    def __init__(self):
        self.lock = threading.Lock()
        self.mac_producer = {}
        self._read_mac_producer_table()

    @staticmethod
    def _load_broken_json(self, text):
        if text[0] != '{':
            raise ValueError()
        left_num, end = 1, 0
        for i in range(1, len(text)):
            if text[i] == '{':
                left_num += 1
            if text[i] == '}':
                left_num -= 1
                if l == 0:
                    end = l
                    break
        if end:
            response = json.loads(text[:end])
            return response
        else:
            print(e)
            print(text)
            raise ValueError()

    def _get_producer_online(self, mac):
        while True:
            try:
                r = requests.get(
                    "http://www.imfirewall.com/ip-mac-lookup/get_mac_info.php?mac=%s&_=%d" % (mac, int(time.time() * 1000))
                )
            except requests.ConnectionError as e:
                print(e)
            except requests.ConnectTimeout as e:
                print(e)
                print("Please Check the network condition")
            try:
                response = json.loads(r.text)
                break
            except json.JSONDecodeError as e:
                try:
                    response = self._load_broken_json(r.text)
                    break
                except ValueError:
                    return 'Null'
        if response['success']:
            return response['result']['mac_producer']
        else:
            return 'Null'

    def find_producer_online(self, mac_prefix):
        if mac_prefix in self.mac_producer:
            return
        producer = self._get_producer_online(mac_prefix + ':22:22:22')
        self.lock.acquire()
        self.mac_producer[mac_prefix] = producer
        self.save()
        self.lock.release()

    def save(self):
        with open('./mac_producer.txt', 'w', encoding='utf-8') as f:
            for mac_pre in self.mac_producer.keys():
                f.write(mac_pre + '\t' + self.mac_producer[mac_pre] + '\n')

    def _read_mac_producer_table(self):
        # Only save the pre 3 bytes address
        if not os.path.exists('./mac_producer.txt'):
            return
        mac_map = self.mac_producer
        with open('./mac_producer.txt', encoding='utf-8') as f:
            line = f.readline()
            while line:
                if len(line) == 1:
                    line = f.readline()
                    continue
                line = line[:-1]
                mac, producer = line.split('\t')
                mac_map[mac] = producer
                line = f.readline()

    def get_producer(self, mac):
        if len(mac) == 17:
            mac_prefix = mac[:8]
        elif len(mac) == 8:
            mac_prefix = mac
        elif mac == 'null':
            return 'Rem'
        else:
            raise ValueError('MAC Adrress Format Error')
        if mac_prefix not in self.mac_producer:
            self.find_producer_online(mac_prefix)
        return self.mac_producer[mac_prefix]
