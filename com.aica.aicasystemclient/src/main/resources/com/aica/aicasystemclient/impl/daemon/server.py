#!/usr/bin/env python

from SimpleXMLRPCServer import SimpleXMLRPCServer
from SocketServer import ThreadingMixIn

import urllib
import urllib2
import json


class MethodRequest(urllib2.Request):
    def __init__(self, *args, **kwargs):
        if 'method' in kwargs:
            self._method = kwargs['method']
            del kwargs['method']
        else:
            self._method = None
        return urllib2.Request.__init__(self, *args, **kwargs)

    def get_method(self, *args, **kwargs):
        if self._method is not None:
            return self._method
        return urllib2.Request.get_method(self, *args, **kwargs)


class Response:
    def __init__(self, code, data = None):
        self.status_code = code
        self.json = json.loads(data) if data else None


class AICA:
    # noinspection HttpUrlsUsage
    def __init__(self, url = 'localhost', port = 8080, api_key = None):
        if not isinstance(port, int):
            port = int(port)

        if url.startswith('http://'):
            self._address = url + ':' + str(port) + '/api'
        elif '//' in url or ':' in url:
            raise ValueError('Invalid URL format ' + url)
        else:
            self._address = 'http://' + url + ':' + str(port) + '/api'

        self._protocol = None
        self._core_version = None
        self.__api_key = api_key
        self.__token = None

    def _endpoint(self, endpoint = ''):
        if self._protocol is None:
            self.protocol()
        return self.__raw_endpoint(self._protocol + '/' + endpoint)

    def __raw_endpoint(self, endpoint):
        return self._address + '/' + endpoint
    
    def __ensure_token(self):
        is_compatible = self._check_version()
        if not is_compatible:
            return
        if self.__token is not None:
            return
        req = MethodRequest(
            self._endpoint('auth/login'),
            headers={'Authorization': 'Bearer ' + self.__api_key},
            method='POST'
        )
        try:
            res = urllib2.urlopen(req)
            self.__token = json.load(res)['token']
            res.close()
        except urllib2.HTTPError as e:
            raise RuntimeError('Failed to authenticate: ' + str(e))


    @staticmethod
    def _safe_uri(uri):
        return urllib.quote_plus(uri)

    def request(self, method, endpoint, params=None, json_data=None):
        headers = {}
        retry = 2
        url = self._endpoint(endpoint)
        if params:
            url += '?' + urllib.urlencode(params)
        data = None
        if json_data is not None:
            data = json_data and json.dumps(json_data).encode('utf-8')
            headers['Content-Type'] = 'application/json'
        while retry > 0:
            if self.__api_key is not None:
                self.__ensure_token()
                headers['Authorization'] = 'Bearer ' + str(self.__token)
            req = MethodRequest(url, data=data, headers=headers, method=method.upper())
            try:
                res = urllib2.urlopen(req, timeout=5)
                response = Response(res.getcode(), res.read())
                res.close()
                return response
            except urllib2.HTTPError as e:
                if e.code == 401:
                    if self.__api_key is None:
                        break
                    self.__token = None
                else:
                    return Response(e.code)
            retry -= 1
        return None
    
    def _check_version(self):
        if self._core_version is None and self.core_version() is None:
            return False
        if self._core_version.startswith('4.') and self._core_version[2] in ['3', '4', '5']:
            return True
        return False

    def core_version(self):
        core_version = None
        try:
            res = urllib2.urlopen(self.__raw_endpoint('version'), timeout=5)
            core_version = json.load(res)
            res.close()
        except urllib2.URLError:
            pass
        self._core_version = core_version
        return self._core_version

    def protocol(self):
        protocol = None
        try:
            res = urllib2.urlopen(self.__raw_endpoint('protocol'), timeout=5)
            protocol = json.load(res)
            res.close()
        except urllib2.URLError:
            pass
        self._protocol = protocol
        return self._protocol

    def check(self):
        if self._protocol is None and self.protocol() is None:
            return False
        elif self._protocol != 'v2':
            return False

        if self._core_version is None and self.core_version() is None:
            return False
        
        if self.__api_key is None:
            return False
        return True


