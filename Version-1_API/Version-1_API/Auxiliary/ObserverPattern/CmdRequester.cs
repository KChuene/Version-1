using Version_1_API.Model;

namespace Version_1_API.Auxiliary.ObserverPattern
{
    public class CmdRequester
    {
        private bool _canRequest = false;

        public void SetCanRequest()
        {
            _canRequest = true;
        }

        public bool CanRequest() { return _canRequest; }

        public Command Request(string clientId)
        {
            CmdProvider provider = CmdProvider.GetInstance();

            return provider.Request(clientId);
        }
    }
}
