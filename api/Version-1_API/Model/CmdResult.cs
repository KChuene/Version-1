namespace Version_1_API.Model
{
    public class CmdResult
    {
        public string targetId { get; set; }
        public string? resultString { get; set; }

        public CmdResult() { }

        public CmdResult(string id, string result) { 
            targetId = id;
            resultString = result;
        }
    }
}
