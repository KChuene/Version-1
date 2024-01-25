# Version-1-Malware
An experimental, in-development, malicious software with RCE, and Data Exfiltration capabilities as a core.

It relies on the an ideal process of infection:
1. Send specially crafted SE emails, with link to malicious site, using a dictionary of addresses.
2. If recipients visit the linked page, the payload is smuggled within imbedded JScript and saved onto the recipients machine.
3. If recipients execute the payload, the payload starts up 3 modules that will help in controlling the recipient machine and further spreading the payload.

## Applications
1. <b> API </b> - Awaits connections from the C2 Client and the Payload, managing remote command execution by receiving commands from C2 Client and passing on to Payload, and receiving command results from Payload and passing them onto C2 Client. Also receives and saves exfiltrated data.
2. <b> C2 Client </b> - Means of connecting to the API to view online Payload instances and issue commands to each individually.
3. <b> Payload </b> - Executes MediaWriter, Exfiltrator and CmdInterpreter modules on compromised machines.


## Modules
1. <b>MediaWriter</b> - Writes the payload to volumes other than the current working volume, this includes volumes of external storage media (ex. USB Drives)
2. <b>Exfiltrator</b> - Traverses the recipient home directory (and subdirectories) to find and exfiltrate files of specific extensions. (the size of data sent is capped)
3. <b>CmdInterpreter</b> - Remote Command Execution, for shell and payload specific commands.


## Languages used
- C# (For the API)
- Java (For the mail sender and the payload)
- Python (For Command and Control client)

## Setup Instructions
1. API - The api currently binds to localhost, so if one would expose the API to other machines on the internal network one will need to setup the nginx proxy to redirect to the localhost port the API is listening on. All in all run the version 1 API exe to run the API.
2. Payload - Simply running (double-clicking) the version 1 paylod should do. It will attempt to connect to http://api.version1.local which should be the address of your proxy server to the API.
3. C2 Client - The c2 client connects to the proxy server address as well, that is http://api.version1.local run the following command to first authenticate (default/fixed credentials):
<i><version-1_c2_cli.exe> -api http://api.version1.local -email version1@gmail.com -password V3rS!0nOne_On1y</i>

Note that version1@gmail.com is not linked to a gmail account. (dummy credentials)
