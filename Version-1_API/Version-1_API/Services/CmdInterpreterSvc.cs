using System.Security.Cryptography;
using System.Text;
using Version_1_API.Auxiliary.ObserverPattern;
using Version_1_API.Model;

namespace Version_1_API.Services
{
    public class CmdInterpreterSvc
    {
        private static CmdInterpreterSvc _instance;
        private static Dictionary<string, Connection> _connections = new Dictionary<string, Connection>();
        private static Dictionary<string, Connection> _listeners = new Dictionary<string, Connection>(); 

        public static CmdInterpreterSvc GetInstance()
        {
            if (_instance == null)
            {
                _instance = new CmdInterpreterSvc();
            }

            return _instance;
        }

        public Connection[] GetListeners()
        {
            return _listeners.Values.ToArray();
        }


        public Response CreateID(int idLength, string clientName)
        {
            int[] start = { 48, 65, 97 }; // start of unicode subsets to choose from
            int[] end = { 58, 91, 123 }; // end of unicode subsets for each start
            idLength = (idLength < 8) ? 8 : idLength;

            StringBuilder builder = null;
            bool isDuplicate = true;
            for (int retryCount = 1; retryCount <= _connections.Count + 1; retryCount++)  // retry on duplicates as many times as ther are ids + 1
            {
                builder = new StringBuilder();

                for (int count = 1; count <= idLength; count++)
                {
                    int unicodeSubsetIndex = RandomNumberGenerator.GetInt32(0, start.Length);

                    builder.Append(Convert.ToChar(
                        RandomNumberGenerator.GetInt32(start[unicodeSubsetIndex], end[unicodeSubsetIndex]))); // add random char from unicode subset
                }

                if (_connections.ContainsKey(builder.ToString()))
                {
                    continue; // retry
                }

                isDuplicate = false;
            }
            string clientId = (builder != null)? builder.ToString(): String.Empty;

            _connections.Add(clientId, new Connection()
            {
                id = clientId,
                clientName = clientName
            });

            if (isDuplicate)
            {
                return new Response((int)EApiExecCode.FAIL, "No allocatable id.", String.Empty);
            }

            return new Response(
                (int)EApiExecCode.SUCCESS, "Connection id created.", clientId);
        }

        public Command RequestCmd(string clientId)
        {
            CmdRequester requester = new(); // new requester so that CanRequest state is reset, hence we can request again
            CmdProvider provider = CmdProvider.GetInstance();

            provider.Subscribe(clientId, requester);
            if (!_listeners.ContainsKey(clientId) && _connections.ContainsKey(clientId))
            {
                _listeners.Add(clientId, _connections[clientId]); // only add request if not present and is from connection, else ignore new request
            }
            else if (!_connections.ContainsKey(clientId))
            {
                // reconnect
                _listeners.Remove(clientId);
                return new Command() { issuerId = String.Empty, targetId = clientId, cmdString = "__init__" };
            }

            while(!requester.CanRequest())
            {
                // busy wait
            }

            _listeners.Remove(clientId); // listening over, so remove
            return requester.Request(clientId);
        }

        public CmdResult SubmitCmd(Command command)
        {
            CmdResultRequester resultRequester = new(); // new result requester for new state
            CmdProvider provider = CmdProvider.GetInstance();

            bool submitted = provider.SubmitCmd(command);
            if(submitted)
            {
                provider.Subscribe(command.targetId, resultRequester);
                while (!resultRequester.CanGetResult())
                {
                    // busy wait
                }

                return resultRequester.GetResult(command.targetId);
            }

            return new CmdResult(command.targetId, "Target not available."); 
        }

        public void SubmitCmdResult(CmdResult cmdResult)
        {
            CmdProvider provider = CmdProvider.GetInstance();
            provider.SubmitCmdResult(cmdResult);
        }
    }
}
