using Microsoft.IdentityModel.Tokens;
using Version_1_API.Model;

namespace Version_1_API.Auxiliary.ObserverPattern
{
    public class CmdProvider
    {
        private Dictionary<string, CmdRequester> _cmdRequesters;
        private Dictionary<string, CmdResultRequester> _resultRequesters;
        private Dictionary<string, Command> _commands;
        private Dictionary<string, CmdResult> _cmdResults;
        private static CmdProvider _instance;


        private CmdProvider()
        {
            _cmdRequesters = new Dictionary<string, CmdRequester>();
            _resultRequesters = new Dictionary<string, CmdResultRequester>();
            _commands = new Dictionary<string, Command>();
            _cmdResults = new Dictionary<string, CmdResult>();
        }

        public static CmdProvider GetInstance()
        {
            if (_instance == null)
            {
                _instance = new CmdProvider();
            }

            return _instance;
        }

        public void Subscribe(string id, CmdRequester requester)
        {
            if(string.IsNullOrEmpty(id))
            {
                return;
            }

            // a new cmd request starts a new sequence if command exection, so we cleanup after a past sequence
            Synchronize(id);

            if (!_cmdRequesters.ContainsKey(id))
            {
                _cmdRequesters.Add(id, requester);
            }
            else if (_cmdRequesters[id] == null)
            {
                _cmdRequesters[id] = requester;
            }
        }

        private void UnsubscribeCmdRequester(string id)
        {
            if (!string.IsNullOrEmpty(id) && !_cmdRequesters.Remove(id))
            {
                _cmdRequesters[id] = null;
            }
        }

        private void UnsubscribeResultRequester(string id)
        {
            if (!string.IsNullOrEmpty(id) && !_resultRequesters.Remove(id))
            {
                _resultRequesters[id] = null;
            }
        }

        public void Subscribe(string id, CmdResultRequester requester)
        {
            if(string.IsNullOrEmpty(id))
            {
                return;
            }

            if (!_resultRequesters.ContainsKey(id))
            {
                _resultRequesters.Add(id, requester);
            }
            else if (_resultRequesters[id] == null)
            {
                _resultRequesters[id] = requester;
            }
        }

        public Command Request(string id)
        {
            if(string.IsNullOrEmpty(id))
            {
                return new Command() { cmdString = "__init__" };
            }

            if (_commands.ContainsKey(id) && _commands[id] != null)
            {

                Command command = _commands[id];
                if (!_commands.Remove(id))
                {
                    _commands[id] = null; // nullify to avoid picking again
                }

                UnsubscribeCmdRequester(id); // remove requester as well so that a new request for the same id can come in (with a new state)
                return command;
            }

            UnsubscribeCmdRequester(id);
            return new Command() { cmdString = "__init__" }; // empty command
        }

        public CmdResult GetResult(string id)
        {
            if(string.IsNullOrEmpty(id))
            {
                return new CmdResult(String.Empty, String.Empty);
            }

            if (_cmdResults.ContainsKey(id) && _cmdResults[id] != null)
            {
                CmdResult cmdResult = _cmdResults[id];
                if (!_cmdResults.Remove(id))
                {
                    _cmdResults[id] = null;
                }

                UnsubscribeResultRequester(id);
                return cmdResult;
            }

            UnsubscribeResultRequester(id);
            return new CmdResult(id, String.Empty); // empty result
        }

        public bool SubmitCmd(Command command)
        {
            if (string.IsNullOrEmpty(command.targetId) || !_cmdRequesters.ContainsKey(command.targetId)) { // is their a request from the targetId?
                return false;
            }

            if (_commands.ContainsKey(command.targetId))
            {
                // replace command already there
                _commands[command.targetId] = command;
            }
            else
            {
                _commands.Add(command.targetId, command); // yes
            }

            NotifyCmdRequester(command.targetId); // requester at corresponding id to request
            return true;
        }

        public void SubmitCmdResult(CmdResult result) {
            if (string.IsNullOrEmpty(result.targetId) || !_resultRequesters.ContainsKey(result.targetId) || 
                _resultRequesters[result.targetId]==null)
            {
                return; // no cmd result requesters for this id/result
            } 

            if(_cmdResults.ContainsKey(result.targetId)) 
            {
                _cmdResults[result.targetId] = result; // overwrite
            }
            else
            {
                _cmdResults.Add(result.targetId, result);
            }

            NotifyResultRequester(result.targetId);
        }

        private void NotifyCmdRequester(string id)
        {
            if(!string.IsNullOrEmpty(id) && _cmdRequesters.ContainsKey(id)) // same check occurs in SubmitCmd()
            {
                _cmdRequesters[id].SetCanRequest();
            }
        }

        private void NotifyResultRequester(string id)
        {
            if( !string.IsNullOrEmpty(id) && _resultRequesters.ContainsKey(id))
            {
                _resultRequesters[id].SetCanGetResult();
            }
        }

        private void Synchronize(string id)
        {
            // sync commands and requesters for a new command exec sequence, clean after past sequence especially a failed one
            // past command has failed if the CmdResultRequester is still waiting

            NotifyResultRequester(id); // releasing waiting requester - empty result will return if command failed
        }
    }
}
