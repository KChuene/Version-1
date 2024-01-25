using Version_1_API.Model;

namespace Version_1_API.Auxiliary.ObserverPattern
{
    public class CmdResultRequester
    {
        private bool _canGetResult = false; // default state
        

        public bool CanGetResult() { return _canGetResult; }
        public void SetCanGetResult() { _canGetResult = true; }

        public CmdResult GetResult(string id)
        {
            CmdProvider provider = CmdProvider.GetInstance();
            return provider.GetResult(id);
        }
    }
}
