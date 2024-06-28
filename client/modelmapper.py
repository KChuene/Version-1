def parameter(name, value):
    return {
        "Name": name,
        "Value": value
    }

def command(session_token, target_id, is_shell, command_str):
    return {
        "issuerId": session_token,
        "targetId": target_id,
        "isShellCmd": is_shell,
        "cmdString": command_str
    }