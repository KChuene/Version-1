import sys
import httpclient


def safe_read(option, args):
    program_options = {"-api", "-session", "-target"}
    for item in args:
        if item == option and args.index(item)==len(args)-1:
            print(f"[!] Expected value for option {option}. None found.")
            sys.exit(-1)

        if item == option:
            value = args[ args.index(option) + 1]
            if value in program_options:
                print(f"[!] Invalid value {value} for option {option}.")
                sys.exit(-1)

            return value

    print("[!] Insufficient args provided.")
    sys.exit(-1)

def prompt(api_url, session, target):
    try:
        httpClient = httpclient.HttpClient(api_url)

        while True:
            cmd = input(f"cmd ({target}) $: ")
            if cmd == "exit":
                print("[i] Bye bye!")
                sys.exit(0)

            if cmd.strip():
                cmd_result = httpClient.api_submit_cmd(session, target, True, cmd)
                print(f"\n{cmd_result}\n")

    except KeyboardInterrupt:
        print("\n[i] Interrupt detected. Terminating.")
        sys.exit(-1)

    except Exception:
        print("\n[!] Unexpected error. Terminating.")
        sys.exit(-1)

if __name__=="__main__":
    api_url = safe_read("-api", sys.argv)
    session = safe_read("-session", sys.argv)
    target = safe_read("-target", sys.argv)

    prompt(api_url, session, target)


