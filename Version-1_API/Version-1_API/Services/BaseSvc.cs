using System.Collections;
using System.Security.Cryptography;
using System.Text;
using Version_1_API.Auxiliary;
using Version_1_API.Model;

namespace Version_1_API.Services
{
    public class BaseSvc
    {
        private static BaseSvc _instance;
        private static Dictionary<string, Connection> ClientIDs = new Dictionary<string, Connection>();

        public static BaseSvc GetInstance()
        {
            if (_instance == null)
            {
                _instance = new BaseSvc();
            }
            return _instance;
        }

        public Response GetPayload()
        {
            byte[] payload = FileHandler.ReadBinaryFile("./Update/payload.exe");

            if (payload != null)
            {
                string base64Encoding = Convert.ToBase64String(payload);
                return new Response((int)EApiExecCode.SUCCESS, "Payload retrieved.", base64Encoding);
            }

            return new Response((int)EApiExecCode.FAIL, "Failed to retrieve payload.", String.Empty);
        }

    } 
}
