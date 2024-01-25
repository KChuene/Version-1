namespace Version_1_API.Model
{
    public class Command
    {
        public string issuerId {  get; set; }
        public string targetId { get; set; }
        public bool isShellCmd { get; set; }
        public string cmdString { get; set; }
    }
}
