using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using Version_1_API.Model;
using Version_1_API.Services;

namespace Version_1_API.Controllers
{
    [Route("api/accountManager")]
    [ApiController]
    public class AccountManagerController : Controller
    {
        private AccountManagerSvc _accountManagerSvc;


        [HttpPost("auth")]
        public ObjectResult Authenticate(RequestParameter[] credentials)
        {
            _accountManagerSvc = AccountManagerSvc.GetInstance();

            if (RequestParameter.get("email", credentials).IsNullOrEmpty())
            {
                return BadRequest(new Response((int)EApiExecCode.ERROR, "Empty email provided.", String.Empty));
            }

            return Ok(_accountManagerSvc.AuthenticateAddress(
                RequestParameter.get("email", credentials),
                RequestParameter.get("password", credentials)
            ));
        }

    }
}
