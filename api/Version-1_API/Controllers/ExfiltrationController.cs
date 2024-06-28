using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Protocols.OpenIdConnect;
using Microsoft.IdentityModel.Tokens;
using Version_1_API.Model;
using Version_1_API.Services;
using System.Text.RegularExpressions;
using System.Web;

namespace Version_1_API.Controllers
{
    [Route("api/exfiltration")]
    [ApiController]
    public class ExfiltrationController : Controller
    {
        private ExfiltrationSvc _exfiltrationSvc;

        public ExfiltrationController()
        {
            _exfiltrationSvc = ExfiltrationSvc.GetInstance();
        }

        [HttpPost]
        public ObjectResult SubmitFile(FileSubmission file)
        {
            if(file !=  null)
            {
                if (file.isBinary && !IsBase64Content(file.content))
                {
                    return BadRequest(new Response((int)EApiExecCode.ERROR, "Invalid content format.", false.ToString()));
                }
            }
            else
            {
                return BadRequest(new Response((int)EApiExecCode.ERROR, "Invalid file submission.", false.ToString()));
            }
            
            return Ok(_exfiltrationSvc.SubmitFile(file));
            
        } 


        private bool IsBase64Content(string content)
        {
            if (content == null) return false;

            string pattern = "[A-Za-z0-9+/=]";

            bool lengthMatch = content.Length % 4 == 0;
            bool contentMatch = new Regex(pattern).Matches(content).Count == content.Length ; 
            if(!(lengthMatch && contentMatch))
            {
                // 1: content length not multiple of 4
                // 2: not all chars in content are expected base64 string chars
                return false;
            }

            return true;
        }
    }
}