class Client:
    def is_reachable(self):
        return True

    def initialize(self, url, api_key):
        self.aica = AICA(url=url, api_key=api_key)
        return self.aica.check()
    
    def ensure_client(func):
        def wrapper(*args, **kwargs):
            if not hasattr(args[0], 'aica') or not args[0].aica.check():
                raise RuntimeError('AICA API is not connected')
            return func(*args, **kwargs)
        return wrapper
    
    @ensure_client
    def call_component_service(self, component, service, payload):
        endpoint = 'application/components/' + AICA._safe_uri(component) + '/service/' + AICA._safe_uri(service)
        return self.aica.request('PUT', endpoint, json_data={'payload': payload}).status_code == 202

    @ensure_client
    def call_controller_service(self, hardware, controller, service, payload):
        endpoint = 'application/hardware/' + AICA._safe_uri(hardware) + '/controller/' + AICA._safe_uri(controller) + '/service/' + AICA._safe_uri(service)
        return self.aica.request('PUT', endpoint, json_data={'payload': payload}).status_code == 202

    @ensure_client
    def load_component(self, component):
        return self.aica.request('PUT', 'application/components/' + AICA._safe_uri(component)).status_code == 202

    @ensure_client
    def load_controller(self, hardware, controller):
        endpoint = 'application/hardware/' + AICA._safe_uri(hardware) + '/controller/' + AICA._safe_uri(controller)
        return self.aica.request('PUT', endpoint).status_code == 202

    # @ensure_client
    # def load_hardware(self, hardware):
    #     return self.aica.request('PUT', 'application/hardware/' + AICA._safe_uri(hardware)).status_code == 202
    
    @ensure_client
    def set_component_parameter(self, component, parameter, value):
        endpoint = 'application/components/' + AICA._safe_uri(component) + '/parameter/' + AICA._safe_uri(parameter)
        return self.aica.request('PUT', endpoint, json_data={'value': value}).status_code == 202

    # @ensure_client
    # def set_application(self, application_name):
    #     safe_application_name = urllib.quote(application_name, safe='')
    #     res = self.aica.request('GET', 'data/applications/' + safe_application_name)

    #     if res.status_code == 200:
    #         return self.aica.request('PUT', 'application', json_data={'payload': res.json["yaml"]}).status_code == 204
    #     return False

    # @ensure_client
    # def start_application(self):
    #     return self.aica.request('PUT', 'application/state/transition', params={'action': 'start'}).status_code == 204

    # @ensure_client
    # def stop_application(self):
    #     return self.aica.request('PUT', 'application/state/transition', params={'action': 'stop'}).status_code == 204
    
    @ensure_client
    def set_component_parameter(self, component, parameter, value):
        endpoint = 'application/components/' + AICA._safe_uri(component) + '/parameter/' + AICA._safe_uri(parameter)
        return self.aica.request('PUT', endpoint, json_data={'value': value}).status_code == 202

    @ensure_client
    def set_controller_parameter(self, hardware, controller, parameter, value):
        endpoint = 'application/hardware/' + AICA._safe_uri(hardware) + '/controller/' + AICA._safe_uri(controller) + '/parameter/' + AICA._safe_uri(parameter)
        return self.aica.request('PUT', endpoint, json_data={'value': value}).status_code == 202

    @ensure_client
    def set_lifecycle_transition(self, component, transition):
        endpoint = 'application/components/' + AICA._safe_uri(component) + '/lifecycle/transition'
        return self.aica.request('PUT', endpoint, json_data={'transition': transition}).status_code == 202

    @ensure_client
    def activate_controller(self, hardware, controller):
        endpoint = 'application/hardware/' + AICA._safe_uri(hardware) + '/controllers'
        return self.aica.request('PUT', endpoint, params={'activate': controller}).status_code == 202

    @ensure_client
    def deactivate_controller(self, hardware, controller):
        endpoint = 'application/hardware/' + AICA._safe_uri(hardware) + '/controllers'
        return self.aica.request('PUT', endpoint, params={'deactivate': controller}).status_code == 202

    @ensure_client
    def unload_component(self, component):
        endpoint = 'application/components/' + AICA._safe_uri(component)
        return self.aica.request('DELETE', endpoint).status_code == 202

    @ensure_client
    def unload_controller(self, hardware, controller):
        endpoint = 'application/hardware/' + AICA._safe_uri(hardware) + '/controller/' + AICA._safe_uri(controller)
        return self.aica.request('DELETE', endpoint).status_code == 202

    # @ensure_client
    # def unload_hardware(self, hardware):
    #     endpoint = 'application/hardware/' + AICA._safe_uri(hardware)
    #     return self.aica.request('DELETE', endpoint).status_code == 202

    @ensure_client
    def manage_sequence(self, sequence_name, action):
        endpoint = 'application/sequences/' + AICA._safe_uri(sequence_name)
        return self.aica.request('PUT', endpoint, params={'action': AICA._safe_uri(action)}).status_code == 204


class MultithreadedSimpleXMLRPCServer(ThreadingMixIn, SimpleXMLRPCServer):
	pass

server = MultithreadedSimpleXMLRPCServer(("127.0.0.1", 40405))
server.RequestHandlerClass.protocol_version = "HTTP/1.1"
server.register_introspection_functions()
server.register_instance(Client())
server.serve_forever()
